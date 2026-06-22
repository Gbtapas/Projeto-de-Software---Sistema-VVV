package com.vvv.reservas.dto;

import com.vvv.reservas.model.enums.TipoPagamento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Form de pagamento (UC04 / RF10–RF11). Juros e parcelamento são calculados no banco. */
public class PagamentoForm {

    @NotNull
    private Long idReserva;

    @NotNull(message = "Selecione a forma de pagamento")
    private TipoPagamento tipo;

    @NotNull
    @Min(value = 1, message = "Mínimo de 1 parcela")
    @Max(value = 12, message = "Máximo de 12 parcelas")
    private Integer parcelas = 1;

    private String nomeCartao;
    private String numeroCartao;
    private String validadeCartao;
    private String cvvCartao;


    public Long getIdReserva() { return idReserva; }
    public void setIdReserva(Long idReserva) { this.idReserva = idReserva; }
    public TipoPagamento getTipo() { return tipo; }
    public void setTipo(TipoPagamento tipo) { this.tipo = tipo; }
    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }

    public String getNomeCartao() { return nomeCartao; }
    public void setNomeCartao(String nomeCartao) { this.nomeCartao = nomeCartao; }
    public String getNumeroCartao() { return numeroCartao; }
    public void setNumeroCartao(String numeroCartao) { this.numeroCartao = numeroCartao; }
    public String getValidadeCartao() { return validadeCartao; }
    public void setValidadeCartao(String validadeCartao) { this.validadeCartao = validadeCartao; }
    public String getCvvCartao() { return cvvCartao; }
    public void setCvvCartao(String cvvCartao) { this.cvvCartao = cvvCartao; }
}
