package com.energiaclara.iam.application.port.out;

import com.energiaclara.core.model.vo.TenantId;
import com.energiaclara.core.model.vo.UserId;
import com.energiaclara.iam.domain.model.User;
import com.energiaclara.iam.domain.model.vo.Email;

import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user, UserId assignedBy);

    Optional<User> findByEmailAndTenantId(Email email, TenantId tenantId);

    boolean existsByEmailAndTenantId(Email email, TenantId tenantId);
}
