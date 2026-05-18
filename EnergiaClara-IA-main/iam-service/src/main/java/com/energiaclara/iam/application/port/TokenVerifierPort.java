package com.energiaclara.iam.application.port;

import com.energiaclara.iam.domain.AuthenticatedPrincipal;

public interface TokenVerifierPort {
    AuthenticatedPrincipal verify(String token);
}
