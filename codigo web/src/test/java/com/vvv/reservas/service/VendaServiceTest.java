package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.Funcionario;
import com.vvv.reservas.model.entity.Modal;
import com.vvv.reservas.model.entity.PontoDeVenda;
import com.vvv.reservas.model.entity.ProgramacaoViagem;
import com.vvv.reservas.model.entity.Reserva;
import com.vvv.reservas.model.entity.Transportadora;
import com.vvv.reservas.model.entity.Venda;
import com.vvv.reservas.model.entity.VendaOnline;
import com.vvv.reservas.model.enums.CanalReserva;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.model.enums.StatusReserva;
import com.vvv.reservas.model.enums.StatusVenda;
import com.vvv.reservas.repository.FuncionarioRepository;
import com.vvv.reservas.repository.PontoDeVendaRepository;
import com.vvv.reservas.repository.ReservaRepository;
import com.vvv.reservas.repository.VendaOnlineRepository;
import com.vvv.reservas.repository.VendaPresencialRepository;
import com.vvv.reservas.repository.VendaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock VendaRepository vendaRepo;
    @Mock VendaOnlineRepository vendaOnlineRepo;
    @Mock VendaPresencialRepository vendaPresencialRepo;
    @Mock ReservaRepository reservaRepo;
    @Mock FuncionarioRepository funcionarioRepo;
    @Mock PontoDeVendaRepository pontoRepo;
    @Mock ReservaService reservaService;
    @Mock AuditoriaService auditoria;
    @Mock TransferenciaService transferenciaService;
    @InjectMocks VendaService service;

    // ------------------------------------------------------------------ reservasOnline

    @Test
    @DisplayName("reservasOnline filtra pelo canal ONLINE")
    void reservasOnline_delegaAoRepositorio() {
        // garantindo que nao vai dar erro aqui
        when(reservaRepo.findByCanalOrderByDataCriacaoDesc(CanalReserva.ONLINE)).thenReturn(List.of());

        List<Reserva> resultado = service.reservasOnline();

        assertThat(resultado).isEmpty();
        verify(reservaRepo).findByCanalOrderByDataCriacaoDesc(CanalReserva.ONLINE);
    }

    // ------------------------------------------------------------------ vendaDaReserva

    @Test
    @DisplayName("vendaDaReserva retorna null quando venda não existe")
    void vendaDaReserva_naoExiste_retornaNull() {
        // mais uma checagem de rotina
        when(vendaRepo.findByReserva_Id(1L)).thenReturn(Optional.empty());

        Venda resultado = service.vendaDaReserva(1L);

        assertThat(resultado).isNull();
    }

    // ------------------------------------------------------------------ pontos

    @Test
    @DisplayName("pontos retorna todos os PDVs ordenados por nome")
    void pontos_delegaAoRepositorio() {
        // garantindo que nao vai dar erro aqui
        when(pontoRepo.findAllByOrderByNomeAsc()).thenReturn(List.of(new PontoDeVenda()));

        List<PontoDeVenda> resultado = service.pontos();

        assertThat(resultado).hasSize(1);
    }

    // ------------------------------------------------------------------ supervisionarOnline

    @Test
    @DisplayName("supervisionarOnline lança exception quando gerente não está vinculado")
    void supervisionarOnline_gerenteNaoEncontrado_lancaException() {
        // garantindo a logica de negocio
        when(funcionarioRepo.findByUsuario_Email("gerente@vvv.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.supervisionarOnline(1L, true, "gerente@vvv.com"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Gerente");
    }

    @Test
    @DisplayName("supervisionarOnline lança exception quando reserva não existe")
    void supervisionarOnline_reservaNaoEncontrada_lancaException() {
        // verificando se ta tudo certo
        when(funcionarioRepo.findByUsuario_Email("g@vvv.com")).thenReturn(Optional.of(new Funcionario()));
        when(reservaRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.supervisionarOnline(99L, true, "g@vvv.com"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Reserva");
    }

    @Test
    @DisplayName("supervisionarOnline lança exception quando reserva já tem venda")
    void supervisionarOnline_vendaJaExistente_lancaException() {
        // so pra ter certeza que ta pegando o valor certo
        Funcionario gerente = new Funcionario();
        Reserva reserva = new Reserva();

        when(funcionarioRepo.findByUsuario_Email("g@vvv.com")).thenReturn(Optional.of(gerente));
        when(reservaRepo.findById(1L)).thenReturn(Optional.of(reserva));
        when(vendaRepo.findByReserva_Id(1L)).thenReturn(Optional.of(new Venda()));

        assertThatThrownBy(() -> service.supervisionarOnline(1L, true, "g@vvv.com"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("supervisionada");
    }

    @Test
    @DisplayName("supervisionarOnline com aprovação cria VendaOnline confirmada e audita")
    void supervisionarOnline_aprovar_criaVendaConfirmadaEAudita() {
        // mais uma checagem de rotina
        Funcionario gerente = new Funcionario();
        Reserva reserva = reservaComTransportadora();
        Venda vendaSalva = new Venda();
        VendaOnline voSalva = new VendaOnline();

        when(funcionarioRepo.findByUsuario_Email("g@vvv.com")).thenReturn(Optional.of(gerente));
        when(reservaRepo.findById(1L)).thenReturn(Optional.of(reserva));
        when(vendaRepo.findByReserva_Id(1L)).thenReturn(Optional.empty());
        when(vendaRepo.saveAndFlush(any())).thenReturn(vendaSalva);
        when(vendaOnlineRepo.saveAndFlush(any())).thenReturn(voSalva);

        service.supervisionarOnline(1L, true, "g@vvv.com");

        verify(auditoria).registrar(eq("vendas"), any(), eq(OperacaoAuditoria.INSERT), eq(null), anyString());
        verify(transferenciaService).transferir(any());
    }

    @Test
    @DisplayName("supervisionarOnline com recusa cancela a reserva e não chama transferir")
    void supervisionarOnline_recusar_cancelaReservaENaoTransfere() {
        // verificando se ta tudo certo
        Funcionario gerente = new Funcionario();
        Reserva reserva = reservaComTransportadora();
        Venda vendaSalva = new Venda();
        VendaOnline voSalva = new VendaOnline();

        when(funcionarioRepo.findByUsuario_Email("g@vvv.com")).thenReturn(Optional.of(gerente));
        when(reservaRepo.findById(2L)).thenReturn(Optional.of(reserva));
        when(vendaRepo.findByReserva_Id(2L)).thenReturn(Optional.empty());
        when(vendaRepo.saveAndFlush(any())).thenReturn(vendaSalva);
        when(vendaOnlineRepo.saveAndFlush(any())).thenReturn(voSalva);
        when(reservaRepo.saveAndFlush(any())).thenReturn(reserva);

        service.supervisionarOnline(2L, false, "g@vvv.com");

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        verify(transferenciaService, never()).transferir(any());
    }

    // ------------------------------------------------------------------ registrarPresencial

    @Test
    @DisplayName("registrarPresencial lança exception quando funcionário não vinculado")
    void registrarPresencial_funcionarioNaoEncontrado_lancaException() {
        // bora testar esse cenario
        when(funcionarioRepo.findByUsuario_Email("func@vvv.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarPresencial(1, 1L, null, 1, "func@vvv.com"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Funcionário");
    }

    @Test
    @DisplayName("registrarPresencial cria reserva, venda presencial e retorna id da reserva")
    void registrarPresencial_sucesso_retornaIdReserva() {
        // garantindo que nao vai dar erro aqui
        Funcionario func = new Funcionario();
        Reserva reserva = reservaComTransportadora();
        Venda vendaSalva = new Venda();

        when(funcionarioRepo.findByUsuario_Email("func@vvv.com")).thenReturn(Optional.of(func));
        when(reservaService.criar(1, 1L, null, CanalReserva.PRESENCIAL)).thenReturn(reserva);
        when(vendaRepo.saveAndFlush(any())).thenReturn(vendaSalva);
        when(pontoRepo.getReferenceById(1)).thenReturn(new PontoDeVenda());
        when(vendaPresencialRepo.saveAndFlush(any())).thenReturn(new com.vvv.reservas.model.entity.VendaPresencial());

        service.registrarPresencial(1, 1L, null, 1, "func@vvv.com");

        verify(reservaService).criar(eq(1), eq(1L), isNull(), eq(CanalReserva.PRESENCIAL));
        verify(auditoria).registrar(eq("vendas"), any(), eq(OperacaoAuditoria.INSERT), eq(null), anyString());
        verify(transferenciaService).transferir(any());
    }

    // ------------------------------------------------------------------ helpers

    private Reserva reservaComTransportadora() {
        Transportadora t = new Transportadora();
        Modal modal = new Modal();
        modal.setTransportadora(t);
        ProgramacaoViagem prog = new ProgramacaoViagem();
        prog.setModal(modal);
        Reserva r = new Reserva();
        r.setProgramacao(prog);
        return r;
    }
}
