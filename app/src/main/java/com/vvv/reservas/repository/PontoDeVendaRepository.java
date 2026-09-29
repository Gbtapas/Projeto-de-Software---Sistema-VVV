package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.PontoDeVenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PontoDeVendaRepository extends JpaRepository<PontoDeVenda, Integer> {
    List<PontoDeVenda> findAllByOrderByNomeAsc();
    List<PontoDeVenda> findAllByAtivoTrueOrderByNomeAsc();
}
