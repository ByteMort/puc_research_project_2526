package be.handshake.poc.model;

import lombok.Data;
import java.time.Instant;

@Data
public class TokenRecord {
    private final String token;
    private final String contactmomentId;
    private final String contactmomentLabel;
    private final Instant expiresAt;
    private volatile boolean used = false;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public long secondsRemaining() {
        long diff = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, diff);
    }
}