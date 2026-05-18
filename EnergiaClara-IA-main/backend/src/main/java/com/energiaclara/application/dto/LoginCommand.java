package com.energiaclara.application.dto;

import com.energiaclara.domain.model.vo.Email;
import com.energiaclara.domain.model.vo.TenantId;

public record LoginCommand(Email email, String password, TenantId tenantId) {}
