package com.vvv.reservas.model.entity;

import com.vvv.reservas.model.enums.TipoPagamento;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Pagamento por cartão de débito (subtipo de {@link Pagamento}).
 * Sempre à vista — não pode ser parcelado (RN19, garantido por CHECK + trigger).
 */
@Entity
@DiscriminatorValue("DEBITO")
public class PagamentoDebito extends Pagamento {

    public PagamentoDebito() {
        setParcelas(1);
    }

    @Override
    public TipoPagamento getTipo() {
        return TipoPagamento.DEBITO;
    }
}
