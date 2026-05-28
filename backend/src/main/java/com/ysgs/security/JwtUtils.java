package com.ysgs.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateJwtToken(UUID userId, String role, Integer tenantId, List<String> permissions) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role)
                .claim("tenantId", tenantId)
                .claim("permissions", permissions)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserUuidFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public Integer getTenantIdFromToken(String token) {
        return (Integer) Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().get("tenantId");
    }

    public List<String> getPermissionsFromToken(String token) {
        return (List<String>) Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().get("permissions");
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }
}