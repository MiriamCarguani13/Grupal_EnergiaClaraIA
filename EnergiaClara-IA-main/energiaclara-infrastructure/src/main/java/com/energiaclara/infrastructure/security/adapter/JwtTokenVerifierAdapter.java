package com.energiaclara.infrastructure.security.adapter;

import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.core.domain.shared.Email;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.core.domain.shared.UserId;
import com.energiaclara.iam.application.port.TokenVerifierPort;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;
import com.energiaclara.iam.domain.Role;
import com.energiaclara.infrastructure.config.security.TokenProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenVerifierAdapter implements TokenVerifierPort {

    private final SecretKey key;
    private final String expectedIssuer;

    public JwtTokenVerifierAdapter(SecretKey jwtSecretKey, TokenProperties props) {
        this.key = jwtSecretKey;
        this.expectedIssuer = props.issuer();
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthenticatedPrincipal verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(expectedIssuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            UUID tenantId = UUID.fromString(claims.get("tenant_id", String.class));
            String email = claims.get("email", String.class);
            List<String> rolesRaw = (List<String>) claims.get("roles", List.class);
            Set<Role> roles = rolesRaw == null
                    ? Set.of()
                    : rolesRaw.stream().map(Role::valueOf).collect(Collectors.toSet());

            return new AuthenticatedPrincipal(
                    UserId.of(userId),
                    TenantId.of(tenantId),
                    new Email(email),
                    roles
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw new DomainException("Token inválido o expirado");
        }
    }
}
