package com.vvv.reservas.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Form da tela inicial: filtra viagens por origem/destino (identificador de 3 letras) e data. */
public class BuscaViagemForm {

    private String origem;
    private String destino;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;

    public String getOrigem() { return vazioParaNull(origem); }
    public void setOrigem(String origem) { this.origem = origem; }
    public String getDestino() { return vazioParaNull(destino); }
    public void setDestino(String destino) { this.destino = destino; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    private static String vazioParaNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim().toUpperCase();
    }
}
