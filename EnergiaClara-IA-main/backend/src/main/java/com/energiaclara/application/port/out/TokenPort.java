package com.energiaclara.application.port.out;

import com.energiaclara.domain.model.User;

public interface TokenPort {
    String generateToken(User user);
    boolean validateToken(String token);
    String extractEmail(String token);
    String extractTenantId(String token);
}
