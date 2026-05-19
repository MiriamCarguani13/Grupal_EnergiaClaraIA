package com.energiaclara.infrastructure.persistence.iam.repository;

import com.energiaclara.infrastructure.persistence.iam.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByCorreoAndInquilinoId(String correo, UUID inquilinoId);

    boolean existsByCorreoAndInquilinoId(String correo, UUID inquilinoId);
}
