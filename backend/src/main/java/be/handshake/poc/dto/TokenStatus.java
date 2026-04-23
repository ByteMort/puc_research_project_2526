package be.handshake.poc.dto;

public record TokenStatus(
        String tokenPreview,
        String contactmomentId,
        long expiresAt,
        boolean used,
        boolean expired
) {}