package com.example.RateLimiting.controller;

import com.example.RateLimiting.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class StatusController {

    @Autowired
    private RateLimiterService rateLimiterService;

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String,Object>>> health(){

        return Mono.just(ResponseEntity.ok(Map.of(
                "status","UP",
                "service","rate-limiting-gateway"
        )));
    }

    //check Rate limit status for the client
    @GetMapping("/rate-limit/status")
    public Mono<ResponseEntity<Map<String, Object>>> getRateLimitStatus(ServerWebExchange exchange){
        String clientId = getClientId(exchange);
        return Mono.just(ResponseEntity.ok(Map.of(
                "status","UP",
                "service","rate-limiting-gateway",
                "clientId",clientId,
                "capacity",rateLimiterService.getCapacity(clientId),
                "availableTokens",rateLimiterService.getAvailableTokens(clientId)
        )));
    }

    //extracts client Id from request
    private String getClientId(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        var remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return "unknown";
    }



}
