package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.Passageiro;
import com.vvv.reservas.model.entity.ProgramacaoViagem;
import com.vvv.reservas.model.entity.Reserva;
import com.vvv.reservas.model.enums.CanalReserva;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.repository.ProgramacaoViagemRepository;
import com.vvv.reservas.repository.ReservaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock ReservaRepository reservaRepository;
    @Mock ProgramacaoViagemRepository programacaoRepository;
    @Mock EntityManager entityManager;
    @Mock AuditoriaService auditoria;
    @InjectMocks ReservaService service;

    // ------------------------------------------------------------------ listarPorUsuario

    @Test
    @DisplayName("listarPorUsuario delega ao repositório com o e-mail informado")
    void listarPorUsuario_delegaAoRepositorio() {
        Reserva r = new Reserva();
        when(reservaRepository.findMinhasReservas("user@vvv.com")).thenReturn(List.of(r));

        List<Reserva> resultado = service.listarPorUsuario("user@vvv.com");

        assertThat(resultado).hasSize(1);
        verify(reservaRepository).findMinhasReservas("user@vvv.com");
    }

    // ------------------------------------------------------------------ buscar

    @Test
    @DisplayName("buscar retorna a reserva quando encontrada")
    void buscar_encontrado_retornaReserva() {
        Reserva r = new Reserva();
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(r));

        Reserva resultado = service.buscar(1L);

        assertThat(resultado).isSameAs(r);
    }

    @Test
    @DisplayName("buscar lança RegraNegocioException quando reserva não existe")
    void buscar_naoEncontrado_lancaException() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não encontrada");
    }

    // ------------------------------------------------------------------ criar

    @Test
    @DisplayName("criar lança exception quando programação não é encontrada")
    void criar_programacaoNaoEncontrada_lancaException() {
        when(programacaoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(99, 1L, CanalReserva.ONLINE))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Viagem");
    }

    @Test
    @DisplayName("criar persiste reserva, faz refresh e registra auditoria")
    void criar_sucesso_retornaReservaERegistraAuditoria() {
        ProgramacaoViagem prog = new ProgramacaoViagem();
        prog.setValorBase(new BigDecimal("250.00"));

        Reserva salva = new Reserva();
        salva.setCanal(CanalReserva.ONLINE);

        when(programacaoRepository.findById(1)).thenReturn(Optional.of(prog));
        when(entityManager.getReference(eq(Passageiro.class), eq(10L))).thenReturn(new Passageiro());
        when(reservaRepository.saveAndFlush(any())).thenReturn(salva);
        doNothing().when(entityManager).refresh(any());

        Reserva resultado = service.criar(1, 10L, CanalReserva.ONLINE);

        assertThat(resultado).isSameAs(salva);
        verify(entityManager).refresh(salva);
        verify(auditoria).registrar(eq("reservas"), any(), eq(OperacaoAuditoria.INSERT), eq(null), anyString());
    }

    @Test
    @DisplayName("criar converte RuntimeException em RegraNegocioException")
    void criar_excecaoBancoDados_convertidaEmRegraNegocioException() {
        ProgramacaoViagem prog = new ProgramacaoViagem();
        prog.setValorBase(BigDecimal.TEN);

        when(programacaoRepository.findById(1)).thenReturn(Optional.of(prog));
        when(entityManager.getReference(eq(Passageiro.class), eq(1L))).thenReturn(new Passageiro());
        when(reservaRepository.saveAndFlush(any()))
                .thenThrow(new RuntimeException(new RuntimeException("RN07: Overbooking prevenido.")));

        assertThatThrownBy(() -> service.criar(1, 1L, CanalReserva.ONLINE))
                .isInstanceOf(RegraNegocioException.class);
    }
}
