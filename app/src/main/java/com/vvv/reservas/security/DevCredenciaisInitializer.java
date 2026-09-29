package com.vvv.reservas.security;

import com.vvv.reservas.model.entity.Usuario;
import com.vvv.reservas.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Increment 0 — Credenciais de desenvolvimento.
 *
 * O seed (05_seed.sql) grava senha_hash como placeholder inválido
 * ('$2a$12$PLACEHOLDER...'). Aqui, no startup, geramos o bcrypt real da senha
 * padrão "senha123" para esses usuários, usando o MESMO encoder da aplicação
 * (evita divergência de hash). É idempotente: após atualizar, o hash deixa de
 * começar por 'PLACEHOLDER' e não é tocado de novo.
 */
@Component
public class DevCredenciaisInitializer implements CommandLineRunner {

    private static final String PLACEHOLDER_PREFIXO = "$2a$12$PLACEHOLDER";
    private static final String SENHA_PADRAO = "senha123";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DevCredenciaisInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<Usuario> pendentes = usuarioRepository.findBySenhaHashStartingWith(PLACEHOLDER_PREFIXO);
        if (pendentes.isEmpty()) {
            return;
        }
        String hash = passwordEncoder.encode(SENHA_PADRAO);
        pendentes.forEach(u -> u.setSenhaHash(hash));
        usuarioRepository.saveAll(pendentes);
        System.out.printf("[DEV] %d usuário(s) tiveram a senha definida como '%s'.%n",
                pendentes.size(), SENHA_PADRAO);
    }
}
