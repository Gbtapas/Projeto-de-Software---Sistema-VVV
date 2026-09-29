package com.vvv.reservas.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/** Vínculo entre funcionário e ponto de venda — RF18 / RN28 (máx. 2 por funcionário). */
@Entity
@Table(name = "funcionarios_pontos_de_venda")
public class FuncionarioPontoDeVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fpv")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_funcionario", nullable = false)
    private Funcionario funcionario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_ponto", nullable = false)
    private PontoDeVenda ponto;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    public Long getId() { return id; }
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }
    public PontoDeVenda getPonto() { return ponto; }
    public void setPonto(PontoDeVenda ponto) { this.ponto = ponto; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
