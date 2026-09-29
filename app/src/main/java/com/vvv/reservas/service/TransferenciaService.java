package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.LogAuditoria;
import com.vvv.reservas.model.entity.Reserva;
import com.vvv.reservas.model.entity.Transportadora;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.model.enums.StatusReserva;
import com.vvv.reservas.repository.LogAuditoriaRepository;
import com.vvv.reservas.repository.ReservaRepository;
import com.vvv.reservas.repository.TransportadoraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Transferência de informações de vendas e manutenções para as transportadoras — RF15.
 * A "transferência" é simulada via registro em log_auditoria (sem API externa real).
 */
@Service
public class TransferenciaService {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaService.class);
    private static final String TABELA_TRANSFERENCIA = "transferencia_transportadora";

    private final ReservaRepository reservaRepo;
    private final TransportadoraRepository transportadoraRepo;
    private final LogAuditoriaRepository logRepo;
    private final AuditoriaService auditoria;

    public TransferenciaService(ReservaRepository reservaRepo, TransportadoraRepository transportadoraRepo,
                                 LogAuditoriaRepository logRepo, AuditoriaService auditoria) {
        this.reservaRepo = reservaRepo;
        this.transportadoraRepo = transportadoraRepo;
        this.logRepo = logRepo;
        this.auditoria = auditoria;
    }

    /**
     * Retorna mapa de transportadora → lista de reservas confirmadas pendentes de transferência.
     */
    @Transactional(readOnly = true)
    public Map<Transportadora, List<Reserva>> listarPendentes() {
        List<Reserva> confirmadas = reservaRepo.findByStatusOrderByDataCriacaoDesc(StatusReserva.CONFIRMADA);

        return confirmadas.stream()
                .filter(r -> !logRepo.existsByTabelaAndIdRegistro(
                        TABELA_TRANSFERENCIA,
                        Long.valueOf(r.getProgramacao().getModal().getTransportadora().getId())))
                .collect(Collectors.groupingBy(
                        r -> r.getProgramacao().getModal().getTransportadora(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * Retorna histórico das últimas transferências realizadas.
     */
    @Transactional(readOnly = true)
    public List<LogAuditoria> historico() {
        return logRepo.findByTabelaOrderByDataHoraDesc(TABELA_TRANSFERENCIA);
    }

    /**
     * Registra a transferência de dados de uma transportadora.
     * Chamado automaticamente após confirmação de venda (RF15: "automaticamente").
     */
    @Transactional
    public void transferir(Integer idTransportadora) {
        List<Reserva> reservas = reservaRepo.findByStatusOrderByDataCriacaoDesc(StatusReserva.CONFIRMADA)
                .stream()
                .filter(r -> r.getProgramacao().getModal().getTransportadora().getId().equals(idTransportadora))
                .collect(Collectors.toList());

        if (reservas.isEmpty()) return;

        String dados = "{\"idTransportadora\":" + idTransportadora
                + ",\"totalReservas\":" + reservas.size()
                + ",\"codigos\":[" + reservas.stream()
                        .map(r -> "\"" + r.getCodigo() + "\"")
                        .collect(Collectors.joining(",")) + "]}";

        auditoria.registrar(TABELA_TRANSFERENCIA, Long.valueOf(idTransportadora),
                OperacaoAuditoria.INSERT, null, dados);

        log.info("RF15: dados transferidos para transportadora {} ({} reservas)", idTransportadora, reservas.size());
    }

    /**
     * Notifica transportadora sobre nova manutenção agendada.
     */
    @Transactional
    public void notificarManutencao(Integer idTransportadora, String descricao) {
        String dados = "{\"tipo\":\"MANUTENCAO\",\"idTransportadora\":" + idTransportadora
                + ",\"descricao\":\"" + descricao + "\"}";
        auditoria.registrar(TABELA_TRANSFERENCIA, Long.valueOf(idTransportadora),
                OperacaoAuditoria.INSERT, null, dados);
        log.info("RF15: manutenção notificada à transportadora {}", idTransportadora);
    }
}
