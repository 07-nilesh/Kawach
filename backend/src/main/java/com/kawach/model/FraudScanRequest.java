package com.kawach.model;

import java.util.List;

public record FraudScanRequest(String url, List<String> detectedEmails) {
}
