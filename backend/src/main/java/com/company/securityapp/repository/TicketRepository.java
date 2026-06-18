package com.company.securityapp.repository;

import com.company.securityapp.entity.Ticket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @EntityGraph(attributePaths = "customer")
    List<Ticket> findAllByOrderByCreatedAtDesc();

    @Override
    @EntityGraph(attributePaths = "customer")
    Optional<Ticket> findById(Long id);
}

