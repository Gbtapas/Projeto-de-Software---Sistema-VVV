package com.vvv.reservas.dto;

import com.vvv.reservas.model.entity.Reserva;
import com.vvv.reservas.model.entity.Venda;

/** Linha da tela de supervisão de vendas online: reserva + venda (pode ser null). */
public record SupervisaoItem(Reserva reserva, Venda venda) {
    public boolean isSupervisionada() {
        return venda != null;
    }
}
