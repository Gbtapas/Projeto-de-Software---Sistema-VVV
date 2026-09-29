package com.vvv.reservas;

import com.vvv.reservas.service.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base de testes (parte 5 — Testes). Estes testes são unitários puros e NÃO exigem
 * o MySQL: validam a tradução das mensagens de regra de negócio lançadas pelas
 * triggers do banco (SIGNAL SQLSTATE '45000' com textos "RNxx").
 *
 * Para testes de integração end-to-end (contexto Spring + banco real), basta criar
 * uma classe anotada com @SpringBootTest com o banco 'vvv' disponível.
 */
class ReservasApplicationTests {

    @Test
    @DisplayName("Extrai a mensagem da regra RN04 de uma cadeia de exceções de trigger")
    void extraiMensagemDeReGraDeNegocio() {
        // se passar isso o resto vai de boa
        Throwable raiz = new SQLException(
                "RN04: Passageiros entre 2 e 10 anos precisam de acompanhante cadastrado no sistema.");
        Throwable wrapper = new RuntimeException("could not execute statement", raiz);

        RegraNegocioException ex = RegraNegocioException.de(wrapper);

        assertTrue(ex.getMessage().startsWith("RN04"),
                "A mensagem deveria começar pelo código da regra de negócio");
    }

    @Test
    @DisplayName("Detecta prevenção de overbooking (RI01/RN07) na cadeia de exceções")
    void detectaOverbooking() {
        // garantindo a logica de negocio
        Throwable raiz = new SQLException("RI01/RN07: Não há vagas disponíveis. Overbooking prevenido.");
        RegraNegocioException ex = RegraNegocioException.de(new RuntimeException(raiz));
        assertTrue(ex.getMessage().contains("Overbooking") || ex.getMessage().startsWith("RI"));
    }
}
