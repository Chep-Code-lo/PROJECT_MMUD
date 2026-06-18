package com.company.securityapp.dto;

import com.company.securityapp.entity.TicketPriority;
import com.company.securityapp.entity.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketRequest(
        @NotNull(message = "Customer id is required.")
        Long customerId,

        @NotBlank(message = "Title is required.")
        @Size(max = 255, message = "Title must be at most 255 characters.")
        String title,

        @NotBlank(message = "Description is required.")
        @Size(max = 2000, message = "Description must be at most 2000 characters.")
        String description,

        @NotNull(message = "Priority is required.")
        TicketPriority priority,

        TicketStatus status,

        @NotNull(message = "Created by id is required.")
        Long createdById,

        Long assignedToId) {
}

