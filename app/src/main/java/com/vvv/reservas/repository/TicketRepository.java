package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /** Ticket emitido pela trigger, localizado pela reserva. */
    Optional<Ticket> findByReserva_Id(Long idReserva);
}
