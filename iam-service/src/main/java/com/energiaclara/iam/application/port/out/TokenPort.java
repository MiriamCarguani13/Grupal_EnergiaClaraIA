package com.energiaclara.iam.application.port.out;

import com.energiaclara.iam.domain.model.User;

public interface TokenPort {
    String generateToken(User user);
    boolean validateToken(String token);
    String extractEmail(String token);
    String extractTenantId(String token);
}
