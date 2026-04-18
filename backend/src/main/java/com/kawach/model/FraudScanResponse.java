package com.kawach.model;

import java.util.List;

public record FraudScanResponse(Integer scamProbability, String riskLevel, List<String> findings) {
}
