package com.vvv.reservas.model.entity;

import com.vvv.reservas.model.enums.StatusAprovacao;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Detalhe de venda online (tabela vendas_online) — RF14/UC09. PK compartilhada com
 * vendas (@MapsId). O gerente virtual aprova/recusa; a trigger trg_vo_valida_gerente_ins
 * garante que o responsável seja do tipo GERENTE_VIRTUAL (RN31).
 */
@Entity
@Table(name = "vendas_online")
public class VendaOnline {

    @Id
    @Column(name = "id_venda")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_venda")
    private Venda venda;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_gerente_virtual", nullable = false)
    private Funcionario gerenteVirtual;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_aprovacao", insertable = false)
    private StatusAprovacao statusAprovacao;

    public Long getId() { return id; }
    public Venda getVenda() { return venda; }
    public void setVenda(Venda venda) { this.venda = venda; }
    public Funcionario getGerenteVirtual() { return gerenteVirtual; }
    public void setGerenteVirtual(Funcionario gerenteVirtual) { this.gerenteVirtual = gerenteVirtual; }
    public LocalDateTime getDataAprovacao() { return dataAprovacao; }
    public void setDataAprovacao(LocalDateTime dataAprovacao) { this.dataAprovacao = dataAprovacao; }
    public StatusAprovacao getStatusAprovacao() { return statusAprovacao; }
    public void setStatusAprovacao(StatusAprovacao statusAprovacao) { this.statusAprovacao = statusAprovacao; }
}
