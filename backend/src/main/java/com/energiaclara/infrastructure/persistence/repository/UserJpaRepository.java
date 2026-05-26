package com.energiaclara.infrastructure.persistence.repository;

import com.energiaclara.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailAndTenantId(String email, UUID tenantId);
    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT u.* FROM iam.usuario u " +
            "JOIN iam.usuario_rol ur ON u.usuario_id = ur.usuario_id " +
            "JOIN iam.rol r ON ur.rol_id = r.rol_id " +
            "WHERE r.nombre = :roleName AND u.inquilino_id = :tenantId", nativeQuery = true)
    java.util.List<UserEntity> findByRoleNameAndTenantId(@org.springframework.data.repository.query.Param("roleName") String roleName, @org.springframework.data.repository.query.Param("tenantId") UUID tenantId);
}
