package com.vvv.reservas.model.enums;

/** Status do pagamento (coluna pagamentos.status). APROVADO dispara emissão do ticket. */
public enum StatusPagamento {
    PENDENTE,
    APROVADO,
    RECUSADO,
    ESTORNADO
}
