package com.spoonful.spoonful.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Service
public class JwtService {

    private final Key key;
    private final long ttlMillis;

    public JwtService(
            @Value("${app.jwt.secret:super-secret-key-change-me-32-bytes-min}") String secret,
            @Value("${app.jwt.ttl:PT168H}") Duration ttl // 168h = 7 days
    ) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 bytes for HS256");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.ttlMillis = ttl.toMillis();
    }

    public String issueToken(Long userId, String email){
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long parseUserId(String token){
        try {
            String sub = parseClaims(token).getSubject();
            return (sub != null) ? Long.valueOf(sub) : null;
        } catch (RuntimeException e) { // includes JwtException, NumberFormatException
            return null;
        }
    }

    public String getEmail(String token){
        try {
            Object v = parseClaims(token).get("email");
            return v != null ? v.toString() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public boolean isValid(String token){
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
