package com.company.securityapp.controller;

import com.company.securityapp.dto.TicketRequest;
import com.company.securityapp.dto.TicketResponse;
import com.company.securityapp.dto.TicketStatusUpdateRequest;
import com.company.securityapp.service.TicketService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketResponse> getTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}")
    public TicketResponse getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ticket created successfully."),
        @ApiResponse(responseCode = "404", description = "Customer not found.")
    })
    public TicketResponse createTicket(@Valid @RequestBody TicketRequest request) {
        return ticketService.createTicket(request);
    }

    @PutMapping("/{id}")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket updated successfully."),
        @ApiResponse(responseCode = "404", description = "Ticket or customer not found.")
    })
    public TicketResponse updateTicket(@PathVariable Long id, @Valid @RequestBody TicketRequest request) {
        return ticketService.updateTicket(id, request);
    }

    @PatchMapping("/{id}/status")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket status updated successfully."),
        @ApiResponse(responseCode = "404", description = "Ticket not found.")
    })
    public TicketResponse updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusUpdateRequest request) {
        return ticketService.updateTicketStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Ticket deleted successfully."),
        @ApiResponse(responseCode = "404", description = "Ticket not found.")
    })
    public void deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
    }
}

