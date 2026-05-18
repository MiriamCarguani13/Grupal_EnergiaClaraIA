package com.energiaclara.infrastructure.security;

import com.energiaclara.core.domain.shared.Email;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.core.domain.shared.UserId;
import com.energiaclara.iam.application.port.TokenIssuerPort;
import com.energiaclara.iam.application.port.TokenVerifierPort;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;
import com.energiaclara.iam.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenAdapter implements TokenIssuerPort, TokenVerifierPort {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenAdapter(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String issueToken(AuthenticatedPrincipal principal) {
        String roles = principal.roles().stream()
                .map(Role::name)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(principal.email().value())
                .claim("userId", principal.userId().toString())
                .claim("tenantId", principal.tenantId().toString())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public AuthenticatedPrincipal verify(String token) {
        Claims claims = getClaims(token);
        Set<Role> roles = extractRoles(token).stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
        return new AuthenticatedPrincipal(
                UserId.of(UUID.fromString(claims.get("userId", String.class))),
                TenantId.of(UUID.fromString(claims.get("tenantId", String.class))),
                new Email(claims.getSubject()),
                roles
        );
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extractTenantId(String token) {
        return getClaims(token).get("tenantId", String.class);
    }

    public String extractUserId(String token) {
        return getClaims(token).get("userId", String.class);
    }

    public Set<String> extractRoles(String token) {
        String roles = getClaims(token).get("roles", String.class);
        if (roles == null || roles.isBlank()) {
            return Set.of();
        }
        return Set.of(roles.split(","));
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
