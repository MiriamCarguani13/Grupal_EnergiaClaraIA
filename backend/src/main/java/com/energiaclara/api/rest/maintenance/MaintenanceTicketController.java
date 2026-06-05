package com.energiaclara.api.rest.maintenance;

import com.energiaclara.core.security.AuthenticatedUser;
import com.energiaclara.infrastructure.persistence.entity.EnergyAnomalyEntity;
import com.energiaclara.infrastructure.persistence.entity.MaintenanceTicketEntity;
import com.energiaclara.infrastructure.persistence.entity.UserEntity;
import com.energiaclara.infrastructure.persistence.repository.EnergyAnomalyRepository;
import com.energiaclara.infrastructure.persistence.repository.MaintenanceTicketRepository;
import com.energiaclara.infrastructure.persistence.repository.UserJpaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance/tickets")
public class MaintenanceTicketController {

    private static final int MAX_EVIDENCE_BYTES = 5 * 1024 * 1024;
    private static final Path EVIDENCE_DIR = Path.of("uploads", "ticket-evidence");
    private static final String STATUS_PENDING = "PENDIENTE";
    private static final String STATUS_IN_PROGRESS = "EN_PROGRESO";
    private static final String STATUS_REPAIRED = "REPARADO";
    private static final String STATUS_CLOSED = "CERRADO";
    private static final String ANOMALY_DETECTED = "DETECTADA";
    private static final String ANOMALY_DERIVED = "DERIVADA";
    private static final String ANOMALY_IN_PROGRESS = "EN_ATENCION";
    private static final String ANOMALY_RESOLVED = "RESUELTA";

    private final MaintenanceTicketRepository ticketRepository;
    private final EnergyAnomalyRepository anomalyRepository;
    private final UserJpaRepository userRepository;
    private final UUID demoTechnicianId;
    private final UUID demoSlaId;
    private final UUID demoBuildingId;

