package com.energiaclara.iam.application.dto;

import com.energiaclara.core.domain.shared.Email;
import com.energiaclara.core.domain.shared.TenantId;

public record LoginCommand(Email email, String rawPassword, TenantId tenantId) {
}
