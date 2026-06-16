package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.FuncionarioPontoDeVenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuncionarioPdvRepository extends JpaRepository<FuncionarioPontoDeVenda, Long> {
    List<FuncionarioPontoDeVenda> findByAtivoTrueOrderByDataInicioDesc();
    List<FuncionarioPontoDeVenda> findByFuncionario_IdAndAtivoTrue(Long idFuncionario);
    long countByFuncionario_IdAndAtivoTrue(Long idFuncionario);
}
