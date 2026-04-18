package com.kawach.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawach.model.AuditResponse;
import com.kawach.model.FraudScanRequest;
import com.kawach.model.FraudScanResponse;
import com.kawach.model.ThreatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;

    public GeminiService(WebClient.Builder webClientBuilder,
                         ObjectMapper objectMapper,
                         @Value("${gemini.api.key}") String apiKey,
                         @Value("${gemini.api.url}") String apiUrl) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public Mono<AuditResponse> analyzeToS(String rawText) {
        log.info("Analyzing ToS with Live Gemini API...");
        log.info("Incoming raw text length: {}", rawText != null ? rawText.length() : 0);
        String safeText = cleanText(rawText);
        String prompt = "You will receive a 2,000-character snippet of a website. You are looking at a summary snippet. Identify the 3 most important risks in these 2,000 characters. Explain everything in plain, simple English as if talking to a teenager. No legal jargon. Instead of 'Third-party data ingestion,' say 'They share your info with other companies.' Output ONLY a valid JSON object matching this exact schema: {\"verdict\": \"AGREE | CAUTION | REJECT\", \"verdictReason\": \"One sentence explaining the verdict.\", \"summary\": [\"Bullet 1\", \"Bullet 2\", \"Bullet 3\"], \"flags\": [{\"severity\": \"Red\", \"point\": \"Short title\", \"explanation\": \"Detailed risk\", \"exactQuote\": \"EXACT 5-8 word substring from the text\"}]}. For exactQuote, you MUST copy the text character-for-character from the source. Do not change a single comma or capital letter. If you cannot find a perfect match, leave it blank. This quote will be used for exact DOM string matching. Do NOT wrap the JSON in markdown blocks. Just raw JSON.\n\nText to analyze:\n" + safeText;

        return callGemini(prompt)
                .flatMap(responseText -> {
                    try {
                        String sanitized = sanitizeJson(responseText);
                        AuditResponse response = objectMapper.readValue(sanitized, AuditResponse.class);
                        return Mono.just(response);
                    } catch (Exception e) {
                        log.error("Failed to parse JSON response from Gemini: {}", responseText, e);
                        return Mono.just(new AuditResponse(
                                "CAUTION", 
                                "Failed to parse AI response.", 
                                List.of(), 
                                List.of(new ThreatResponse("Yellow", "Analysis Error", "Failed to parse AI response.", ""))
                        ));
                    }
                })
                .timeout(Duration.ofSeconds(45))
                .onErrorResume(e -> {
                    log.error("Error/Timeout in analyzeToS: ", e);
                    return Mono.just(new AuditResponse(
                            "CAUTION", 
                            "The document is too large or the AI provider timed out.", 
                            List.of(), 
                            List.of(new ThreatResponse("Yellow", "Analysis Delayed", "Provider timeout or text too large.", ""))
                    ));
                });
    }

    public Mono<FraudScanResponse> scanFraud(FraudScanRequest request) {
        log.info("Scanning Fraud with Live Gemini API...");
        String prompt = "You will receive a 2,000-character snippet of a website. Perform a high-density security audit. Be concise but specific. Verification Logic: You are an elite cybersecurity analyst. If the site is a known, high-reputation domain like facebook.com, https://www.google.com/search?q=google.com, or microsoft.com, you must assign a 0% Scam Probability. Focus only on potential phishing, typo-squatting (e.g., https://www.google.com/search?q=faceb0ok.com), or predatory data clauses in the ToS. Output ONLY a valid JSON object with keys: 'scamProbability' (integer 0-100), 'riskLevel' (strictly 'High', 'Medium', or 'Low'), and 'findings' (a list of 2 short strings explaining the risk). Do NOT wrap the JSON in markdown blocks. Just raw JSON.\n\nURL: " + request.url() + "\nEmails: " + request.detectedEmails();

        return callGemini(prompt)
                .flatMap(responseText -> {
                    try {
                        String sanitized = sanitizeJson(responseText);
                        FraudScanResponse response = objectMapper.readValue(sanitized, FraudScanResponse.class);
                        return Mono.just(response);
                    } catch (Exception e) {
                        log.error("Failed to parse JSON object from Gemini: {}", responseText, e);
                        return Mono.just(new FraudScanResponse(50, "Medium", List.of("Failed to parse AI analysis.", "Proceed with caution.")));
                    }
                })
                .timeout(Duration.ofSeconds(45))
                .onErrorResume(e -> {
                    log.error("Error/Timeout in scanFraud: ", e);
                    return Mono.just(new FraudScanResponse(0, "Processing Error", List.of("The AI provider is currently under heavy load. Please try again in a few seconds.")));
                });
    }

    private String cleanText(String input) {
        if (input == null) return "";
        String cleaned = input.replace("\"", "'")
                              .replaceAll("[\\n\\r\\t]+", " ")
                              .replaceAll("[^\\x00-\\x7F]", "");
        return cleaned.length() > 2000 ? cleaned.substring(0, 2000) : cleaned;
    }

    private String sanitizeJson(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private Mono<String> callGemini(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        String maskedKey = apiKey != null && apiKey.length() > 8 ? apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4) : "****";
        log.info("Sending request to Gemini API: {}?key={}", apiUrl, maskedKey);

        return webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> {
                    try {
                        log.info("Raw Success Response from Gemini: {}", objectMapper.writeValueAsString(response));
                    } catch (Exception e) {
                        // ignore
                    }
                })
                .doOnError(error -> log.error("GOOGLE API ERROR: {}", error.getMessage()))
                .map(response -> {
                    try {
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        return (String) parts.get(0).get("text");
                    } catch (Exception e) {
                        log.error("Unexpected response structure: {}", response);
                        throw new RuntimeException("Unexpected response structure from Gemini API");
                    }
                });
    }
}
