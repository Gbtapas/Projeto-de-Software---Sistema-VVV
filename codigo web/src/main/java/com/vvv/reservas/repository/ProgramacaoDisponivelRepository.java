package com.vvv.reservas.repository;

import com.vvv.reservas.model.view.ProgramacaoDisponivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/** Consulta de viagens disponíveis (UC02) sobre a view vw_programacoes_disponiveis. */
public interface ProgramacaoDisponivelRepository extends JpaRepository<ProgramacaoDisponivel, Integer> {

    @Query(value = """
            SELECT * FROM vw_programacoes_disponiveis
            WHERE (:origem  IS NULL OR cod_origem  = :origem)
              AND (:destino IS NULL OR cod_destino = :destino)
              AND (:data    IS NULL OR data_viagem = :data)
            ORDER BY data_viagem ASC, valor_base ASC
            """, nativeQuery = true)
    List<ProgramacaoDisponivel> buscar(@Param("origem") String origem,
                                       @Param("destino") String destino,
                                       @Param("data") LocalDate data);
}
