package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.Passageiro;
import com.vvv.reservas.model.entity.ProgramacaoViagem;
import com.vvv.reservas.model.entity.Reserva;
import com.vvv.reservas.model.enums.CanalReserva;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.model.enums.StatusReserva;
import com.vvv.reservas.repository.ProgramacaoViagemRepository;
import com.vvv.reservas.repository.ReservaRepository;
import com.vvv.reservas.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Criação de reservas (UC03 / RF07). A regra de negócio (idade, acompanhante,
 * capacidade, desconto, geração de código e decremento de vagas) é toda feita por
 * triggers no banco. Aqui apenas montamos a linha e, após salvar, recarregamos a
 * entidade para enxergar os valores calculados pelas triggers.
 */
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ProgramacaoViagemRepository programacaoRepository;
    private final EntityManager entityManager;
    private final AuditoriaService auditoria;
    private final TicketRepository ticketRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          ProgramacaoViagemRepository programacaoRepository,
                          EntityManager entityManager,
                          AuditoriaService auditoria,
                          TicketRepository ticketRepository) {
        this.reservaRepository = reservaRepository;
        this.programacaoRepository = programacaoRepository;
        this.entityManager = entityManager;
        this.auditoria = auditoria;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public java.util.List<Reserva> listarPorUsuario(String email) {
        return reservaRepository.findMinhasReservas(email);
    }

    @Transactional(readOnly = true)
    public Reserva buscar(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));
    }

    /** UC21: cancela reserva se não tiver ticket emitido e o status permitir. */
    @Transactional
    public void cancelar(Long id, String emailUsuario) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        if (!reservaRepository.existsByIdAndPassageiro_Cliente_Usuario_Email(id, emailUsuario))
            throw new RegraNegocioException("Você não tem permissão para cancelar esta reserva.");

        if (reserva.getStatus() == StatusReserva.CANCELADA)
            throw new RegraNegocioException("Esta reserva já está cancelada.");

        if (ticketRepository.findByReserva_Id(id).isPresent())
            throw new RegraNegocioException("Não é possível cancelar: ticket já emitido para esta reserva.");

        String statusAnterior = reserva.getStatus().name();
        try {
            reserva.setStatus(StatusReserva.CANCELADA);
            reservaRepository.saveAndFlush(reserva);
        } catch (RuntimeException e) {
            throw RegraNegocioException.de(e);
        }
        auditoria.registrar("reservas", id, OperacaoAuditoria.DELETE,
                "{\"status\":\"" + statusAnterior + "\"}", null);
    }

    @Transactional
    public Reserva criar(Integer idProgramacao, Long idPassageiro, CanalReserva canal) {
        ProgramacaoViagem prog = programacaoRepository.findById(idProgramacao)
                .orElseThrow(() -> new RegraNegocioException("Viagem (programação) não encontrada."));

        Reserva reserva = new Reserva();
        reserva.setCanal(canal);
        reserva.setValorBruto(prog.getValorBase());
        reserva.setProgramacao(prog);
        reserva.setPassageiro(entityManager.getReference(Passageiro.class, idPassageiro));

        try {
            Reserva salva = reservaRepository.saveAndFlush(reserva);
            // As triggers preencheram codigo, valor_desconto, valor_total e status.
            entityManager.refresh(salva);
            auditoria.registrar("reservas", salva.getId(), OperacaoAuditoria.INSERT,
                    null, "{\"codigo\":\"" + salva.getCodigo() + "\",\"canal\":\"" + canal + "\",\"programacao\":" + idProgramacao + "}");
            return salva;
        } catch (RuntimeException e) {
            throw RegraNegocioException.de(e);
        }
    }
}
