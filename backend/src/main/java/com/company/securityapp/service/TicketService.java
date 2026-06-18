package com.company.securityapp.service;

import com.company.securityapp.dto.TicketRequest;
import com.company.securityapp.dto.TicketResponse;
import com.company.securityapp.dto.TicketStatusUpdateRequest;
import com.company.securityapp.entity.Customer;
import com.company.securityapp.entity.Ticket;
import com.company.securityapp.entity.TicketStatus;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.CustomerRepository;
import com.company.securityapp.repository.TicketRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;

    public TicketService(
            TicketRepository ticketRepository,
            CustomerRepository customerRepository,
            AuditLogService auditLogService) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.auditLogService = auditLogService;
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TicketResponse getTicketById(Long id) {
        return toResponse(findTicket(id));
    }

    @Transactional
    public TicketResponse createTicket(TicketRequest request) {
        Customer customer = findCustomer(request.customerId());

        Ticket ticket = new Ticket();
        applyRequest(ticket, request, customer, TicketStatus.OPEN);
        Ticket savedTicket = ticketRepository.save(ticket);
        auditLogService.logBusinessAction(
                "CREATE_TICKET",
                "Ticket",
                savedTicket.getId(),
                true,
                "Created ticket for customer #" + customer.getId());
        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse updateTicket(Long id, TicketRequest request) {
        Ticket ticket = findTicket(id);
        Customer customer = findCustomer(request.customerId());

        applyRequest(ticket, request, customer, ticket.getStatus());
        Ticket savedTicket = ticketRepository.save(ticket);
        auditLogService.logBusinessAction(
                "UPDATE_TICKET",
                "Ticket",
                savedTicket.getId(),
                true,
                "Updated ticket #" + savedTicket.getId());
        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse updateTicketStatus(Long id, TicketStatusUpdateRequest request) {
        Ticket ticket = findTicket(id);
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(request.status());
        Ticket savedTicket = ticketRepository.save(ticket);
        auditLogService.logBusinessAction(
                "UPDATE_TICKET_STATUS",
                "Ticket",
                savedTicket.getId(),
                true,
                "Updated ticket status from " + oldStatus + " to " + savedTicket.getStatus());
        return toResponse(savedTicket);
    }

    @Transactional
    public void deleteTicket(Long id) {
        Ticket ticket = findTicket(id);
        ticketRepository.delete(ticket);
        auditLogService.logBusinessAction(
                "DELETE_TICKET",
                "Ticket",
                ticket.getId(),
                true,
                "Deleted ticket #" + ticket.getId());
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ticket not found."));
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found."));
    }

    private void applyRequest(Ticket ticket, TicketRequest request, Customer customer, TicketStatus defaultStatus) {
        ticket.setCustomer(customer);
        ticket.setTitle(normalizeText(request.title()));
        ticket.setDescription(normalizeText(request.description()));
        ticket.setPriority(request.priority());
        ticket.setStatus(request.status() == null ? defaultStatus : request.status());
        ticket.setCreatedById(request.createdById());
        ticket.setAssignedToId(request.assignedToId());
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getCustomer().getId(),
                ticket.getCustomer().getName(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedById(),
                ticket.getAssignedToId(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
}

