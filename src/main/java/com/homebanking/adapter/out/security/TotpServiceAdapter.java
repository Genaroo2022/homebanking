package com.homebanking.adapter.out.security;

import com.homebanking.port.out.auth.TotpService;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

@Component
public class TotpServiceAdapter implements TotpService {

    private static final String HMAC_ALGO = "HmacSHA1";
    private static final int SECRET_BYTES = 20;

    private final SecureRandom random = new SecureRandom();
    private final Base32 base32 = new Base32();
    private final int timeStepSeconds;
    private final int digits;
    private final int window;

    public TotpServiceAdapter(
            @Value("${totp.time-step:30}") int timeStepSeconds,
            @Value("${totp.digits:6}") int digits,
            @Value("${totp.window:1}") int window) {
        this.timeStepSeconds = timeStepSeconds;
        this.digits = digits;
        this.window = window;
    }

    @Override
    public String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        random.nextBytes(bytes);
        return base32.encodeToString(bytes).replace("=", "");
    }

    @Override
    public String buildProvisioningUri(String issuer, String accountName, String secret) {
        String encodedIssuer = urlEncode(issuer);
        String encodedAccount = urlEncode(accountName);
        return "otpauth://totp/" + encodedIssuer + ":" + encodedAccount
                + "?secret=" + secret
                + "&issuer=" + encodedIssuer
                + "&digits=" + digits
                + "&period=" + timeStepSeconds;
    }

    @Override
    public boolean verifyCode(String secret, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        long timeStep = Instant.now().getEpochSecond() / timeStepSeconds;
        for (int i = -window; i <= window; i++) {
            String candidate = generateCode(secret, timeStep + i);
            if (candidate.equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String generateCode(String secret, long timeStep) {
        try {
            byte[] key = base32.decode(secret);
            byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(key, HMAC_ALGO));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary =
                    ((hash[offset] & 0x7f) << 24) |
                            ((hash[offset + 1] & 0xff) << 16) |
                            ((hash[offset + 2] & 0xff) << 8) |
                            (hash[offset + 3] & 0xff);
            int otp = binary % (int) Math.pow(10, digits);
            return String.format("%0" + digits + "d", otp);
        } catch (Exception ex) {
            return "";
        }
    }

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
