package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.LogAuditoria;
import com.vvv.reservas.model.entity.Usuario;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.repository.LogAuditoriaRepository;
import com.vvv.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock LogAuditoriaRepository logRepo;
    @Mock UsuarioRepository usuarioRepo;
    @InjectMocks AuditoriaService service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------------------------------------------------ registrar

    @Test
    @DisplayName("registrar persiste LogAuditoria com todos os campos informados")
    void registrar_salvaNaTabela() {
        service.registrar("passageiros", 10L, OperacaoAuditoria.INSERT, null, "{\"nome\":\"João\"}");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logRepo).save(captor.capture());

        LogAuditoria salvo = captor.getValue();
        assertThat(salvo.getTabela()).isEqualTo("passageiros");
        assertThat(salvo.getIdRegistro()).isEqualTo(10L);
        assertThat(salvo.getOperacao()).isEqualTo(OperacaoAuditoria.INSERT);
        assertThat(salvo.getDadosAnteriores()).isNull();
        assertThat(salvo.getDadosNovos()).isEqualTo("{\"nome\":\"João\"}");
    }

    @Test
    @DisplayName("registrar resolve idUsuario via SecurityContext quando autenticado")
    void registrar_usuarioAutenticado_resolveIdUsuario() {
        Usuario u = new Usuario();
        u.setEmail("admin@vvv.com");

        var auth = new UsernamePasswordAuthenticationToken("admin@vvv.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(usuarioRepo.findByEmail("admin@vvv.com")).thenReturn(Optional.of(u));

        service.registrar("reservas", 1L, OperacaoAuditoria.UPDATE, "{}", "{}");

        verify(logRepo).save(any());
    }

    @Test
    @DisplayName("registrar não lança exceção quando SecurityContext está vazio (anonimous)")
    void registrar_semUsuarioAutenticado_idUsuarioNull() {
        service.registrar("reservas", 1L, OperacaoAuditoria.INSERT, null, "{}");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logRepo).save(captor.capture());
        assertThat(captor.getValue().getIdUsuario()).isNull();
    }

    @Test
    @DisplayName("registrar absorve exceção do repositório sem propagar para o chamador")
    void registrar_falhaNoRepositorio_naoInterrompeFluxo() {
        when(logRepo.save(any())).thenThrow(new RuntimeException("banco indisponível"));

        // não deve lançar nenhuma exceção
        service.registrar("tabela", 1L, OperacaoAuditoria.INSERT, null, "{}");
    }
}
