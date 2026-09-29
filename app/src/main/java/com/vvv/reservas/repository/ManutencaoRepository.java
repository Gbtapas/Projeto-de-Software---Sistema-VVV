package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Manutencao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Integer> {
    List<Manutencao> findAllByOrderByDataInicioDesc();
}
