package com.vvv.reservas.model.entity;

import com.vvv.reservas.model.enums.StatusProgramacao;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Instância operacional de uma viagem: rota + modal + data (tabela programacoes_viagem).
 * É o "produto" que o cliente reserva. Capacidade controlada em vagasDisponiveis (RN07).
 */
@Entity
@Table(name = "programacoes_viagem")
public class ProgramacaoViagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_programacao")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rota", nullable = false)
    private Rota rota;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_modal", nullable = false)
    private Modal modal;

    @Column(name = "data_viagem", nullable = false)
    private LocalDate dataViagem;

    @Column(name = "vagas_disponiveis", nullable = false)
    private Integer vagasDisponiveis;

    @Column(name = "valor_base", nullable = false)
    private BigDecimal valorBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusProgramacao status;

    public Integer getId() { return id; }
    public Rota getRota() { return rota; }
    public void setRota(Rota rota) { this.rota = rota; }
    public Modal getModal() { return modal; }
    public void setModal(Modal modal) { this.modal = modal; }
    public LocalDate getDataViagem() { return dataViagem; }
    public void setDataViagem(LocalDate dataViagem) { this.dataViagem = dataViagem; }
    public Integer getVagasDisponiveis() { return vagasDisponiveis; }
    public void setVagasDisponiveis(Integer vagasDisponiveis) { this.vagasDisponiveis = vagasDisponiveis; }
    public BigDecimal getValorBase() { return valorBase; }
    public void setValorBase(BigDecimal valorBase) { this.valorBase = valorBase; }
    public StatusProgramacao getStatus() { return status; }
    public void setStatus(StatusProgramacao status) { this.status = status; }
}
