package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    Optional<Venda> findByReserva_Id(Long idReserva);
}
