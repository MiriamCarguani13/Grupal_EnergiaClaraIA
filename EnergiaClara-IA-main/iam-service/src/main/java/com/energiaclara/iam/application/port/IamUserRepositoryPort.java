package com.energiaclara.iam.application.port;

import com.energiaclara.core.domain.shared.Email;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.iam.domain.IamUser;

import java.util.Optional;

public interface IamUserRepositoryPort {
    IamUser save(IamUser user);

    Optional<IamUser> findByEmailAndTenantId(Email email, TenantId tenantId);

    boolean existsByEmailAndTenantId(Email email, TenantId tenantId);
}
