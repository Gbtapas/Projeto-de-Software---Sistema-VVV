package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.FuncionarioPontoDeVenda;
import com.vvv.reservas.model.entity.PontoDeVenda;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.repository.FuncionarioPdvRepository;
import com.vvv.reservas.repository.FuncionarioRepository;
import com.vvv.reservas.repository.PontoDeVendaRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PontoDeVendaServiceTest {

    @Mock PontoDeVendaRepository pdvRepo;
    @Mock FuncionarioRepository funcionarioRepo;
    @Mock FuncionarioPdvRepository fpvRepo;
    @Mock AuditoriaService auditoria;
    @InjectMocks PontoDeVendaService service;

    // ------------------------------------------------------------------ listar

    @Test
    @DisplayName("listar retorna apenas PDVs ativos")
    void listar_delegaAoRepositorio() {
        PontoDeVenda pdv = new PontoDeVenda();
        pdv.setNome("Loja Centro");
        when(pdvRepo.findAllByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(pdv));

        List<PontoDeVenda> resultado = service.listar();

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("listarVinculos retorna vínculos ativos")
    void listarVinculos_delegaAoRepositorio() {
        when(fpvRepo.findByAtivoTrueOrderByDataInicioDesc()).thenReturn(List.of(new FuncionarioPontoDeVenda()));

        List<FuncionarioPontoDeVenda> resultado = service.listarVinculos();

        assertThat(resultado).hasSize(1);
    }

    // ------------------------------------------------------------------ salvar

    @Test
    @DisplayName("salvar persiste PDV com código gerado e registra auditoria")
    void salvar_sucesso_retornaPdvERegistraAuditoria() throws Exception {
        PontoDeVenda salvo = new PontoDeVenda();
        salvo.setCnpj("12345678000199");
        salvo.setNome("Filial Norte");
        java.lang.reflect.Field idField = PontoDeVenda.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(salvo, 1);

        when(pdvRepo.save(any())).thenReturn(salvo);

        PontoDeVenda resultado = service.salvar(
                "12345678000199", "Filial Norte", "Rua A", "Centro",
                "01310100", "São Paulo", "SP", "(11)1234-5678", null);

        assertThat(resultado.getNome()).isEqualTo("Filial Norte");

        ArgumentCaptor<PontoDeVenda> captor = ArgumentCaptor.forClass(PontoDeVenda.class);
        verify(pdvRepo).save(captor.capture());
        assertThat(captor.getValue().getCodigo()).startsWith("PDV");
        assertThat(captor.getValue().getAtivo()).isTrue();

        verify(auditoria).registrar(eq("pontos_de_venda"), any(), eq(OperacaoAuditoria.INSERT), eq(null), anyString());
    }

    // ------------------------------------------------------------------ vincular

    @Test
    @DisplayName("vincular usa hoje como data de início quando dataInicio é null")
    void vincular_dataNull_usaHoje() {
        FuncionarioPontoDeVenda salvo = new FuncionarioPontoDeVenda();
        when(funcionarioRepo.getReferenceById(1L)).thenReturn(new com.vvv.reservas.model.entity.Funcionario());
        when(pdvRepo.getReferenceById(2)).thenReturn(new PontoDeVenda());
        when(fpvRepo.save(any())).thenReturn(salvo);

        service.vincular(1L, 2, null);

        ArgumentCaptor<FuncionarioPontoDeVenda> captor = ArgumentCaptor.forClass(FuncionarioPontoDeVenda.class);
        verify(fpvRepo).save(captor.capture());
        assertThat(captor.getValue().getDataInicio()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("vincular persiste vínculo e registra auditoria")
    void vincular_comData_salvaEAudita() {
        LocalDate inicio = LocalDate.of(2026, 1, 15);
        FuncionarioPontoDeVenda salvo = new FuncionarioPontoDeVenda();
        when(funcionarioRepo.getReferenceById(1L)).thenReturn(new com.vvv.reservas.model.entity.Funcionario());
        when(pdvRepo.getReferenceById(2)).thenReturn(new PontoDeVenda());
        when(fpvRepo.save(any())).thenReturn(salvo);

        service.vincular(1L, 2, inicio);

        verify(fpvRepo).save(any());
        verify(auditoria).registrar(eq("funcionarios_pontos_de_venda"), any(), eq(OperacaoAuditoria.INSERT),
                eq(null), anyString());
    }

    // ------------------------------------------------------------------ desvincular

    @Test
    @DisplayName("desvincular lança exception quando vínculo não encontrado")
    void desvincular_naoEncontrado_lancaException() {
        when(fpvRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desvincular(99L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Vínculo");
    }

    @Test
    @DisplayName("desvincular define ativo=false com data de hoje e audita")
    void desvincular_sucesso_desativaEAudita() {
        FuncionarioPontoDeVenda fpv = new FuncionarioPontoDeVenda();
        fpv.setAtivo(true);
        when(fpvRepo.findById(5L)).thenReturn(Optional.of(fpv));
        when(fpvRepo.save(any())).thenReturn(fpv);

        service.desvincular(5L);

        assertThat(fpv.getAtivo()).isFalse();
        assertThat(fpv.getDataFim()).isEqualTo(LocalDate.now());
        verify(auditoria).registrar(eq("funcionarios_pontos_de_venda"), eq(5L), eq(OperacaoAuditoria.UPDATE),
                eq("{\"ativo\":true}"), eq("{\"ativo\":false}"));
    }
}
