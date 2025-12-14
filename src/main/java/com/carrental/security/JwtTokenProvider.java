// ==== JWT Token Provider ====
package com.carrental.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${spring.security.jwt.refresh-expiration}")
    private long refreshExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate JWT access token
     */
    public String generateAccessToken(Long userId, String email, List<String> roles) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpiration);

            return Jwts.builder()
                    .setSubject(String.valueOf(userId))
                    .claim("email", email)
                    .claim("roles", roles)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex);
            throw ex;
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex);
            throw ex;
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex);
            throw ex;
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex);
            throw ex;
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex);
            throw ex;
        }
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(Long userId) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + refreshExpiration);

            return Jwts.builder()
                    .setSubject(String.valueOf(userId))
                    .claim("type", "refresh")
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception ex) {
            logger.error("Error generating refresh token: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Get user ID from token
     */
    public Long getUserIdFromToken(String token) {
        try {
            return Long.parseLong(Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject());
        } catch (JwtException | NumberFormatException ex) {
            logger.error("Error getting user ID from token: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Get email from token
     */
    public String getEmailFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("email", String.class);
        } catch (JwtException ex) {
            logger.error("Error getting email from token: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Get roles from token
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("roles", List.class);
        } catch (JwtException ex) {
            logger.error("Error getting roles from token: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex);
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex);
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex);
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex);
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex);
        }
        return false;
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
        } catch (JwtException ex) {
            logger.error("Error getting expiration from token: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception ex) {
            logger.error("Error checking token expiration: {}", ex.getMessage());
            return true;
        }
    }
}
