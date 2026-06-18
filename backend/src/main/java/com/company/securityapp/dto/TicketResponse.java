package com.company.securityapp.dto;

import com.company.securityapp.entity.TicketPriority;
import com.company.securityapp.entity.TicketStatus;
import java.time.Instant;

public record TicketResponse(
        Long id,
        Long customerId,
        String customerName,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        Long createdById,
        Long assignedToId,
        Instant createdAt,
        Instant updatedAt) {
}

