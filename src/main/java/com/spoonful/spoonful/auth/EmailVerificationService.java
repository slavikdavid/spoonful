package com.spoonful.spoonful.auth;

import com.spoonful.spoonful.mail.EmailService;
import com.spoonful.spoonful.user.User;
import com.spoonful.spoonful.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokens;
    private final UserRepository users;
    private final EmailService mail;

    @Value("${app.verify.base-url:http://localhost:8080/api/auth/verify}")
    private String verifyBaseUrl;

    @Value("${app.verify.ttl:PT24H}") // 24 hours
    private Duration ttl;

    private static final SecureRandom RNG = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    /** create + send link */
    @Transactional
    public void sendVerification(User user) {

        EmailVerificationToken t = new EmailVerificationToken();
        t.setUser(user);
        t.setToken(generateToken());
        t.setExpiresAt(Instant.now().plus(ttl));
        tokens.save(t);

        String link = verifyBaseUrl + "?token=" + t.getToken();
        String html = """
                <p>Hello %s,</p>
                <p>Confirm your email by clicking the link below:</p>
                <p><a href="%s">%s</a></p>
                <p>This link expires in %d hours.</p>
                """.formatted(
                safe(user.getDisplayName()),
                link, link,
                ttl.toHours()
        );

        mail.send(user.getEmail(), "Confirm your email", html);
    }

    @Transactional
    public void verify(String token) {
        EmailVerificationToken t = tokens.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (t.isUsed()) throw new IllegalStateException("Token already used");
        if (t.isExpired()) throw new IllegalStateException("Token expired");

        User u = t.getUser();
        u.setEmailVerified(true);
        users.save(u);

        t.setUsed(true);
        tokens.save(t);
    }

    private static String safe(String s) { return s == null ? "there" : s; }

    private static String generateToken() {
        byte[] buf = new byte[24];
        RNG.nextBytes(buf);
        return HEX.formatHex(buf);
    }

    @Scheduled(cron = "0 0 * * * *") // every hour
    public void cleanup() {
        tokens.deleteByUsedIsTrueOrExpiresAtBefore(Instant.now());
    }
}