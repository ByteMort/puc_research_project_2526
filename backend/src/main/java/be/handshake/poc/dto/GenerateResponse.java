package be.handshake.poc.dto;

public record GenerateResponse(
        String token,             // volledig token — enkel voor lokale PoC demo
        String tokenPreview,
        String contactmomentId,
        String contactmomentLabel,
        long expiresAt,
        int ttlSeconds,
        String qrCodeBase64
) {}