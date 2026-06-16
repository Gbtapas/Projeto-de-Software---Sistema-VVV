package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Aeroporto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AeroportoRepository extends JpaRepository<Aeroporto, Integer> {
    List<Aeroporto> findAllByOrderByCodigoIataAsc();
}
