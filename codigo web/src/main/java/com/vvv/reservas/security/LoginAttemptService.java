package com.vvv.reservas.security;

import com.vvv.reservas.model.entity.Usuario;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.repository.UsuarioRepository;
import com.vvv.reservas.service.AuditoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * UC18 — Rastreia tentativas de login e bloqueia a conta por 15 minutos após 3 falhas.
 * Registra acessos bem-sucedidos em log_auditoria (UC18 passo 6).
 */
@Service
public class LoginAttemptService {

    private static final int MAX_TENTATIVAS = 3;
    private static final int MINUTOS_BLOQUEIO = 15;

    private final UsuarioRepository usuarioRepo;
    private final AuditoriaService auditoria;

    public LoginAttemptService(UsuarioRepository usuarioRepo, AuditoriaService auditoria) {
        this.usuarioRepo = usuarioRepo;
        this.auditoria = auditoria;
    }

    @Transactional
    public void registrarFalha(String email) {
        usuarioRepo.findByEmail(email).ifPresent(u -> {
            int falhas = u.getTentativasFalhas() + 1;
            u.setTentativasFalhas(falhas);
            if (falhas >= MAX_TENTATIVAS) {
                u.setBloqueadoAte(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEIO));
            }
            usuarioRepo.save(u);
        });
    }

    @Transactional
    public void registrarSucesso(String email) {
        usuarioRepo.findByEmail(email).ifPresent(u -> {
            u.setTentativasFalhas(0);
            u.setBloqueadoAte(null);
            u.setUltimoAcesso(LocalDateTime.now());
            usuarioRepo.save(u);
            auditoria.registrar("usuarios", u.getId(), OperacaoAuditoria.INSERT,
                    null, "{\"evento\":\"LOGIN\",\"email\":\"" + email + "\"}");
        });
    }
}
