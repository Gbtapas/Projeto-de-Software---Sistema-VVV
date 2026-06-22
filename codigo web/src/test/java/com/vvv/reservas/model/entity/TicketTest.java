package com.vvv.reservas.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketTest {

    @Test
    @DisplayName("getTempoFormatado retorna string vazia quando tempoEstimadoMin é null")
    void getTempoFormatado_null_retornaVazio() {
        // se passar isso o resto vai de boa
        Ticket t = new Ticket();

        assertThat(t.getTempoFormatado()).isEmpty();
    }

    @Test
    @DisplayName("getTempoFormatado formata 90 minutos como '1h30min'")
    void getTempoFormatado_90min_retorna1h30min() {
        // bora testar esse cenario
        Ticket t = comTempo(90);

        assertThat(t.getTempoFormatado()).isEqualTo("1h30min");
    }

    @Test
    @DisplayName("getTempoFormatado formata 60 minutos como '1h'")
    void getTempoFormatado_60min_retorna1h() {
        // verificando se ta tudo certo
        Ticket t = comTempo(60);

        assertThat(t.getTempoFormatado()).isEqualTo("1h");
    }

    @Test
    @DisplayName("getTempoFormatado formata 45 minutos como '45min'")
    void getTempoFormatado_45min_retorna45min() {
        // bora testar esse cenario
        Ticket t = comTempo(45);

        assertThat(t.getTempoFormatado()).isEqualTo("45min");
    }

    @Test
    @DisplayName("getTempoFormatado formata 0 minutos como '0min'")
    void getTempoFormatado_zeroMin_retorna0min() {
        // checando o comportamento esperado
        Ticket t = comTempo(0);

        assertThat(t.getTempoFormatado()).isEqualTo("0min");
    }

    @Test
    @DisplayName("getTempoFormatado formata 120 minutos como '2h'")
    void getTempoFormatado_120min_retorna2h() {
        // mais uma checagem de rotina
        Ticket t = comTempo(120);

        assertThat(t.getTempoFormatado()).isEqualTo("2h");
    }

    @Test
    @DisplayName("getTempoFormatado formata 125 minutos como '2h5min'")
    void getTempoFormatado_125min_retorna2h5min() {
        // mais uma checagem de rotina
        Ticket t = comTempo(125);

        assertThat(t.getTempoFormatado()).isEqualTo("2h5min");
    }

    // ------------------------------------------------------------------ helper

    private Ticket comTempo(int minutos) {
        try {
            Ticket t = new Ticket();
            java.lang.reflect.Field f = Ticket.class.getDeclaredField("tempoEstimadoMin");
            f.setAccessible(true);
            f.set(t, minutos);
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
