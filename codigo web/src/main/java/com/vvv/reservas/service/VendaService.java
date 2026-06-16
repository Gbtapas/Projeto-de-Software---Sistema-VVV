package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.*;
import com.vvv.reservas.model.enums.CanalReserva;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.model.enums.StatusAprovacao;
import com.vvv.reservas.model.enums.StatusReserva;
import com.vvv.reservas.model.enums.StatusVenda;
import com.vvv.reservas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vendas presenciais (RF13) e online (RF14 / UC09).
 *
 * - Online: o Gerente Virtual aprova/recusa a reserva online; a trigger
 *   trg_vo_valida_gerente_ins garante que o responsável seja GERENTE_VIRTUAL (RN31).
 * - Presencial: o Funcionário registra a venda em um PDV e confirma manualmente (RN29).
 */
@Service
public class VendaService {

    private final VendaRepository vendaRepo;
    private final VendaOnlineRepository vendaOnlineRepo;
    private final VendaPresencialRepository vendaPresencialRepo;
    private final ReservaRepository reservaRepo;
    private final FuncionarioRepository funcionarioRepo;
    private final PontoDeVendaRepository pontoRepo;
    private final ReservaService reservaService;
    private final AuditoriaService auditoria;
    private final TransferenciaService transferenciaService;

    public VendaService(VendaRepository vendaRepo, VendaOnlineRepository vendaOnlineRepo,
                        VendaPresencialRepository vendaPresencialRepo, ReservaRepository reservaRepo,
                        FuncionarioRepository funcionarioRepo, PontoDeVendaRepository pontoRepo,
                        ReservaService reservaService, AuditoriaService auditoria,
                        TransferenciaService transferenciaService) {
        this.vendaRepo = vendaRepo;
        this.vendaOnlineRepo = vendaOnlineRepo;
        this.vendaPresencialRepo = vendaPresencialRepo;
        this.reservaRepo = reservaRepo;
        this.funcionarioRepo = funcionarioRepo;
        this.pontoRepo = pontoRepo;
        this.reservaService = reservaService;
        this.auditoria = auditoria;
        this.transferenciaService = transferenciaService;
    }

    // ---------- consultas de apoio ----------
    @Transactional(readOnly = true)
    public List<Reserva> reservasOnline() {
        return reservaRepo.findByCanalOrderByDataCriacaoDesc(CanalReserva.ONLINE);
    }

    @Transactional(readOnly = true)
    public Venda vendaDaReserva(Long idReserva) {
        return vendaRepo.findByReserva_Id(idReserva).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PontoDeVenda> pontos() { return pontoRepo.findAllByOrderByNomeAsc(); }

    // ---------- RF14 / UC09: aprovar ou recusar venda online ----------
    @Transactional
    public void supervisionarOnline(Long idReserva, boolean aprovar, String gerenteEmail) {
        Funcionario gerente = funcionarioRepo.findByUsuario_Email(gerenteEmail)
                .orElseThrow(() -> new RegraNegocioException("Gerente não vinculado a um funcionário."));
        Reserva reserva = reservaRepo.findById(idReserva)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));
        if (vendaRepo.findByReserva_Id(idReserva).isPresent()) {
            throw new RegraNegocioException("Esta reserva já foi supervisionada (venda existente).");
        }
        try {
            // Venda base
            Venda venda = new Venda();
            venda.setReserva(reserva);
            venda.setValorTotal(reserva.getValorTotal());
            Venda salva = vendaRepo.saveAndFlush(venda);

            // Detalhe online (trigger valida o tipo do gerente — RN31)
            VendaOnline vo = new VendaOnline();
            vo.setVenda(salva);
            vo.setGerenteVirtual(gerente);
            vendaOnlineRepo.saveAndFlush(vo);

            // Decisão do gerente
            vo.setStatusAprovacao(aprovar ? StatusAprovacao.APROVADA : StatusAprovacao.RECUSADA);
            vo.setDataAprovacao(LocalDateTime.now());
            salva.setStatus(aprovar ? StatusVenda.CONFIRMADA : StatusVenda.CANCELADA);
            vendaOnlineRepo.saveAndFlush(vo);
            vendaRepo.saveAndFlush(salva);

            // Recusa cancela a reserva (a trigger devolve a vaga)
            if (!aprovar) {
                reserva.setStatus(StatusReserva.CANCELADA);
                reservaRepo.saveAndFlush(reserva);
            }

            // RnF06: auditoria
            auditoria.registrar("vendas", salva.getId(), OperacaoAuditoria.INSERT,
                    null, "{\"tipo\":\"ONLINE\",\"status\":\"" + salva.getStatus() + "\",\"reserva\":" + idReserva + "}");

            // RF15: notifica transportadora se venda confirmada
            if (aprovar) {
                Integer idTransportadora = reserva.getProgramacao().getModal().getTransportadora().getId();
                transferenciaService.transferir(idTransportadora);
            }
        } catch (RegraNegocioException e) {
            throw e;
        } catch (RuntimeException e) {
            throw RegraNegocioException.de(e);
        }
    }

    // ---------- RF13: registrar venda presencial ----------
    @Transactional
    public Long registrarPresencial(Integer idProgramacao, Long idPassageiro, Integer idPonto,
                                    String funcionarioEmail) {
        Funcionario func = funcionarioRepo.findByUsuario_Email(funcionarioEmail)
                .orElseThrow(() -> new RegraNegocioException("Funcionário não vinculado ao usuário logado."));
        try {
            // Cria a reserva no canal PRESENCIAL (regras de idade/capacidade via triggers)
            Reserva reserva = reservaService.criar(idProgramacao, idPassageiro, CanalReserva.PRESENCIAL);

            Venda venda = new Venda();
            venda.setReserva(reserva);
            venda.setValorTotal(reserva.getValorTotal());
            Venda salva = vendaRepo.saveAndFlush(venda);

            VendaPresencial vp = new VendaPresencial();
            vp.setVenda(salva);
            vp.setFuncionario(func);
            vp.setPonto(pontoRepo.getReferenceById(idPonto));
            vp.setConfirmadoPor(func);                 // confirmação manual (RN29)
            vp.setDataConfirmacao(LocalDateTime.now());
            vendaPresencialRepo.saveAndFlush(vp);

            salva.setStatus(StatusVenda.CONFIRMADA);
            vendaRepo.saveAndFlush(salva);

            // RnF06: auditoria
            auditoria.registrar("vendas", salva.getId(), OperacaoAuditoria.INSERT,
                    null, "{\"tipo\":\"PRESENCIAL\",\"status\":\"CONFIRMADA\",\"reserva\":" + reserva.getId() + "}");

            // RF15: notifica transportadora
            Integer idTransportadora = reserva.getProgramacao().getModal().getTransportadora().getId();
            transferenciaService.transferir(idTransportadora);

            return reserva.getId();
        } catch (RegraNegocioException e) {
            throw e;
        } catch (RuntimeException e) {
            throw RegraNegocioException.de(e);
        }
    }
}
