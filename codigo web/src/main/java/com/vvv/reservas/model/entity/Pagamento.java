package com.vvv.reservas.model.entity;

import com.vvv.reservas.model.enums.StatusPagamento;
import com.vvv.reservas.model.enums.TipoPagamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pagamento de uma reserva (tabela pagamentos, 1:1 com reserva — RN22).
 *
 * GENERALIZAÇÃO (alinhamento ao modelo do professor): classe ABSTRATA com herança
 * de tabela única (SINGLE_TABLE), discriminada pela coluna 'tipo'. Os subtipos são
 * {@link PagamentoCredito} e {@link PagamentoDebito}. Nenhuma mudança de banco: a
 * coluna 'tipo' (ENUM CREDITO/DEBITO) passa a ser o discriminador.
 *
 * Os valores de juros/total/parcela são calculados por trigger BEFORE INSERT
 * (RN19–RN21): mapeados como insertable=false. O app insere o subtipo + parcelas +
 * valor_bruto; depois atualiza o status para APROVADO (o que dispara a emissão
 * automática do ticket via trigger AFTER UPDATE).
 */
@Entity
@Table(name = "pagamentos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagamento")
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_reserva", nullable = false, unique = true)
    private Reserva reserva;

    @Column(name = "parcelas", nullable = false)
    private Integer parcelas = 1;

    @Column(name = "valor_bruto", nullable = false)
    private BigDecimal valorBruto;

    @Column(name = "valor_juros", insertable = false, updatable = false)
    private BigDecimal valorJuros;

    @Column(name = "valor_total", insertable = false, updatable = false)
    private BigDecimal valorTotal;

    @Column(name = "valor_parcela", insertable = false, updatable = false)
    private BigDecimal valorParcela;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", insertable = false)
    private StatusPagamento status;

    @Column(name = "codigo_autorizacao")
    private String codigoAutorizacao;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    /** Tipo do pagamento, derivado do subtipo concreto (= valor do discriminador). */
    @Transient
    public abstract TipoPagamento getTipo();

    public Long getId() { return id; }
    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }
    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }
    public BigDecimal getValorBruto() { return valorBruto; }
    public void setValorBruto(BigDecimal valorBruto) { this.valorBruto = valorBruto; }
    public BigDecimal getValorJuros() { return valorJuros; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public BigDecimal getValorParcela() { return valorParcela; }
    public StatusPagamento getStatus() { return status; }
    public void setStatus(StatusPagamento status) { this.status = status; }
    public String getCodigoAutorizacao() { return codigoAutorizacao; }
    public void setCodigoAutorizacao(String codigoAutorizacao) { this.codigoAutorizacao = codigoAutorizacao; }
    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDateTime dataPagamento) { this.dataPagamento = dataPagamento; }
}
