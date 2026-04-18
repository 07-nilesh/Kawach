package com.kawach.controller;

import com.kawach.model.AuditRequest;
import com.kawach.model.AuditResponse;
import com.kawach.model.FraudScanRequest;
import com.kawach.model.FraudScanResponse;
import com.kawach.model.ThreatResponse;
import com.kawach.service.GeminiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final GeminiService geminiService;

    public ApiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/audit-tos")
    public Mono<AuditResponse> auditTos(@RequestBody AuditRequest request) {
        return geminiService.analyzeToS(request.rawText());
    }

    @PostMapping("/scan-fraud")
    public Mono<FraudScanResponse> scanFraud(@RequestBody FraudScanRequest request) {
        return geminiService.scanFraud(request);
    }
}
