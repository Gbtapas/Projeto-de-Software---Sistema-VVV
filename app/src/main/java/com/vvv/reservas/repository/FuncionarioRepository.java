package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    Optional<Funcionario> findByUsuario_Email(String email);
    List<Funcionario> findAllByAtivoTrueOrderByNomeAsc();
    boolean existsByCpf(String cpf);
}
