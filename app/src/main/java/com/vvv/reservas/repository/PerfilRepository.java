package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Short> {
    Optional<Perfil> findByNome(String nome);
}
