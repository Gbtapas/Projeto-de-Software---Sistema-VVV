package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Passageiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PassageiroRepository extends JpaRepository<Passageiro, Long> {

    Optional<Passageiro> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    List<Passageiro> findAllByAtivoTrueOrderByNomeAsc();
}
