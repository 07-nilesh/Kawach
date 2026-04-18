package com.kawach.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ApiController {

    @GetMapping("/health")
    public Mono<String> healthCheck() {
        return Mono.just("Kawach Core Online (M1 Host)");
    }
}
