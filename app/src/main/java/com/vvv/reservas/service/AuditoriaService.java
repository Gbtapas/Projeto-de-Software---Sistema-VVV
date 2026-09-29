package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.LogAuditoria;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.repository.LogAuditoriaRepository;
import com.vvv.reservas.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Registro de auditoria de operações críticas no banco (tabela log_auditoria — RnF06).
 * Usa REQUIRES_NEW para garantir que o log seja persistido independente do estado
 * da transação chamadora.
 */
@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final LogAuditoriaRepository logRepo;
    private final UsuarioRepository usuarioRepo;

    public AuditoriaService(LogAuditoriaRepository logRepo, UsuarioRepository usuarioRepo) {
        this.logRepo = logRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String tabela, Long idRegistro, OperacaoAuditoria operacao,
                          String dadosAnteriores, String dadosNovos) {
        try {
            LogAuditoria entrada = new LogAuditoria();
            entrada.setTabela(tabela);
            entrada.setIdRegistro(idRegistro);
            entrada.setOperacao(operacao);
            entrada.setDadosAnteriores(dadosAnteriores);
            entrada.setDadosNovos(dadosNovos);
            entrada.setIdUsuario(resolverIdUsuario());
            entrada.setIpAddress(resolverIp());
            logRepo.save(entrada);
        } catch (Exception e) {
            // Falha de auditoria nunca interrompe o fluxo principal
            log.error("Falha ao registrar auditoria: tabela={}, id={}", tabela, idRegistro, e);
        }
    }

    private Long resolverIdUsuario() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
            return usuarioRepo.findByEmail(auth.getName()).map(u -> u.getId()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String resolverIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
