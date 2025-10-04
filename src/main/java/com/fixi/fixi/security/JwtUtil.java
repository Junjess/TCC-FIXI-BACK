package com.fixi.fixi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "umaChaveSecretaBemGrandeParaAssinarTokens123";
    private static final long EXPIRATION = 1000 * 60 * 60; // 1 hora

    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Gerar token
    public static String generateToken(String subject, String role) {
        return Jwts.builder()
                .setSubject(subject) // geralmente o id do usuário
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // Validar e extrair claims
    public static Jws<Claims> validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token);
    }

    // Pegar role
    public static String getRole(String token) {
        return validateToken(token).getBody().get("role", String.class);
    }
}
