package com.vvv.reservas.service;

import com.vvv.reservas.dto.PassageiroForm;
import com.vvv.reservas.model.entity.Passageiro;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.repository.PassageiroRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassageiroServiceTest {

    @Mock PassageiroRepository passageiroRepository;
    @Mock AuditoriaService auditoria;
    @InjectMocks PassageiroService service;

    // ------------------------------------------------------------------ listarAtivos

    @Test
    @DisplayName("listarAtivos delega ao repositório e retorna a lista")
    void listarAtivos_retornaPassageirosAtivos() {
        Passageiro p = passageiro("12345678901", "Ana Silva");
        when(passageiroRepository.findAllByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(p));

        List<Passageiro> resultado = service.listarAtivos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Ana Silva");
    }

    // ------------------------------------------------------------------ buscar

    @Test
    @DisplayName("buscar retorna passageiro quando encontrado")
    void buscar_passageiroEncontrado_retornaPassageiro() {
        Passageiro p = passageiro("12345678901", "João Souza");
        when(passageiroRepository.findById(1L)).thenReturn(Optional.of(p));

        Passageiro resultado = service.buscar(1L);

        assertThat(resultado.getNome()).isEqualTo("João Souza");
    }

    @Test
    @DisplayName("buscar lança RegraNegocioException quando não encontrado")
    void buscar_passageiroNaoEncontrado_lancaException() {
        when(passageiroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não encontrado");
    }

    // ------------------------------------------------------------------ cadastrar

    @Test
    @DisplayName("cadastrar lança exceção quando CPF já existe")
    void cadastrar_cpfDuplicado_lancaException() {
        PassageiroForm form = form("12345678901", "Maria");
        when(passageiroRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(form))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("CPF");

        verify(passageiroRepository, never()).save(any());
    }

    @Test
    @DisplayName("cadastrar salva passageiro com código gerado e registra auditoria")
    void cadastrar_cpfNovo_salvaNaBaseERegistraAuditoria() {
        PassageiroForm form = form("98765432100", "Pedro Alves");
        Passageiro salvo = passageiro("98765432100", "Pedro Alves");
        when(passageiroRepository.existsByCpf("98765432100")).thenReturn(false);
        when(passageiroRepository.save(any())).thenReturn(salvo);

        Passageiro resultado = service.cadastrar(form);

        assertThat(resultado.getCpf()).isEqualTo("98765432100");

        ArgumentCaptor<Passageiro> captor = ArgumentCaptor.forClass(Passageiro.class);
        verify(passageiroRepository).save(captor.capture());
        assertThat(captor.getValue().getCodigo()).startsWith("PAS");
        assertThat(captor.getValue().getAtivo()).isTrue();

        verify(auditoria).registrar(eq("passageiros"), any(), eq(OperacaoAuditoria.INSERT), eq(null), anyString());
    }

    // ------------------------------------------------------------------ helpers

    private Passageiro passageiro(String cpf, String nome) {
        Passageiro p = new Passageiro();
        p.setCpf(cpf);
        p.setNome(nome);
        p.setDataNascimento(LocalDate.of(1990, 1, 1));
        p.setAtivo(true);
        p.setCodigo("PAS" + System.currentTimeMillis());
        return p;
    }

    private PassageiroForm form(String cpf, String nome) {
        PassageiroForm f = new PassageiroForm();
        f.setCpf(cpf);
        f.setNome(nome);
        f.setDataNascimento(LocalDate.of(1990, 1, 1));
        return f;
    }
}
