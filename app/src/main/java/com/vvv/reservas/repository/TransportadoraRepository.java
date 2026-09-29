package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Transportadora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportadoraRepository extends JpaRepository<Transportadora, Integer> {
    List<Transportadora> findAllByOrderByNomeAsc();
}
