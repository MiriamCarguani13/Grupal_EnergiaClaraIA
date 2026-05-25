package com.energiaclara.infrastructure.persistence.adapter;

import com.energiaclara.domain.model.Role;
import com.energiaclara.domain.model.User;
import com.energiaclara.domain.model.vo.Email;
import com.energiaclara.domain.model.vo.TenantId;
import com.energiaclara.domain.model.vo.UserId;
import com.energiaclara.application.port.out.UserRepositoryPort;
import com.energiaclara.infrastructure.persistence.entity.RoleEntity;
import com.energiaclara.infrastructure.persistence.entity.UserEntity;
import com.energiaclara.infrastructure.persistence.entity.UserRoleEntity;
import com.energiaclara.infrastructure.persistence.repository.RoleJpaRepository;
import com.energiaclara.infrastructure.persistence.repository.UserJpaRepository;
import com.energiaclara.infrastructure.persistence.repository.UserRoleJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

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
    public User save(User user, UserId assignedBy) {
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
            assignment.setAssignedBy(assignedBy.value());
            userRoleRepository.save(assignment);
        }
        return user;
    }

    @Override
    public Optional<User> findByEmailAndTenantId(Email email, TenantId tenantId) {
        return userRepository.findByEmailAndTenantId(email.value(), tenantId.value())
                .map(this::toDomain);
    }

    @Override
    public boolean existsByEmailAndTenantId(Email email, TenantId tenantId) {
        return userRepository.existsByEmailAndTenantId(email.value(), tenantId.value());
    }

    private UserEntity toEntity(User user) {
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

    private User toDomain(UserEntity entity) {
        Set<Role> roles = loadRoles(entity.getId());
        return new User(
                UserId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                Email.of(entity.getEmail()),
                entity.getFullName(),
                entity.getHashedPassword(),
                roles,
                entity.isActive(),
                entity.getCreatedAt()
        );
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
