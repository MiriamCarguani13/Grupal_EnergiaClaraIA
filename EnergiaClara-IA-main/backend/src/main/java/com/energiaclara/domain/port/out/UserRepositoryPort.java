package com.energiaclara.domain.port.out;

import com.energiaclara.domain.model.User;
import com.energiaclara.domain.model.vo.Email;
import com.energiaclara.domain.model.vo.TenantId;
import com.energiaclara.domain.model.vo.UserId;

import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user, UserId assignedBy);
    Optional<User> findByEmailAndTenantId(Email email, TenantId tenantId);
    boolean existsByEmailAndTenantId(Email email, TenantId tenantId);
}
