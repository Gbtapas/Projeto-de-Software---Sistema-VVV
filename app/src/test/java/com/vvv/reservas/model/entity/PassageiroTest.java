package com.vvv.reservas.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PassageiroTest {

    @Test
    @DisplayName("getIdade calcula corretamente a idade com base na data de nascimento")
    void getIdade_calculaIdadeCorreta() {
        // verificando se ta tudo certo
        // bora ver se ta calculando a idade direito
        Passageiro p = new Passageiro();
        p.setDataNascimento(LocalDate.now().minusYears(25));

        assertThat(p.getIdade()).isEqualTo(25);
    }

    @Test
    @DisplayName("getIdade retorna 0 quando dataNascimento é null")
    void getIdade_dataNascimentoNull_retornaZero() {
        // bora testar esse cenario
        // sem data de nascimento a idade tem que ser zero pra nao dar pau
        Passageiro p = new Passageiro();
        p.setDataNascimento(null);

        assertThat(p.getIdade()).isEqualTo(0);
    }

    @Test
    @DisplayName("getIdade retorna 0 para bebê nascido hoje")
    void getIdade_nascidoHoje_retornaZero() {
        // garantindo que nao vai dar erro aqui
        // recem nascido nao tem nem um ano ainda ne
        Passageiro p = new Passageiro();
        p.setDataNascimento(LocalDate.now());

        assertThat(p.getIdade()).isEqualTo(0);
    }

    @Test
    @DisplayName("getIdade retorna 1 para aniversário já ocorrido este ano")
    void getIdade_aniversarioJaOcorrido_retorna1() {
        // bora testar esse cenario
        Passageiro p = new Passageiro();
        p.setDataNascimento(LocalDate.now().minusYears(1).minusDays(1));

        assertThat(p.getIdade()).isEqualTo(1);
    }

    @Test
    @DisplayName("ativo=true é o valor padrão de um passageiro novo")
    void novoPassageiro_ativoTrue_porPadrao() {
        // so pra ter certeza que ta pegando o valor certo
        // todo passageiro novo ja entra como ativo no sistema
        Passageiro p = new Passageiro();

        assertThat(p.getAtivo()).isTrue();
    }
}
