package com.smartwallet.security;

import com.smartwallet.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        if (!jwtProperties.algorithm().equalsIgnoreCase("HS256")) {
            throw new IllegalStateException("Only HS256 JWT is supported");
        }
        this.signingKey = Keys.hmacShaKeyFor(normalizeSecret(jwtProperties.secret()));
    }

    private static byte[] normalizeSecret(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String createAccessToken(String phone) {
        long minutes = jwtProperties.accessTokenExpireMinutes();
        Date now = new Date();
        Date exp = new Date(now.getTime() + minutes * 60_000);

        return Jwts.builder()
                .subject(phone)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey)
                .compact();
    }

    public String parseSubject(String jwt) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
        return claims.getSubject();
    }
}
