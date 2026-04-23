package be.handshake.poc.service;

import be.handshake.poc.dto.*;
import be.handshake.poc.model.TokenRecord;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private final ConcurrentHashMap<String, TokenRecord> store = new ConcurrentHashMap<>();

    private static final Map<String, String> CONTACTMOMENTEN = Map.of(
            "cm_001", "Hoorcollege – Webbeveiliging",
            "cm_002", "Werkgroep – GDPR Praktijk",
            "cm_003", "Stagegesprek – Mentorgesprek"
    );

    public GenerateResponse generate(String cmId, int ttlSeconds) throws Exception {
        String label = CONTACTMOMENTEN.getOrDefault(cmId, "Onbekend contactmoment");
        String token = UUID.randomUUID().toString();
        Instant exp  = Instant.now().plusSeconds(ttlSeconds);

        store.put(token, new TokenRecord(token, cmId, label, exp));

        String qrBase64 = buildQrBase64(token);
        String preview  = token.substring(0, 8) + "...";

        return new GenerateResponse(token, preview, cmId, label, exp.getEpochSecond(), ttlSeconds, qrBase64);
    }

    public ScanResponse scan(String token) {
        TokenRecord record = store.get(token);

        if (record == null)        return ScanResponse.error("Token niet gevonden in database");
        if (record.isExpired())    return ScanResponse.error("Token verlopen (exp < now)");
        if (record.isUsed())       return ScanResponse.error("Token al gebruikt — replay-aanval geblokkeerd");

        record.setUsed(true);
        return ScanResponse.ok(record.getContactmomentId(), record.getContactmomentLabel());
    }

    public List<TokenStatus> getAllStatuses() {
        return store.values().stream()
                .sorted(Comparator.comparing(TokenRecord::getExpiresAt).reversed())
                .map(r -> new TokenStatus(
                        r.getToken().substring(0, 8) + "...",
                        r.getContactmomentId(),
                        r.getExpiresAt().getEpochSecond(),
                        r.isUsed(),
                        r.isExpired()
                ))
                .collect(Collectors.toList());
    }

    // Verwijder verlopen tokens elk uur (GDPR – dataminimalisatie)
    @Scheduled(fixedRate = 3_600_000)
    public void purgeExpiredTokens() {
        store.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    private String buildQrBase64(String content) throws Exception {
        var matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 256, 256);
        var out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }
}