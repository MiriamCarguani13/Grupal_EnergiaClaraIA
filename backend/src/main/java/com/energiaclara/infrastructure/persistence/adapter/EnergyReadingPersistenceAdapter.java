package com.energiaclara.infrastructure.persistence.adapter;

import com.energiaclara.application.port.out.LoadKpiSnapshotsPort;
import com.energiaclara.application.energyops.dto.EnergyReadingRecord;
import com.energiaclara.application.port.out.LoadEnergyReadingsPort;
import com.energiaclara.application.port.out.SaveEnergyReadingPort;
import com.energiaclara.infrastructure.persistence.entity.EnergyReadingEntity;
import com.energiaclara.infrastructure.persistence.repository.EnergyReadingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EnergyReadingPersistenceAdapter implements SaveEnergyReadingPort, LoadKpiSnapshotsPort, LoadEnergyReadingsPort {

    private final EnergyReadingRepository readingRepository;

    public EnergyReadingPersistenceAdapter(EnergyReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    @Override
    public EnergyReadingRecord save(EnergyReadingRecord reading) {
        return toRecord(readingRepository.save(toEntity(reading)));
    }

    @Override
    public List<EnergyReadingRecord> loadRecentReadings() {
        return readingRepository.findTop20ByOrderByMeasuredAtDesc().stream()
                .map(this::toRecord)
                .toList();
    }

    @Override
    public List<EnergyReadingRecord> loadRecentEnergyReadings() {
        return readingRepository.findTop50ByOrderByMeasuredAtDesc().stream()
                .map(this::toRecord)
                .toList();
    }

    private EnergyReadingEntity toEntity(EnergyReadingRecord record) {
        EnergyReadingEntity entity = new EnergyReadingEntity();
        entity.setId(record.id());
        entity.setTenantId(record.tenantId());
        entity.setMedidorId(record.medidorId());
        entity.setRegistradaPor(record.registradaPor());
        entity.setFacilityId(record.facilityId());
        entity.setMeterId(record.meterId());
        entity.setMeasuredAt(record.measuredAt());
        entity.setPeriodoInicio(record.periodoInicio());
        entity.setKwh(record.kwh());
        entity.setVoltage(record.voltage());
        entity.setPowerFactor(record.powerFactor());
        return entity;
    }

    private EnergyReadingRecord toRecord(EnergyReadingEntity entity) {
        return new EnergyReadingRecord(
                entity.getId(),
                entity.getTenantId(),
                entity.getMedidorId(),
                entity.getRegistradaPor(),
                entity.getFacilityId(),
                entity.getMeterId(),
                entity.getMeasuredAt(),
                entity.getPeriodoInicio(),
                entity.getKwh(),
                entity.getVoltage(),
                entity.getPowerFactor()
        );
    }
}
