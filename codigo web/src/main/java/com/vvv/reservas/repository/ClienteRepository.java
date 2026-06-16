package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    List<Cliente> findAllByAtivoTrueOrderByNomeAsc();
}
