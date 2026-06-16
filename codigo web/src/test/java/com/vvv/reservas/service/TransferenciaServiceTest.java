package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.LogAuditoria;
import com.vvv.reservas.model.entity.Modal;
import com.vvv.reservas.model.entity.ProgramacaoViagem;
import com.vvv.reservas.model.entity.Reserva;
import com.vvv.reservas.model.entity.Transportadora;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.model.enums.StatusReserva;
import com.vvv.reservas.repository.LogAuditoriaRepository;
import com.vvv.reservas.repository.ReservaRepository;
import com.vvv.reservas.repository.TransportadoraRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock ReservaRepository reservaRepo;
    @Mock TransportadoraRepository transportadoraRepo;
    @Mock LogAuditoriaRepository logRepo;
    @Mock AuditoriaService auditoria;
    @InjectMocks TransferenciaService service;

    // ------------------------------------------------------------------ historico

    @Test
    @DisplayName("historico delega ao repositório pela tabela transferencia_transportadora")
    void historico_delegaAoRepositorio() {
        LogAuditoria log = new LogAuditoria();
        when(logRepo.findByTabelaOrderByDataHoraDesc("transferencia_transportadora"))
                .thenReturn(List.of(log));

        List<LogAuditoria> resultado = service.historico();

        assertThat(resultado).hasSize(1);
        verify(logRepo).findByTabelaOrderByDataHoraDesc("transferencia_transportadora");
    }

    // ------------------------------------------------------------------ transferir

    @Test
    @DisplayName("transferir não registra auditoria quando não há reservas confirmadas para a transportadora")
    void transferir_semReservas_naoRegistraAuditoria() {
        when(reservaRepo.findByStatusOrderByDataCriacaoDesc(StatusReserva.CONFIRMADA))
                .thenReturn(List.of());

        service.transferir(1);

        verify(auditoria, never()).registrar(anyString(), anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("transferir registra auditoria com dados das reservas da transportadora")
    void transferir_comReservas_registraAuditoria() {
        Reserva r = reservaParaTransportadora(1);
        when(reservaRepo.findByStatusOrderByDataCriacaoDesc(StatusReserva.CONFIRMADA))
                .thenReturn(List.of(r));

        service.transferir(1);

        verify(auditoria).registrar(
                eq("transferencia_transportadora"),
                eq(1L),
                eq(OperacaoAuditoria.INSERT),
                eq(null),
                anyString());
    }

    @Test
    @DisplayName("transferir ignora reservas de outras transportadoras")
    void transferir_reservasDeOutraTransportadora_naoRegistra() {
        Reserva rDeOutra = reservaParaTransportadora(99);
        when(reservaRepo.findByStatusOrderByDataCriacaoDesc(StatusReserva.CONFIRMADA))
                .thenReturn(List.of(rDeOutra));

        service.transferir(1); // buscando pela transportadora 1, mas a reserva é da 99

        verify(auditoria, never()).registrar(anyString(), anyLong(), any(), any(), any());
    }

    // ------------------------------------------------------------------ notificarManutencao

    @Test
    @DisplayName("notificarManutencao registra auditoria com tipo MANUTENCAO")
    void notificarManutencao_registraAuditoria() {
        service.notificarManutencao(5, "Substituição de peças");

        verify(auditoria).registrar(
                eq("transferencia_transportadora"),
                eq(5L),
                eq(OperacaoAuditoria.INSERT),
                eq(null),
                anyString());
    }

    // ------------------------------------------------------------------ listarPendentes

    @Test
    @DisplayName("listarPendentes filtra reservas já transferidas e agrupa por transportadora")
    void listarPendentes_filtraJaTransferidas() {
        Reserva pendente = reservaParaTransportadora(1);
        Reserva jaTransferida = reservaParaTransportadora(2);

        when(reservaRepo.findByStatusOrderByDataCriacaoDesc(StatusReserva.CONFIRMADA))
                .thenReturn(List.of(pendente, jaTransferida));
        when(logRepo.existsByTabelaAndIdRegistro("transferencia_transportadora", 1L))
                .thenReturn(false);
        when(logRepo.existsByTabelaAndIdRegistro("transferencia_transportadora", 2L))
                .thenReturn(true);

        var resultado = service.listarPendentes();

        assertThat(resultado).hasSize(1);
    }

    // ------------------------------------------------------------------ helper

    private Reserva reservaParaTransportadora(Integer idTransportadora) {
        Transportadora t = new Transportadora();
        // id é gerado pelo banco, mas para testes de filtragem basta o getId() retornar null
        // aqui usamos reflexão para definir o id
        try {
            java.lang.reflect.Field f = Transportadora.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(t, idTransportadora);
        } catch (Exception ignored) {
        }

        Modal modal = new Modal();
        modal.setTransportadora(t);

        ProgramacaoViagem prog = new ProgramacaoViagem();
        prog.setModal(modal);

        Reserva r = new Reserva();
        r.setProgramacao(prog);
        r.setStatus(StatusReserva.CONFIRMADA);
        return r;
    }
}
