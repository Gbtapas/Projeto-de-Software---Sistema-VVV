package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Cidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CidadeRepository extends JpaRepository<Cidade, Integer> {

    List<Cidade> findAllByOrderByNomeAsc();
}
