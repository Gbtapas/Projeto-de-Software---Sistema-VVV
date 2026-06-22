package com.vvv.reservas.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BuscaViagemFormTest {

    @Test
    @DisplayName("getOrigem converte texto em maiúsculas e remove espaços")
    void getOrigem_textoMinusculo_retornaMaiusculo() {
        // garantindo que nao vai dar erro aqui
        BuscaViagemForm form = new BuscaViagemForm();
        form.setOrigem("  gru  ");

        assertThat(form.getOrigem()).isEqualTo("GRU");
    }

    @Test
    @DisplayName("getOrigem retorna null quando origem é nula")
    void getOrigem_null_retornaNull() {
        // conferindo os valores retornados
        BuscaViagemForm form = new BuscaViagemForm();
        form.setOrigem(null);

        assertThat(form.getOrigem()).isNull();
    }

    @Test
    @DisplayName("getOrigem retorna null quando origem está em branco")
    void getOrigem_branco_retornaNull() {
        // garantindo que nao vai dar erro aqui
        BuscaViagemForm form = new BuscaViagemForm();
        form.setOrigem("   ");

        assertThat(form.getOrigem()).isNull();
    }

    @Test
    @DisplayName("getDestino converte texto em maiúsculas")
    void getDestino_textoMinusculo_retornaMaiusculo() {
        // garantindo a logica de negocio
        BuscaViagemForm form = new BuscaViagemForm();
        form.setDestino("sao");

        assertThat(form.getDestino()).isEqualTo("SAO");
    }

    @Test
    @DisplayName("getData retorna a data exatamente como foi definida")
    void getData_retornaDataDefinida() {
        // conferindo os valores retornados
        BuscaViagemForm form = new BuscaViagemForm();
        LocalDate data = LocalDate.of(2026, 8, 20);
        form.setData(data);

        assertThat(form.getData()).isEqualTo(data);
    }
}
