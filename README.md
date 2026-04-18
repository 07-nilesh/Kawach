# 🛡️ Kawach

**A dual-threat AI Chrome Extension acting as an active digital shield against predatory Terms of Service and phishing fraud.**

---

## The Problem
Navigating the modern web exposes users to two silent threats: predatory Terms of Service (ToS) agreements that strip away digital rights, and sophisticated phishing pages designed to harvest credentials. Users lack the time to read 50-page legal documents and the technical expertise to identify well-disguised malicious sites.

## The Solution: Kawach
Kawach bridges the gap between complex legal jargon, hidden digital threats, and the end user. Powered by the Google Gemini API, this lightweight Chrome Extension actively monitors your browsing environment and neutralizes threats before they compromise your data.

### Core Capabilities
* **ToS Legal Auditor:** Scrapes the active DOM for Terms of Service text, feeds it through a highly constrained Gemini AI prompt, and generates an immediate Red/Yellow/Green threat dashboard outlining dangerous clauses.
* **Fraud Shield:** Extracts page URLs and DOM-embedded email addresses, passing them through our risk-analysis engine to generate a real-time Scam Probability Score.

---

## 🏗️ Technical Architecture

Kawach is built strictly for high concurrency, zero-latency processing, and secure browser integration. 

* **Frontend (The Shield):** Chrome Extension Manifest V3, Vanilla JavaScript, HTML5, Tailwind CSS. Built entirely without heavy frameworks to ensure lightning-fast DOM injection and extraction.
* **Backend (The Core):** Java Spring Boot REST API. Engineered for high-throughput payload routing, acting as a distributed compute node to handle rapid-fire extension requests without bottlenecking.
* **AI Orchestration:** Google Gemini API. Accessed via strict server-side system prompts that force the LLM to return heavily validated, deterministic JSON arrays instead of conversational text.

---

## 🗂️ Project Structure

```text
kawach/
│
├── README.md                  <-- Project documentation & setup instructions
├── .gitignore                 <-- Standard ignores (node_modules, target/, .env)
├── presentation/              <-- Pitch deck and backup demo video
│
├── extension/                 <-- [FRONTEND TERRITORY]
│   ├── manifest.json          <-- The V3 configuration and permissions
│   ├── popup/
│   │   ├── index.html         <-- Tailwind-styled UI dashboard
│   │   ├── popup.js           <-- Event listeners and API fetch logic
│   │   └── style.css          <-- Minimal custom CSS
│   ├── scripts/
│   │   ├── background.js      <-- Service worker for extension lifecycle
│   │   └── content.js         <-- DOM Scraper (pulls ToS text and Regex emails)
│   └── assets/
│       ├── icon-16.png        <-- Extension branding
│       ├── icon-48.png
│       └── icon-128.png
│
└── backend/                   <-- [BACKEND TERRITORY]
    ├── pom.xml                <-- Maven dependencies (Spring Boot, REST, JSON)
    └── src/
        └── main/
            ├── java/com/kawach/
            │   ├── KawachApplication.java    <-- App Entry Point
            │   ├── controller/
            │   │   └── ApiController.java    <-- Endpoints: /api/audit-tos, /api/scan-fraud
            │   ├── service/
            │   │   └── GeminiService.java    <-- Handles prompts and API calls
            │   └── model/
            │       ├── AuditRequest.java     <-- Incoming scraped text payload
            │       └── ThreatResponse.java   <-- Outgoing JSON array (Red/Yellow/Green)
            └── resources/
                └── application.properties    <-- CORS config, server port, Gemini API keys