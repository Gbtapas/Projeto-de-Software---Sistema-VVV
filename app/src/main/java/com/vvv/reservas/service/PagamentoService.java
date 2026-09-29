package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.Pagamento;
import com.vvv.reservas.model.entity.PagamentoCredito;
import com.vvv.reservas.model.entity.PagamentoDebito;
import com.vvv.reservas.model.entity.Reserva;
import com.vvv.reservas.model.entity.Ticket;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.model.enums.StatusPagamento;
import com.vvv.reservas.model.enums.TipoPagamento;
import com.vvv.reservas.repository.PagamentoRepository;
import com.vvv.reservas.repository.ReservaRepository;
import com.vvv.reservas.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Processamento de pagamento (UC04 / RF10–RF12) e consulta do ticket emitido (UC05).
 * O cálculo de juros/parcelas (RN19–RN21) é feito por trigger BEFORE INSERT; a
 * confirmação da reserva e a EMISSÃO DO TICKET (RN22/RN23) são feitas por trigger
 * AFTER UPDATE quando o status vira APROVADO. Aqui apenas simulamos a operadora.
 */
@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final ReservaRepository reservaRepository;
    private final TicketRepository ticketRepository;
    private final EntityManager entityManager;
    private final AuditoriaService auditoria;
    private final TransferenciaService transferenciaService;

    public PagamentoService(PagamentoRepository pagamentoRepository,
                            ReservaRepository reservaRepository,
                            TicketRepository ticketRepository,
                            EntityManager entityManager,
                            AuditoriaService auditoria,
                            TransferenciaService transferenciaService) {
        this.pagamentoRepository = pagamentoRepository;
        this.reservaRepository = reservaRepository;
        this.ticketRepository = ticketRepository;
        this.entityManager = entityManager;
        this.auditoria = auditoria;
        this.transferenciaService = transferenciaService;
    }

    /**
     * Cria o pagamento PENDENTE e, simulando a autorização da operadora, o aprova.
     * A aprovação dispara a trigger que confirma a reserva e emite o ticket.
     */
    @Transactional
    public Pagamento processarEAprovar(Long idReserva, TipoPagamento tipo, Integer parcelas) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RegraNegocioException("Reserva não encontrada."));

        if (pagamentoRepository.findByReserva_Id(idReserva).isPresent()) {
            throw new RegraNegocioException("Esta reserva já possui um pagamento (RN22: 1 pagamento por reserva).");
        }

        try {
            // Generalização: instancia o subtipo concreto conforme a forma escolhida.
            Pagamento pag = switch (tipo) {
                case CREDITO -> new PagamentoCredito();
                case DEBITO -> new PagamentoDebito();
            };
            pag.setReserva(reserva);
            pag.setParcelas(tipo == TipoPagamento.DEBITO ? 1 : parcelas); // RN19
            pag.setValorBruto(reserva.getValorTotal()); // valor da reserva já com desconto

            // INSERT: trigger calcula valor_juros, valor_total e valor_parcela
            Pagamento salvo = pagamentoRepository.saveAndFlush(pag);
            entityManager.refresh(salvo);

            // UPDATE para APROVADO: trigger confirma a reserva e emite o ticket
            salvo.setStatus(StatusPagamento.APROVADO);
            salvo.setCodigoAutorizacao("AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            salvo.setDataPagamento(LocalDateTime.now());
            pagamentoRepository.saveAndFlush(salvo);
            entityManager.refresh(salvo);

            // RnF06: auditoria do pagamento aprovado
            auditoria.registrar("pagamentos", salvo.getId(), OperacaoAuditoria.UPDATE,
                    "{\"status\":\"PENDENTE\"}",
                    "{\"status\":\"APROVADO\",\"autorizacao\":\"" + salvo.getCodigoAutorizacao() + "\"}");

            // RF15: notifica transportadora automaticamente após confirmação
            Integer idTransportadora = reserva.getProgramacao().getModal().getTransportadora().getId();
            transferenciaService.transferir(idTransportadora);

            return salvo;
        } catch (RegraNegocioException e) {
            throw e;
        } catch (RuntimeException e) {
            throw RegraNegocioException.de(e);
        }
    }

    @Transactional(readOnly = true)
    public Pagamento buscarPagamentoDaReserva(Long idReserva) {
        return pagamentoRepository.findByReserva_Id(idReserva).orElse(null);
    }

    @Transactional(readOnly = true)
    public Ticket buscarTicketDaReserva(Long idReserva) {
        return ticketRepository.findByReserva_Id(idReserva)
                .orElseThrow(() -> new RegraNegocioException("Ticket ainda não emitido para esta reserva."));
    }

    @Transactional(readOnly = true)
    public Ticket buscarTicket(Long idTicket) {
        return ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RegraNegocioException("Ticket não encontrado."));
    }
}
