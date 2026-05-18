package com.energiaclara.infrastructure.persistence;

import com.energiaclara.core.domain.shared.Email;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.iam.application.port.IamUserRepositoryPort;
import com.energiaclara.iam.domain.IamUser;
import com.energiaclara.iam.domain.Role;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements IamUserRepositoryPort {

    private final UserJpaRepository userRepository;
    private final RoleJpaRepository roleRepository;
    private final UserRoleJpaRepository userRoleRepository;

    public UserRepositoryAdapter(UserJpaRepository userRepository,
                                 RoleJpaRepository roleRepository,
                                 UserRoleJpaRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public IamUser save(IamUser user) {
        UserEntity entity = toEntity(user);
        userRepository.save(entity);

        Instant now = Instant.now();
        for (Role role : user.getRoles()) {
            RoleEntity roleEntity = roleRepository.findByName(role.name())
                    .orElseThrow(() -> new IllegalStateException(
                            "Rol '" + role.name() + "' no existe en [iam].[rol]. Pedir a DBA seedear catálogo."));

            UserRoleEntity assignment = new UserRoleEntity();
            assignment.setId(UUID.randomUUID());
            assignment.setUserId(user.getId().value());
            assignment.setRoleId(roleEntity.getId());
            assignment.setTenantId(user.getTenantId().value());
            assignment.setBuildingId(null);
            assignment.setAssignedAt(now);
            assignment.setAssignedBy(user.getId().value());
            userRoleRepository.save(assignment);
        }
        return user;
    }

    @Override
    public Optional<IamUser> findByEmailAndTenantId(Email email, TenantId tenantId) {
        return userRepository.findByEmailAndTenantId(email.value(), tenantId.value())
                .map(this::toDomain);
    }

    @Override
    public boolean existsByEmailAndTenantId(Email email, TenantId tenantId) {
        return userRepository.existsByEmailAndTenantId(email.value(), tenantId.value());
    }

    private UserEntity toEntity(IamUser user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId().value());
        entity.setTenantId(user.getTenantId().value());
        entity.setEmail(user.getEmail().value());
        entity.setFullName(user.getFullName());
        entity.setHashedPassword(user.getHashedPassword());
        entity.setActive(user.isActive());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getCreatedAt());
        return entity;
    }

    private IamUser toDomain(UserEntity entity) {
        Set<Role> roles = loadRoles(entity.getId());
        IamUser user = IamUser.register(TenantId.of(entity.getTenantId()), new Email(entity.getEmail()), entity.getFullName(), entity.getHashedPassword(), roles);
        if (!entity.isActive()) {
            user.deactivate();
        }
        return user;
    }

    private Set<Role> loadRoles(UUID userId) {
        List<UserRoleEntity> assignments = userRoleRepository.findByUserId(userId);
        Set<Role> roles = new HashSet<>();
        for (UserRoleEntity assignment : assignments) {
            roleRepository.findById(assignment.getRoleId())
                    .ifPresent(re -> {
                        try {
                            roles.add(Role.valueOf(re.getName()));
                        } catch (IllegalArgumentException ignored) {
                            // role en DB no mapea al enum del backend → omitir
                        }
                    });
        }
        return roles;
    }
}
