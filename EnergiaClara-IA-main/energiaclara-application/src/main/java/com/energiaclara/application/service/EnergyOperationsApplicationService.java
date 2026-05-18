package com.energiaclara.application.service;

import com.energiaclara.application.port.in.CreateMaintenanceTicketUseCase;
import com.energiaclara.application.port.in.DetectAnomalyUseCase;
import com.energiaclara.application.port.in.EvaluateChallengeUseCase;
import com.energiaclara.application.port.in.RegisterEnergyReadingUseCase;
import com.energiaclara.application.port.out.DomainEventPublisherPort;
import com.energiaclara.application.port.out.EnergyAnomalyRepositoryPort;
import com.energiaclara.application.port.out.EnergyBaselineProviderPort;
import com.energiaclara.application.port.out.EnergyChallengeRepositoryPort;
import com.energiaclara.application.port.out.EnergyReadingRepositoryPort;
import com.energiaclara.application.port.out.MaintenanceTicketRepositoryPort;
import com.energiaclara.application.usecase.CreateMaintenanceTicketCommand;
import com.energiaclara.application.usecase.DetectAnomalyCommand;
import com.energiaclara.application.usecase.EvaluateChallengeCommand;
import com.energiaclara.application.usecase.RegisterEnergyReadingCommand;
import com.energiaclara.core.domain.challenge.ChallengeStatus;
import com.energiaclara.core.domain.energy.AnomalyId;
import com.energiaclara.core.domain.energy.AnomalyPolicy;
import com.energiaclara.core.domain.energy.AnomalySeverity;
import com.energiaclara.core.domain.energy.EnergyAnomaly;
import com.energiaclara.core.domain.energy.EnergyBaseline;
import com.energiaclara.core.domain.energy.EnergyReading;
import com.energiaclara.core.domain.energy.EnergyReadingId;
import com.energiaclara.core.domain.energy.KwhValue;
import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.core.domain.ticket.MaintenanceTicket;
import com.energiaclara.core.domain.ticket.MaintenanceTicketId;
import com.energiaclara.core.domain.ticket.TicketPriority;
import com.energiaclara.core.domain.ticket.TicketPriorityPolicy;

import java.util.Optional;

public class EnergyOperationsApplicationService implements RegisterEnergyReadingUseCase, DetectAnomalyUseCase, CreateMaintenanceTicketUseCase, EvaluateChallengeUseCase {
    private final EnergyReadingRepositoryPort readingRepository;
    private final EnergyAnomalyRepositoryPort anomalyRepository;
    private final MaintenanceTicketRepositoryPort ticketRepository;
    private final EnergyChallengeRepositoryPort challengeRepository;
    private final EnergyBaselineProviderPort baselineProvider;
    private final DomainEventPublisherPort eventPublisher;

    public EnergyOperationsApplicationService(EnergyReadingRepositoryPort readingRepository, EnergyAnomalyRepositoryPort anomalyRepository, MaintenanceTicketRepositoryPort ticketRepository, EnergyChallengeRepositoryPort challengeRepository, EnergyBaselineProviderPort baselineProvider, DomainEventPublisherPort eventPublisher) {
        this.readingRepository = readingRepository;
        this.anomalyRepository = anomalyRepository;
        this.ticketRepository = ticketRepository;
        this.challengeRepository = challengeRepository;
        this.baselineProvider = baselineProvider;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EnergyReadingId register(RegisterEnergyReadingCommand command) {
        EnergyReading reading = EnergyReading.register(command.actor().tenantId(), command.facilityId(), command.kwhValue(), command.timestamp());
        readingRepository.save(reading);
        eventPublisher.publish(reading.pullDomainEvents());
        return reading.getId();
    }

    @Override
    public Optional<AnomalyId> detect(DetectAnomalyCommand command) {
        EnergyReading reading = readingRepository.findById(command.readingId())
                .orElseThrow(() -> new DomainException("Lectura energética no encontrada: " + command.readingId().value()));
        EnergyBaseline baseline = baselineProvider.currentBaselineFor(command.actor().tenantId());
        if (!AnomalyPolicy.isAnomaly(reading, baseline)) {
            return Optional.empty();
        }
        KwhValue expected = baseline.getExpectedFor(reading.getFacilityId(), reading.getTimestamp());
        KwhValue delta = reading.getKwhValue().subtract(expected);
        AnomalySeverity severity = AnomalyPolicy.calculateSeverity(delta, baseline);
        EnergyAnomaly anomaly = EnergyAnomaly.detect(reading.getId(), command.actor().tenantId(), command.type(), severity, delta);
        anomalyRepository.save(anomaly);
        eventPublisher.publish(anomaly.pullDomainEvents());
        return Optional.of(anomaly.getId());
    }

    @Override
    public MaintenanceTicketId create(CreateMaintenanceTicketCommand command) {
        TicketPriority priority = TicketPriorityPolicy.calculate(command.severity(), command.estimatedWaste());
        MaintenanceTicket ticket = MaintenanceTicket.openForAnomaly(command.actor().tenantId(), command.facilityId(), command.anomalyId(), command.description(), priority);
        ticketRepository.save(ticket);
        return ticket.getId();
    }

    @Override
    public ChallengeStatus evaluate(EvaluateChallengeCommand command) {
        command.challenge().evaluate(command.baselineConsumption(), command.actualConsumption(), command.evaluatedAt());
        challengeRepository.save(command.challenge());
        return command.challenge().getStatus();
    }
}
