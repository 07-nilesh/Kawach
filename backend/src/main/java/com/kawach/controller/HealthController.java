package com.kawach.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public Mono<Map<String, String>> healthCheck() {
        return Mono.just(Map.of(
            "status", "shield-active",
            "host", "M1-Silicon"
        ));
    }
}
