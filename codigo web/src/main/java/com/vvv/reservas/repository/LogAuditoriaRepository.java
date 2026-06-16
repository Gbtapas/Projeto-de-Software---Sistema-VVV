package com.vvv.reservas.repository;

import com.vvv.reservas.model.entity.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    List<LogAuditoria> findByTabelaOrderByDataHoraDesc(String tabela);
    boolean existsByTabelaAndIdRegistro(String tabela, Long idRegistro);
}
