package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    /** Usuários cujo hash ainda é o placeholder do seed (para inicialização de dev). */
    List<Usuario> findBySenhaHashStartingWith(String prefixo);
}
