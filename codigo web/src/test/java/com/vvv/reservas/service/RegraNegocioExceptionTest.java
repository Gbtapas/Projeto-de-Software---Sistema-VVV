package com.vvv.reservas.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class RegraNegocioExceptionTest {

    @Test
    @DisplayName("construtor simples preserva a mensagem recebida")
    void construtor_preservaMensagem() {
        // conferindo os valores retornados
        RegraNegocioException ex = new RegraNegocioException("Erro de negócio");

        assertThat(ex.getMessage()).isEqualTo("Erro de negócio");
    }

    @Test
    @DisplayName("de() extrai mensagem com prefixo RN da causa raiz")
    void de_extraiMensagemComPrefixoRN() {
        // so pra ter certeza que ta pegando o valor certo
        Throwable causa = new SQLException("RN04: Passageiros entre 2 e 10 anos precisam de acompanhante.");
        Throwable wrapper = new RuntimeException("could not execute statement", causa);

        RegraNegocioException ex = RegraNegocioException.de(wrapper);

        assertThat(ex.getMessage()).startsWith("RN04");
    }

    @Test
    @DisplayName("de() extrai mensagem com prefixo RI da causa raiz")
    void de_extraiMensagemComPrefixoRI() {
        // garantindo a logica de negocio
        Throwable causa = new SQLException("RI01/RN07: Não há vagas disponíveis. Overbooking prevenido.");
        RegraNegocioException ex = RegraNegocioException.de(new RuntimeException(causa));

        assertThat(ex.getMessage()).satisfiesAnyOf(
                msg -> assertThat(msg).contains("Overbooking"),
                msg -> assertThat(msg).startsWith("RI"));
    }

    @Test
    @DisplayName("de() usa mensagem de fallback quando nenhuma regra é identificada")
    void de_semMensagemReconhecida_usaFallback() {
        // mais uma checagem de rotina
        Throwable ex = new RuntimeException("foreign key constraint fails");

        RegraNegocioException resultado = RegraNegocioException.de(ex);

        assertThat(resultado.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("de() remove prefixo técnico do driver, mantendo apenas o texto da regra")
    void de_removePrefixoTecnicoDoDriver() {
        // garantindo que nao vai dar erro aqui
        String mensagemDriver = "com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: RN05: Passageiro menor de 2 anos.";
        Throwable causa = new SQLException(mensagemDriver);

        RegraNegocioException ex = RegraNegocioException.de(new RuntimeException(causa));

        assertThat(ex.getMessage()).startsWith("RN05");
    }

    @Test
    @DisplayName("de() percorre toda a cadeia de causas até encontrar a mensagem RN")
    void de_percorreCadeiaCompleta() {
        // bora testar esse cenario
        Throwable nivel3 = new SQLException("RN08: Regra de desconto aplicada.");
        Throwable nivel2 = new RuntimeException("constraint violation", nivel3);
        Throwable nivel1 = new RuntimeException("transaction rolled back", nivel2);

        RegraNegocioException ex = RegraNegocioException.de(nivel1);

        assertThat(ex.getMessage()).startsWith("RN08");
    }

    @Test
    @DisplayName("de() detecta Overbooking mesmo sem prefixo RN/RI")
    void de_detectaOverbooking() {
        // se passar isso o resto vai de boa
        Throwable causa = new RuntimeException("Overbooking prevenido.");

        RegraNegocioException ex = RegraNegocioException.de(causa);

        assertThat(ex.getMessage()).contains("Overbooking");
    }
}
