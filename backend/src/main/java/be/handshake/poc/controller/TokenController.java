package be.handshake.poc.controller;

import be.handshake.poc.dto.*;
import be.handshake.poc.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> generate(@RequestBody GenerateRequest req) {
        try {
            int ttl = req.ttlSeconds() > 0 ? req.ttlSeconds() : 300;
            return ResponseEntity.ok(tokenService.generate(req.contactmomentId(), ttl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> scan(@RequestBody ScanRequest req) {
        if (req.token() == null || req.token().isBlank()) {
            return ResponseEntity.badRequest().body(ScanResponse.error("Geen token meegegeven"));
        }
        return ResponseEntity.ok(tokenService.scan(req.token()));
    }

    @GetMapping("/status")
    public ResponseEntity<List<TokenStatus>> status() {
        return ResponseEntity.ok(tokenService.getAllStatuses());
    }
}