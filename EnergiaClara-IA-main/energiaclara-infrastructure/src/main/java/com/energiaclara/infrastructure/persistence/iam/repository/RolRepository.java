package com.energiaclara.infrastructure.persistence.iam.repository;

import com.energiaclara.infrastructure.persistence.iam.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RolRepository extends JpaRepository<RolEntity, UUID> {

    Optional<RolEntity> findByNombre(String nombre);
}
