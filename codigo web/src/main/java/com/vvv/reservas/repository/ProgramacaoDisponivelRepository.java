package com.vvv.reservas.repository;

import com.vvv.reservas.model.view.ProgramacaoDisponivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/** Consulta de viagens disponíveis (UC02) sobre a view vw_programacoes_disponiveis. */
public interface ProgramacaoDisponivelRepository extends JpaRepository<ProgramacaoDisponivel, Integer> {

    @Query("""
            SELECT p FROM ProgramacaoDisponivel p
            WHERE (:origem  IS NULL OR p.codOrigem  = :origem)
              AND (:destino IS NULL OR p.codDestino = :destino)
              AND (:data    IS NULL OR p.dataViagem = :data)
            ORDER BY p.dataViagem ASC, p.valorBase ASC
            """)
    List<ProgramacaoDisponivel> buscar(@Param("origem") String origem,
                                       @Param("destino") String destino,
                                       @Param("data") LocalDate data);
}
