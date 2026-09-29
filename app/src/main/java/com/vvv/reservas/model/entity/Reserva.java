package com.vvv.reservas.model.entity;

import com.vvv.reservas.model.enums.CanalReserva;
import com.vvv.reservas.model.enums.StatusReserva;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Reserva de passagem (tabela reservas). Colunas calculadas pelo banco via trigger
 * (codigo, valor_desconto, valor_total, status, data_criacao) são mapeadas como
 * insertable=false: o app deve fazer entityManager.refresh() após salvar para lê-las.
 * O app insere apenas: canal, valor_bruto e as FKs.
 */
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long id;

    @Column(name = "codigo", insertable = false, updatable = false)
    private String codigo;

    @Column(name = "data_criacao", insertable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", insertable = false)
    private StatusReserva status;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false)
    private CanalReserva canal;

    @Column(name = "valor_bruto", nullable = false)
    private BigDecimal valorBruto;

    @Column(name = "valor_desconto", insertable = false, updatable = false)
    private BigDecimal valorDesconto;

    @Column(name = "valor_total", insertable = false, updatable = false)
    private BigDecimal valorTotal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_passageiro", nullable = false)
    private Passageiro passageiro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_programacao", nullable = false)
    private ProgramacaoViagem programacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_acompanhante")
    private Passageiro acompanhante;

    /** Cliente que efetuou a compra (separado do passageiro que viaja). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente comprador;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public StatusReserva getStatus() { return status; }
    public void setStatus(StatusReserva status) { this.status = status; }
    public CanalReserva getCanal() { return canal; }
    public void setCanal(CanalReserva canal) { this.canal = canal; }
    public BigDecimal getValorBruto() { return valorBruto; }
    public void setValorBruto(BigDecimal valorBruto) { this.valorBruto = valorBruto; }
    public BigDecimal getValorDesconto() { return valorDesconto; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public Passageiro getPassageiro() { return passageiro; }
    public void setPassageiro(Passageiro passageiro) { this.passageiro = passageiro; }
    public ProgramacaoViagem getProgramacao() { return programacao; }
    public void setProgramacao(ProgramacaoViagem programacao) { this.programacao = programacao; }
    public Passageiro getAcompanhante() { return acompanhante; }
    public void setAcompanhante(Passageiro acompanhante) { this.acompanhante = acompanhante; }
    public Cliente getComprador() { return comprador; }
    public void setComprador(Cliente comprador) { this.comprador = comprador; }
}
