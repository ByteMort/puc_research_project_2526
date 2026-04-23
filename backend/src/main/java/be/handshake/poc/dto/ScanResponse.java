package be.handshake.poc.dto;

public record ScanResponse(
        boolean success,
        String contactmomentId,
        String contactmomentLabel,
        String error
        // Bewust GEEN naam, email of user_id — GDPR
) {
    public static ScanResponse ok(String cmId, String label) {
        return new ScanResponse(true, cmId, label, null);
    }

    public static ScanResponse error(String reason) {
        return new ScanResponse(false, null, null, reason);
    }
}