    public MaintenanceTicketController(
            MaintenanceTicketRepository ticketRepository,
            EnergyAnomalyRepository anomalyRepository,
            UserJpaRepository userRepository,
            @Value("${app.maintenance.demo-technician-id:55555555-5555-5555-5555-555555555555}") UUID demoTechnicianId,
            @Value("${app.maintenance.demo-sla-id:66666666-6666-6666-6666-666666666666}") UUID demoSlaId,
            @Value("${app.maintenance.demo-building-id:22222222-2222-2222-2222-222222222222}") UUID demoBuildingId
    ) {
        this.ticketRepository = ticketRepository;
        this.anomalyRepository = anomalyRepository;
        this.userRepository = userRepository;
        this.demoTechnicianId = demoTechnicianId;
        this.demoSlaId = demoSlaId;
        this.demoBuildingId = demoBuildingId;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TECNICO','ADMIN_INSTITUCION')")
    public List<MaintenanceTicketDto> list(
            @AuthenticationPrincipal AuthenticatedUser current,
            Authentication authentication
    ) {
        List<MaintenanceTicketEntity> tickets;
        if (hasRole(authentication, "ADMIN_INSTITUCION")) {
            tickets = ticketRepository.findTop50ByTenantIdOrderByUpdatedAtDesc(current.tenantId());
        } else {
            tickets = ticketRepository.findTop50ByTenantIdAndAssignedToOrderByUpdatedAtDesc(current.tenantId(), current.userId());
        }
        return tickets.stream().map(this::toDto).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_INSTITUCION')")
    public ResponseEntity<MaintenanceTicketDto> create(
            @Valid @RequestBody CreateMaintenanceTicketRequest request,
            @AuthenticationPrincipal AuthenticatedUser current
    ) {
        EnergyAnomalyEntity anomaly = anomalyRepository.findById(request.anomalyId())
                .orElseThrow(() -> new IllegalArgumentException("Anomalia no encontrada"));
        if (!anomaly.getTenantId().equals(current.tenantId())) {
            throw new AccessDeniedException("Anomalia de otro tenant");
        }
        if (!ANOMALY_DETECTED.equals(anomaly.getEstado())) {
            throw new IllegalStateException("Solo una anomalia DETECTADA puede derivarse a ticket");
        }
        ticketRepository.findByAnomalyId(anomaly.getId())
                .ifPresent(existing -> {
                    throw new IllegalStateException("La anomalia ya tiene un ticket asociado");
                });

        UUID assignedTo = request.assignedTo() == null ? demoTechnicianId : request.assignedTo();
        MaintenanceTicketEntity ticket = new MaintenanceTicketEntity();
        Instant now = Instant.now();
        ticket.setTenantId(current.tenantId());
        ticket.setBuildingId(demoBuildingId);
        ticket.setAnomalyId(anomaly.getId());
        ticket.setSlaPolicyId(demoSlaId);
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(normalizePriority(request.priority()));
        ticket.setStatus(STATUS_PENDING);
        ticket.setAssignedTo(assignedTo);
        ticket.setCreatedBy(current.userId());
        ticket.setResponsibleTechnicianId(assignedTo);
        ticket.setSlaDueAt(now.plusSeconds(24 * 60 * 60L));
        ticket.setSlaBreached(false);
        ticket.setReopenCount(0);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        MaintenanceTicketEntity saved = ticketRepository.save(ticket);
        anomaly.setEstado(ANOMALY_DERIVED);
        anomaly.setTicketId(saved.getId());
        anomaly.setResponsibleTechnicianId(assignedTo);
        anomaly.setResponsibleTechnicianName(resolveUserName(assignedTo));
        anomalyRepository.save(anomaly);

        return ResponseEntity.ok(toDto(saved));
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('TECNICO','ADMIN_INSTITUCION')")
    public MaintenanceTicketDto get(
            @PathVariable UUID ticketId,
            @AuthenticationPrincipal AuthenticatedUser current,
            Authentication authentication
    ) {
        MaintenanceTicketEntity ticket = loadAuthorizedTicket(ticketId, current, authentication);
        return toDto(ticket);
    }

    @PutMapping("/{ticketId}/technical-sheet")
    @PreAuthorize("hasAnyRole('TECNICO','ADMIN_INSTITUCION')")
    public ResponseEntity<MaintenanceTicketDto> updateTechnicalSheet(
            @PathVariable UUID ticketId,
            @Valid @RequestBody UpdateTechnicalSheetRequest request,
            @AuthenticationPrincipal AuthenticatedUser current,
            Authentication authentication
    ) {
        MaintenanceTicketEntity ticket = loadAuthorizedTicket(ticketId, current, authentication);
        boolean admin = hasRole(authentication, "ADMIN_INSTITUCION");
        if (!admin && isResolved(ticket.getStatus())) {
            throw new AccessDeniedException("El ticket resuelto solo puede ser modificado por ADMIN");
        }
        Instant now = Instant.now();

        if (ticket.getAssignedTo() == null) {
            ticket.setAssignedTo(current.userId());
        }
        ticket.setResponsibleTechnicianId(current.userId());
        ticket.setTechnicalDiagnosis(request.technicalDiagnosis());
        ticket.setAppliedSolution(request.appliedSolution());
        ticket.setUsedMaterials(request.usedMaterials());
        ticket.setTechnicalNotes(request.technicalNotes());
        String technicalStatus = normalizeTechnicalStatus(request.technicalStatus());
        ticket.setTechnicalStatus(technicalStatus);
        ticket.setAttendedAt(now);
        ticket.setUpdatedAt(now);
        if (request.evidenceImageDataUrl() != null && !request.evidenceImageDataUrl().isBlank()) {
            ticket.setEvidenceImageUrl(storeEvidence(ticket.getId(), request.evidenceImageDataUrl()));
        }
        if (STATUS_REPAIRED.equals(technicalStatus)) {
            ticket.setStatus(STATUS_REPAIRED);
            resolveAssociatedAnomaly(ticket, current.userId(), now);
        } else if (STATUS_CLOSED.equals(technicalStatus)) {
            ticket.setStatus(STATUS_CLOSED);
            ticket.setClosedAt(now);
            ticket.setClosedBy(current.userId());
            resolveAssociatedAnomaly(ticket, current.userId(), now);
        } else if (STATUS_PENDING.equals(normalizeTicketStatus(ticket.getStatus()))) {
            ticket.setStatus(STATUS_IN_PROGRESS);
            markAssociatedAnomalyInProgress(ticket);
        }

        return ResponseEntity.ok(toDto(ticketRepository.save(ticket)));
    }

    private MaintenanceTicketEntity loadAuthorizedTicket(
            UUID ticketId,
            AuthenticatedUser current,
            Authentication authentication
    ) {
        MaintenanceTicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));

        if (!ticket.getTenantId().equals(current.tenantId())) {
            throw new AccessDeniedException("Ticket de otro tenant");
        }
        if (hasRole(authentication, "ADMIN_INSTITUCION")) {
            return ticket;
        }
        if (ticket.getAssignedTo() != null && ticket.getAssignedTo().equals(current.userId())) {
            return ticket;
        }
        throw new AccessDeniedException("El ticket no esta asignado al tecnico actual");
    }

    private boolean hasRole(Authentication authentication, String role) {
        String authority = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }

    private String normalizeTechnicalStatus(String status) {
        if (status == null || status.isBlank()) {
            return "EN_REVISION";
        }
        return status.trim().toUpperCase();
    }

    private String normalizePriority(String priority) {
        String normalized = priority == null ? "MEDIA" : priority.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "BAJA", "LOW" -> "BAJA";
            case "MEDIA", "MEDIUM" -> "MEDIA";
            case "ALTA", "HIGH" -> "ALTA";
            case "CRITICA", "CRITICAL" -> "CRITICA";
            default -> "MEDIA";
        };
    }

