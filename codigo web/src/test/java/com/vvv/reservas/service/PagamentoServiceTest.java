package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.Modal;
import com.vvv.reservas.model.entity.Pagamento;
import com.vvv.reservas.model.entity.PagamentoCredito;
import com.vvv.reservas.model.entity.PagamentoDebito;
import com.vvv.reservas.model.entity.ProgramacaoViagem;
import com.vvv.reservas.model.entity.Reserva;
import com.vvv.reservas.model.entity.Ticket;
import com.vvv.reservas.model.entity.Transportadora;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.model.enums.TipoPagamento;
import com.vvv.reservas.repository.PagamentoRepository;
import com.vvv.reservas.repository.ReservaRepository;
import com.vvv.reservas.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock PagamentoRepository pagamentoRepository;
    @Mock ReservaRepository reservaRepository;
    @Mock TicketRepository ticketRepository;
    @Mock EntityManager entityManager;
    @Mock AuditoriaService auditoria;
    @Mock TransferenciaService transferenciaService;
    @InjectMocks PagamentoService service;

    // ------------------------------------------------------------------ processarEAprovar

    @Test
    @DisplayName("processarEAprovar lança exception quando reserva não existe")
    void processarEAprovar_reservaNaoEncontrada_lancaException() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processarEAprovar(99L, TipoPagamento.DEBITO, 1))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Reserva não encontrada");
    }

    @Test
    @DisplayName("processarEAprovar lança exception quando já existe pagamento para a reserva (RN22)")
    void processarEAprovar_pagamentoJaExistente_lancaException() {
        Reserva reserva = reservaComValorTotal(new BigDecimal("300.00"));
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(pagamentoRepository.findByReserva_Id(1L)).thenReturn(Optional.of(new PagamentoDebito()));

        assertThatThrownBy(() -> service.processarEAprovar(1L, TipoPagamento.DEBITO, 1))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("RN22");
    }

    @Test
    @DisplayName("processarEAprovar com DÉBITO força parcelas = 1 independente do valor informado (RN19)")
    void processarEAprovar_debito_forcaParcelas1() {
        Reserva reserva = reservaComValorTotal(new BigDecimal("500.00"));
        PagamentoDebito pagamento = new PagamentoDebito();
        pagamento.setValorBruto(new BigDecimal("500.00"));

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(pagamentoRepository.findByReserva_Id(1L)).thenReturn(Optional.empty());
        when(pagamentoRepository.saveAndFlush(any())).thenReturn(pagamento);
        doNothing().when(entityManager).refresh(any());
        doNothing().when(transferenciaService).transferir(any());

        service.processarEAprovar(1L, TipoPagamento.DEBITO, 4);

        org.mockito.ArgumentCaptor<Pagamento> captor =
                org.mockito.ArgumentCaptor.forClass(Pagamento.class);
        // saveAndFlush é chamado 2x: INSERT e depois UPDATE para APROVADO
        verify(pagamentoRepository, org.mockito.Mockito.times(2)).saveAndFlush(captor.capture());
        assertThat(captor.getAllValues().get(0).getParcelas()).isEqualTo(1);
    }

    @Test
    @DisplayName("processarEAprovar com CRÉDITO instancia PagamentoCredito e registra auditoria")
    void processarEAprovar_credito_salvaEAudita() {
        Reserva reserva = reservaComValorTotal(new BigDecimal("800.00"));
        PagamentoCredito pagamento = new PagamentoCredito();
        pagamento.setValorBruto(new BigDecimal("800.00"));

        when(reservaRepository.findById(2L)).thenReturn(Optional.of(reserva));
        when(pagamentoRepository.findByReserva_Id(2L)).thenReturn(Optional.empty());
        when(pagamentoRepository.saveAndFlush(any())).thenReturn(pagamento);
        doNothing().when(entityManager).refresh(any());
        doNothing().when(transferenciaService).transferir(any());

        Pagamento resultado = service.processarEAprovar(2L, TipoPagamento.CREDITO, 2);

        assertThat(resultado).isInstanceOf(PagamentoCredito.class);
        verify(auditoria).registrar(eq("pagamentos"), any(), eq(OperacaoAuditoria.UPDATE), anyString(), anyString());
    }

    // ------------------------------------------------------------------ buscarPagamentoDaReserva

    @Test
    @DisplayName("buscarPagamentoDaReserva retorna null quando não há pagamento")
    void buscarPagamentoDaReserva_semPagamento_retornaNull() {
        when(pagamentoRepository.findByReserva_Id(1L)).thenReturn(Optional.empty());

        Pagamento resultado = service.buscarPagamentoDaReserva(1L);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("buscarPagamentoDaReserva retorna o pagamento existente")
    void buscarPagamentoDaReserva_comPagamento_retornaPagamento() {
        PagamentoCredito pag = new PagamentoCredito();
        when(pagamentoRepository.findByReserva_Id(1L)).thenReturn(Optional.of(pag));

        Pagamento resultado = service.buscarPagamentoDaReserva(1L);

        assertThat(resultado).isSameAs(pag);
    }

    // ------------------------------------------------------------------ buscarTicketDaReserva

    @Test
    @DisplayName("buscarTicketDaReserva lança exception quando ticket ainda não foi emitido")
    void buscarTicketDaReserva_naoEmitido_lancaException() {
        when(ticketRepository.findByReserva_Id(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarTicketDaReserva(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Ticket");
    }

    @Test
    @DisplayName("buscarTicketDaReserva retorna o ticket quando emitido")
    void buscarTicketDaReserva_emitido_retornaTicket() {
        Ticket ticket = new Ticket();
        when(ticketRepository.findByReserva_Id(5L)).thenReturn(Optional.of(ticket));

        Ticket resultado = service.buscarTicketDaReserva(5L);

        assertThat(resultado).isSameAs(ticket);
    }

    // ------------------------------------------------------------------ buscarTicket

    @Test
    @DisplayName("buscarTicket lança exception quando id de ticket não existe")
    void buscarTicket_naoEncontrado_lancaException() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarTicket(99L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Ticket");
    }

    @Test
    @DisplayName("buscarTicket retorna o ticket pelo id")
    void buscarTicket_encontrado_retornaTicket() {
        Ticket ticket = new Ticket();
        when(ticketRepository.findById(7L)).thenReturn(Optional.of(ticket));

        Ticket resultado = service.buscarTicket(7L);

        assertThat(resultado).isSameAs(ticket);
    }

    // ------------------------------------------------------------------ helpers

    private Reserva reservaComValorTotal(BigDecimal valor) {
        Transportadora transportadora = new Transportadora();

        Modal modal = new Modal();
        modal.setTransportadora(transportadora);

        ProgramacaoViagem prog = new ProgramacaoViagem();
        prog.setModal(modal);
        prog.setValorBase(valor);

        Reserva reserva = new Reserva();
        reserva.setProgramacao(prog);
        // valor_total é inserável=false, simulamos via reflexão ou deixamos null
        // o service usa reserva.getValorTotal() que retorna null nesse caso
        return reserva;
    }
}
