package com.energiaclara.infrastructure.consumption.persistence.repository;

import com.energiaclara.infrastructure.consumption.persistence.entity.LecturaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * SKELETON. JPA repository para {@code consumo.lectura}.
 * TODO equipo Consumption: añadir métodos query según necesidades.
 */
public interface LecturaRepository extends JpaRepository<LecturaEntity, UUID> {

    List<LecturaEntity> findAllByInquilinoIdAndMedidorIdAndPeriodoFinBetween(
            UUID inquilinoId, UUID medidorId, Instant from, Instant to);

    // Historial reciente por medidor (más nuevas primero) — alimenta el baseline dinámico del motor AI.
    List<LecturaEntity> findByInquilinoIdAndMedidorIdOrderByPeriodoFinDesc(
            UUID inquilinoId, UUID medidorId, Pageable pageable);

    // TODO: findByInquilinoIdAndMedidorIdAndPeriodoInicio para enforcing invariante (única lectura por periodo)
}
