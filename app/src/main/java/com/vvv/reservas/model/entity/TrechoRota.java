package com.vvv.reservas.model.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

/**
 * Segmento ordenado de uma rota (tabela trechos_rota) — RN10/RN14.
 * Os horários e o tempo estimado aqui são usados pela trigger que emite o ticket.
 */
@Entity
@Table(name = "trechos_rota")
public class TrechoRota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trecho")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rota", nullable = false)
    private Rota rota;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cidade_origem", nullable = false)
    private Cidade cidadeOrigem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cidade_destino", nullable = false)
    private Cidade cidadeDestino;

    @Column(name = "hora_partida", nullable = false)
    private LocalTime horaPartida;

    @Column(name = "hora_chegada", nullable = false)
    private LocalTime horaChegada;

    @Column(name = "tempo_estimado_min", nullable = false)
    private Integer tempoEstimadoMin;

    public Integer getId() { return id; }
    public Rota getRota() { return rota; }
    public void setRota(Rota rota) { this.rota = rota; }
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public Cidade getCidadeOrigem() { return cidadeOrigem; }
    public void setCidadeOrigem(Cidade cidadeOrigem) { this.cidadeOrigem = cidadeOrigem; }
    public Cidade getCidadeDestino() { return cidadeDestino; }
    public void setCidadeDestino(Cidade cidadeDestino) { this.cidadeDestino = cidadeDestino; }
    public LocalTime getHoraPartida() { return horaPartida; }
    public void setHoraPartida(LocalTime horaPartida) { this.horaPartida = horaPartida; }
    public LocalTime getHoraChegada() { return horaChegada; }
    public void setHoraChegada(LocalTime horaChegada) { this.horaChegada = horaChegada; }
    public Integer getTempoEstimadoMin() { return tempoEstimadoMin; }
    public void setTempoEstimadoMin(Integer tempoEstimadoMin) { this.tempoEstimadoMin = tempoEstimadoMin; }
}