    private void markAssociatedAnomalyInProgress(MaintenanceTicketEntity ticket) {
        if (ticket.getAnomalyId() == null) {
            return;
        }
        anomalyRepository.findById(ticket.getAnomalyId()).ifPresent(anomaly -> {
            if (!ANOMALY_RESOLVED.equals(anomaly.getEstado())) {
                anomaly.setEstado(ANOMALY_IN_PROGRESS);
                anomaly.setTicketId(ticket.getId());
                anomaly.setResponsibleTechnicianId(ticket.getResponsibleTechnicianId());
                anomaly.setResponsibleTechnicianName(resolveUserName(ticket.getResponsibleTechnicianId()));
                anomalyRepository.save(anomaly);
            }
        });
    }

    private void resolveAssociatedAnomaly(MaintenanceTicketEntity ticket, UUID resolvedBy, Instant resolvedAt) {
        if (ticket.getAnomalyId() == null) {
            return;
        }
        anomalyRepository.findById(ticket.getAnomalyId()).ifPresent(anomaly -> {
            anomaly.setEstado(ANOMALY_RESOLVED);
            anomaly.setTicketId(ticket.getId());
            anomaly.setResolvedAt(resolvedAt);
            anomaly.setResolvedBy(resolvedBy);
            anomaly.setResponsibleTechnicianId(ticket.getResponsibleTechnicianId());
            anomaly.setResponsibleTechnicianName(resolveUserName(ticket.getResponsibleTechnicianId()));
            anomalyRepository.save(anomaly);
        });
    }

    private String resolveUserName(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .map(UserEntity::getFullName)
                .orElse(null);
    }

    private boolean isResolved(String status) {
        String normalized = normalizeTicketStatus(status);
        return STATUS_REPAIRED.equals(normalized) || STATUS_CLOSED.equals(normalized);
    }

    private String normalizeTicketStatus(String status) {
        if (status == null || status.isBlank() || "ASIGNADO".equals(status) || "ABIERTO".equals(status)) {
            return STATUS_PENDING;
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private String storeEvidence(UUID ticketId, String dataUrl) {
        String prefix;
        String extension;
        if (dataUrl.startsWith("data:image/jpeg;base64,")) {
            prefix = "data:image/jpeg;base64,";
            extension = "jpg";
        } else if (dataUrl.startsWith("data:image/jpg;base64,")) {
            prefix = "data:image/jpg;base64,";
            extension = "jpg";
        } else if (dataUrl.startsWith("data:image/png;base64,")) {
            prefix = "data:image/png;base64,";
            extension = "png";
        } else {
            throw new IllegalArgumentException("La evidencia debe ser una imagen JPG o PNG");
        }

        byte[] bytes = Base64.getDecoder().decode(dataUrl.substring(prefix.length()));
        if (bytes.length > MAX_EVIDENCE_BYTES) {
            throw new IllegalArgumentException("La evidencia no debe superar 5MB");
        }

        try {
            Files.createDirectories(EVIDENCE_DIR);
            String fileName = ticketId + "-" + System.currentTimeMillis() + "." + extension;
            Files.write(EVIDENCE_DIR.resolve(fileName), bytes);
            return "/uploads/ticket-evidence/" + fileName;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la evidencia fotografica");
        }
    }

    private MaintenanceTicketDto toDto(MaintenanceTicketEntity ticket) {
        return new MaintenanceTicketDto(
                ticket.getId(),
                ticket.getAnomalyId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                normalizeTicketStatus(ticket.getStatus()),
                ticket.getAssignedTo(),
                ticket.getSlaDueAt(),
                ticket.isSlaBreached(),
                ticket.getTechnicalDiagnosis(),
                ticket.getAppliedSolution(),
                ticket.getUsedMaterials(),
                ticket.getTechnicalNotes(),
                ticket.getTechnicalStatus(),
                ticket.getAttendedAt(),
                ticket.getResponsibleTechnicianId(),
                ticket.getEvidenceImageUrl()
        );
    }
}
