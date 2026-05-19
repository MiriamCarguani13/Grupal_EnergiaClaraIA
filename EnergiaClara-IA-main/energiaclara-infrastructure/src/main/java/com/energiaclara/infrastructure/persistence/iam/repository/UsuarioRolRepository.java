package com.energiaclara.infrastructure.persistence.iam.repository;

import com.energiaclara.infrastructure.persistence.iam.entity.UsuarioRolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRolEntity, UUID> {

    List<UsuarioRolEntity> findAllByUsuarioIdAndInquilinoId(UUID usuarioId, UUID inquilinoId);
}
