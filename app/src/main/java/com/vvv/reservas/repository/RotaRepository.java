package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Rota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RotaRepository extends JpaRepository<Rota, Integer> {
    @Query("SELECT r FROM Rota r JOIN FETCH r.cidadeOrigem JOIN FETCH r.cidadeDestino ORDER BY r.codigo ASC")
    List<Rota> findAllByOrderByCodigoAsc();
}
