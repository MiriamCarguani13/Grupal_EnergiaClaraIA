package com.energiaclara.iam.application.port;

import com.energiaclara.iam.domain.AuthenticatedPrincipal;

public interface TokenIssuerPort {
    String issueToken(AuthenticatedPrincipal principal);
}
