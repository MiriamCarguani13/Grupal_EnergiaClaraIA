package com.energiaclara.core.domain.challenge;

import com.energiaclara.core.domain.energy.KwhValue;
import com.energiaclara.core.domain.shared.DateRange;
import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.core.domain.shared.FacilityId;
import com.energiaclara.core.domain.shared.TenantId;

import java.time.Instant;
import java.util.Objects;

public class EnergyChallenge {
    private final EnergyChallengeId id;
    private final TenantId tenantId;
    private final FacilityId facilityId;
    private final String title;
    private final KwhTarget target;
    private final DateRange period;
    private ChallengeStatus status;

    private EnergyChallenge(EnergyChallengeId id, TenantId tenantId, FacilityId facilityId, String title, KwhTarget target, DateRange period, ChallengeStatus status) {
        this.id = Objects.requireNonNull(id, "EnergyChallengeId no puede ser nulo");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId no puede ser nulo");
        this.facilityId = Objects.requireNonNull(facilityId, "FacilityId no puede ser nulo");
        this.title = validateTitle(title);
        this.target = Objects.requireNonNull(target, "KwhTarget no puede ser nulo");
        this.period = Objects.requireNonNull(period, "DateRange no puede ser nulo");
        this.status = Objects.requireNonNull(status, "ChallengeStatus no puede ser nulo");
    }

    public static EnergyChallenge launch(TenantId tenantId, FacilityId facilityId, String title, KwhTarget target, DateRange period) {
        return new EnergyChallenge(EnergyChallengeId.generate(), tenantId, facilityId, title, target, period, ChallengeStatus.ACTIVE);
    }

    public void evaluate(KwhValue baselineConsumption, KwhValue actualConsumption, Instant evaluatedAt) {
        if (status != ChallengeStatus.ACTIVE) {
            throw new DomainException("Solo se pueden evaluar retos activos: " + id.value());
        }
        if (!period.contains(evaluatedAt)) {
            this.status = ChallengeStatus.EXPIRED;
            return;
        }
        if (ChallengePolicy.isAchieved(baselineConsumption, actualConsumption, target)) {
            this.status = ChallengeStatus.ACHIEVED;
        }
    }

    private static String validateTitle(String title) {
        Objects.requireNonNull(title, "El título no puede ser nulo");
        String trimmed = title.trim();
        if (trimmed.length() < 5) {
            throw new DomainException("El título del reto debe tener al menos 5 caracteres");
        }
        return trimmed;
    }

    public EnergyChallengeId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public FacilityId getFacilityId() { return facilityId; }
    public String getTitle() { return title; }
    public KwhTarget getTarget() { return target; }
    public DateRange getPeriod() { return period; }
    public ChallengeStatus getStatus() { return status; }
}
