package com.kawach.model;

import java.util.List;

public record AuditResponse(
        String verdict,
        String verdictReason,
        List<String> summary,
        List<ThreatResponse> flags
) {
}
