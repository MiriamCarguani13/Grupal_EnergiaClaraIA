package com.energiaclara.api.rest.maintenance;

import com.energiaclara.application.maintenance.dto.CreateTicketCommand;
import com.energiaclara.application.maintenance.service.TicketService;
import com.energiaclara.domain.maintenance.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody CreateTicketCommand command) {
        Ticket createdTicket = ticketService.createTicket(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }

    @GetMapping
    public ResponseEntity<java.util.List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(ticketService.getTicketById(ticketId));
    }

    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<Ticket> assignTicket(@PathVariable UUID ticketId, @RequestBody Map<String, String> body) {
        String tecnicoIdStr = body.get("tecnicoId");
        if (tecnicoIdStr == null || tecnicoIdStr.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UUID tecnicoId = UUID.fromString(tecnicoIdStr);
        Ticket assignedTicket = ticketService.assignTicket(ticketId, tecnicoId);
        return ResponseEntity.ok(assignedTicket);
    }

    @PostMapping("/{ticketId}/close")
    public ResponseEntity<Ticket> closeTicket(@PathVariable UUID ticketId, @RequestBody Map<String, String> body) {
        String tecnicoIdStr = body.get("tecnicoId");
        String qrHash = body.get("qrHash");
        
        if (tecnicoIdStr == null || tecnicoIdStr.isEmpty() || qrHash == null || qrHash.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        UUID tecnicoId = UUID.fromString(tecnicoIdStr);
        Ticket closedTicket = ticketService.closeTicket(ticketId, tecnicoId, qrHash);
        return ResponseEntity.ok(closedTicket);
    }
}
