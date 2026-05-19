package com.energiaclara.infrastructure.security.adapter;

import com.energiaclara.iam.application.port.TokenIssuerPort;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;
import com.energiaclara.iam.domain.Role;
import com.energiaclara.infrastructure.config.security.TokenProperties;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenIssuerAdapter implements TokenIssuerPort {

    private final SecretKey key;
    private final TokenProperties props;

    public JwtTokenIssuerAdapter(SecretKey jwtSecretKey, TokenProperties props) {
        this.key = jwtSecretKey;
        this.props = props;
    }

    @Override
    public String issueToken(AuthenticatedPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(props.accessTokenMinutes() * 60L);
        List<String> roles = principal.roles().stream().map(Role::name).toList();

        return Jwts.builder()
                .issuer(props.issuer())
                .subject(principal.userId().value().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .claim("tenant_id", principal.tenantId().value().toString())
                .claim("email", principal.email().value())
                .claim("roles", roles)
                .claim("token_type", "access")
                .signWith(key)
                .compact();
    }

    public long getAccessTokenSeconds() {
        return props.accessTokenMinutes() * 60L;
    }
}
