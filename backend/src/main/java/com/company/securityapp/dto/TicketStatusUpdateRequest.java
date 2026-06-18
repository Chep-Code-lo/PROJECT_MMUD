package com.company.securityapp.dto;

import com.company.securityapp.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TicketStatusUpdateRequest(
        @NotNull(message = "Status is required.")
        TicketStatus status) {
}
