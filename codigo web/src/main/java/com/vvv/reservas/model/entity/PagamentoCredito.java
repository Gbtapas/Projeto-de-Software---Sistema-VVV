package com.vvv.reservas.model.entity;

import com.vvv.reservas.model.enums.TipoPagamento;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Pagamento por cartão de crédito (subtipo de {@link Pagamento}).
 * Pode ser parcelado: até 4x sem juros (RN20), acima de 4x com 5% (RN21) — cálculo
 * feito pela trigger trg_pagamento_before_insert.
 */
@Entity
@DiscriminatorValue("CREDITO")
public class PagamentoCredito extends Pagamento {

    @Override
    public TipoPagamento getTipo() {
        return TipoPagamento.CREDITO;
    }
}
