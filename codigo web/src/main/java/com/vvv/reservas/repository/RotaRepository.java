package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Rota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RotaRepository extends JpaRepository<Rota, Integer> {
    List<Rota> findAllByOrderByCodigoAsc();
}
