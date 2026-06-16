package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.LogAuditoria;
import com.vvv.reservas.repository.LogAuditoriaRepository;
import com.vvv.reservas.repository.ReservaRepository;
import com.vvv.reservas.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** UC10: relatórios operacionais e financeiros. */
@Service
public class RelatorioService {

    private final EntityManager em;
    private final LogAuditoriaRepository logRepo;

    public RelatorioService(EntityManager em, LogAuditoriaRepository logRepo) {
        this.em = em;
        this.logRepo = logRepo;
    }

    /** Reservas agrupadas por dia no período, com total de registros e valor somado. */
    @Transactional(readOnly = true)
    public List<Object[]> reservasPorPeriodo(LocalDate inicio, LocalDate fim) {
        LocalDateTime ini = inicio.atStartOfDay();
        LocalDateTime end = fim.plusDays(1).atStartOfDay();
        return em.createQuery(
                "SELECT CAST(r.dataCriacao AS LocalDate), COUNT(r), SUM(r.valorTotal) " +
                "FROM Reserva r WHERE r.dataCriacao >= :ini AND r.dataCriacao < :end " +
                "GROUP BY CAST(r.dataCriacao AS LocalDate) ORDER BY 1", Object[].class)
                .setParameter("ini", ini)
                .setParameter("end", end)
                .getResultList();
    }

    /** Vendas confirmadas por canal (ONLINE vs PRESENCIAL) no período. */
    @Transactional(readOnly = true)
    public Map<String, Long> vendasPorCanal(LocalDate inicio, LocalDate fim) {
        LocalDateTime ini = inicio.atStartOfDay();
        LocalDateTime end = fim.plusDays(1).atStartOfDay();
        List<Object[]> rows = em.createQuery(
                "SELECT r.canal, COUNT(r) FROM Reserva r " +
                "WHERE r.status = com.vvv.reservas.model.enums.StatusReserva.CONFIRMADA " +
                "AND r.dataCriacao >= :ini AND r.dataCriacao < :end " +
                "GROUP BY r.canal", Object[].class)
                .setParameter("ini", ini)
                .setParameter("end", end)
                .getResultList();
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("ONLINE", 0L);
        result.put("PRESENCIAL", 0L);
        for (Object[] row : rows) {
            result.put(row[0].toString(), (Long) row[1]);
        }
        return result;
    }

    /** Modais com mais reservas confirmadas no período (top 10). */
    @Transactional(readOnly = true)
    public List<Object[]> ocupacaoModais(LocalDate inicio, LocalDate fim) {
        LocalDateTime ini = inicio.atStartOfDay();
        LocalDateTime end = fim.plusDays(1).atStartOfDay();
        return em.createQuery(
                "SELECT m.codigo, m.tipo, m.modelo, COUNT(r) " +
                "FROM Reserva r JOIN r.programacao p JOIN p.modal m " +
                "WHERE r.status = com.vvv.reservas.model.enums.StatusReserva.CONFIRMADA " +
                "AND r.dataCriacao >= :ini AND r.dataCriacao < :end " +
                "GROUP BY m.id, m.codigo, m.tipo, m.modelo ORDER BY COUNT(r) DESC", Object[].class)
                .setParameter("ini", ini)
                .setParameter("end", end)
                .setMaxResults(10)
                .getResultList();
    }

    /** Transferências registradas na log_auditoria por transportadora (texto livre no JSON). */
    @Transactional(readOnly = true)
    public List<LogAuditoria> transferencias() {
        return logRepo.findByTabelaOrderByDataHoraDesc("transferencia_transportadora");
    }
}
