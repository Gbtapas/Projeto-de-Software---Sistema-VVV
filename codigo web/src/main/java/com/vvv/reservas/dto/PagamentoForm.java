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

    public Long getIdReserva() { return idReserva; }
    public void setIdReserva(Long idReserva) { this.idReserva = idReserva; }
    public TipoPagamento getTipo() { return tipo; }
    public void setTipo(TipoPagamento tipo) { this.tipo = tipo; }
    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }
}
