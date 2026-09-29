package com.vvv.reservas.service;

/** Erro de regra de negócio — normalmente originado de um SIGNAL de trigger do banco. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }

    /**
     * Extrai a mensagem mais útil de uma cadeia de exceções. As triggers do VVV
     * lançam SIGNAL SQLSTATE '45000' com textos como "RN04: ...", que ficam na
     * mensagem da SQLException encadeada.
     */
    public static RegraNegocioException de(Throwable t) {
        Throwable atual = t;
        String melhor = t.getMessage();
        while (atual != null) {
            String msg = atual.getMessage();
            if (msg != null && (msg.contains("RN") || msg.contains("RI") || msg.contains("Overbooking"))) {
                melhor = msg;
            }
            atual = atual.getCause();
        }
        // Limpa prefixos técnicos do driver, mantendo apenas o texto da regra
        if (melhor != null) {
            int idx = melhor.indexOf("RN");
            if (idx < 0) idx = melhor.indexOf("RI");
            if (idx > 0) melhor = melhor.substring(idx);
        }
        return new RegraNegocioException(melhor != null ? melhor : "Operação rejeitada pelas regras de negócio.");
    }
}
