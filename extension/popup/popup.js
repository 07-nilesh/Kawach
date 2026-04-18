document.addEventListener('DOMContentLoaded', () => {
    const auditBtn = document.getElementById('audit-btn');
    const scanBtn = document.getElementById('scan-btn');
    const resultsContainer = document.getElementById('results-container');

    // The live IP address of Member 2's Backend
    const BACKEND_URL = "http://10.0.27.93:8080";

    // --- UI RENDERING ENGINE (UPDATED FOR YOUR CUSTOM CSS) ---

    function renderToSResults(threatArray) {
        if (!threatArray || threatArray.length === 0) {
            resultsContainer.innerHTML = `
                <div class="success-box">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                    <span>No significant threats found.</span>
                </div>`;
            return;
        }

        let htmlContent = `
            <div class="results-header">
                <div class="results-header-dot"></div>
                <div class="results-header-title">AI LEGAL AUDIT</div>
            </div>`;

        threatArray.forEach(threat => {
            let severity = (threat.severity || "").toUpperCase();
            let severityClass = severity === "RED" ? "red" : (severity === "YELLOW" ? "yellow" : "green");

            let iconPath = severity === "RED"
                ? `<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>`
                : (severity === "YELLOW"
                    ? `<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>`
                    : `<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>`);

            htmlContent += `
                <div class="threat-card ${severityClass}">
                    <svg class="threat-icon ${severityClass}" fill="none" stroke="currentColor" viewBox="0 0 24 24">${iconPath}</svg>
                    <div>
                        <div class="threat-cat">${threat.point}</div>
                        <div class="threat-explanation">${threat.explanation}</div>
                    </div>
                </div>
            `;
        });
        resultsContainer.innerHTML = htmlContent;
    }

    function renderFraudResults(data) {
        let severityClass = data.scamProbability > 70 ? "danger" : (data.scamProbability > 40 ? "warn" : "safe");

        let htmlContent = `
            <div class="score-ring-wrap">
                <div class="score-label">SCAM PROBABILITY</div>
                <div class="score-circle ${severityClass}">
                    <div class="score-num ${severityClass}">${data.scamProbability}<span class="pct">%</span></div>
                </div>
            </div>
            
            <div class="intel-box">
                <div class="intel-title">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg> 
                    THREAT INTEL
                </div>
                <div class="intel-body">
                    <ul>
                        ${Array.isArray(data.findings) ? data.findings.map(finding => `<li>${finding}</li>`).join('') : `<li>${data.findings || 'No findings reported.'}</li>`}
                    </ul>
                </div>
            </div>
        `;

        if (data.exposedEmails && data.exposedEmails.length > 0) {
            htmlContent += `
                <div class="emails-box">
                    <div class="emails-title">EXPOSED TARGETS (${data.exposedEmails.length})</div>
                    <div class="email-chips">
            `;
            data.exposedEmails.forEach(email => {
                htmlContent += `<div class="email-chip">${email}</div>`;
            });
            htmlContent += `</div></div>`;
        }

        resultsContainer.innerHTML = htmlContent;
    }

    // --- NETWORK LOGIC ---

    async function sendToBackend(endpoint, payload) {
        try {
            // Updated loading state to use your new custom CSS classes
            resultsContainer.innerHTML = `
                <div class="loading-state">
                    <div class="spinner"></div>
                    <div>
                        <div class="loading-text">Transmitting to Secure Server</div>
                        <div class="loading-sub">Awaiting AI Analysis</div>
                    </div>
                </div>`;

            const response = await fetch(`${BACKEND_URL}${endpoint}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!response.ok) throw new Error(`Server status: ${response.status}`);

            const jsonResponse = await response.json();
            console.log("Backend Response:", jsonResponse);

            if (endpoint.includes("audit-tos")) {
                renderToSResults(jsonResponse);
            } else {
                renderFraudResults(jsonResponse);
            }

        } catch (error) {
            console.error("Backend Connection Error:", error);
            resultsContainer.innerHTML = `
                <div class="error-box">
                    <div class="error-title">Connection Failed</div>
                    <div class="error-msg">Cannot reach Member 2's server at ${BACKEND_URL}. Ensure you are on the same Wi-Fi network.</div>
                </div>`;
        }
    }

    // --- EXTRACTOR LOGIC ---

    async function executeScraper(actionType, endpoint) {
        resultsContainer.innerHTML = `
            <div class="loading-state">
                <div class="spinner"></div>
                <div class="loading-text">Extracting webpage data...</div>
            </div>`;

        try {
            const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
            await chrome.scripting.executeScript({ target: { tabId: tab.id }, files: ['scripts/content.js'] });

            chrome.tabs.sendMessage(tab.id, { action: actionType }, async (response) => {
                if (chrome.runtime.lastError) {
                    resultsContainer.innerHTML = `<div class="error-box"><div class="error-title">Error</div><div class="error-msg">Could not read webpage content.</div></div>`;
                    return;
                }

                if (response && response.success) {
                    const payload = actionType === "SCRAPE_TOS" 
                        ? { rawText: response.data } 
                        : { url: response.data.url, detectedEmails: response.data.emails };
                    await sendToBackend(endpoint, payload);
                }
            });
        } catch (error) {
            console.error("Execution error:", error);
            resultsContainer.innerHTML = `<div class="error-box"><div class="error-title">System Error</div><div class="error-msg">An internal extraction error occurred.</div></div>`;
        }
    }

    // --- TRIGGERS (UPDATED TO V1 ENDPOINTS) ---
    auditBtn.addEventListener('click', () => executeScraper("SCRAPE_TOS", "/api/v1/audit-tos"));
    scanBtn.addEventListener('click', () => executeScraper("SCRAPE_FRAUD", "/api/v1/scan-fraud"));
});