package com.vvv.reservas.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Detalhe de venda presencial (tabela vendas_presenciais) — RF13/RN29. PK compartilhada
 * com vendas (@MapsId). Registra o funcionário e o ponto de venda, com confirmação manual.
 */
@Entity
@Table(name = "vendas_presenciais")
public class VendaPresencial {

    @Id
    @Column(name = "id_venda")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_venda")
    private Venda venda;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_funcionario", nullable = false)
    private Funcionario funcionario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_ponto", nullable = false)
    private PontoDeVenda ponto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmado_por")
    private Funcionario confirmadoPor;

    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;

    public Long getId() { return id; }
    public Venda getVenda() { return venda; }
    public void setVenda(Venda venda) { this.venda = venda; }
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }
    public PontoDeVenda getPonto() { return ponto; }
    public void setPonto(PontoDeVenda ponto) { this.ponto = ponto; }
    public Funcionario getConfirmadoPor() { return confirmadoPor; }
    public void setConfirmadoPor(Funcionario confirmadoPor) { this.confirmadoPor = confirmadoPor; }
    public LocalDateTime getDataConfirmacao() { return dataConfirmacao; }
    public void setDataConfirmacao(LocalDateTime dataConfirmacao) { this.dataConfirmacao = dataConfirmacao; }
}
