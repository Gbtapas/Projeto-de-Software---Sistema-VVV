package com.vvv.reservas.model.entity;

import com.vvv.reservas.model.enums.StatusTicket;
import com.vvv.reservas.model.enums.TipoPassagem;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Ticket emitido (tabela tickets). É criado AUTOMATICAMENTE pela trigger
 * trg_pagamento_after_update quando o pagamento é aprovado (RN23). O app NUNCA
 * insere ou altera ticket — apenas consulta. Por isso é mapeado como @Immutable.
 */
@Entity
@Immutable
@Table(name = "tickets")
public class Ticket {

    @Id
    @Column(name = "id_ticket")
    private Long id;

    @Column(name = "codigo_ticket")
    private String codigoTicket;

    @Column(name = "localizador")
    private String localizador;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_passagem")
    private TipoPassagem tipoPassagem;

    @Column(name = "hora_partida")
    private LocalTime horaPartida;

    @Column(name = "hora_chegada")
    private LocalTime horaChegada;

    @Column(name = "tempo_estimado_min")
    private Integer tempoEstimadoMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusTicket status;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    public Long getId() { return id; }
    public String getCodigoTicket() { return codigoTicket; }
    public String getLocalizador() { return localizador; }
    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public TipoPassagem getTipoPassagem() { return tipoPassagem; }
    public LocalTime getHoraPartida() { return horaPartida; }
    public LocalTime getHoraChegada() { return horaChegada; }
    public Integer getTempoEstimadoMin() { return tempoEstimadoMin; }
    public StatusTicket getStatus() { return status; }
    public Reserva getReserva() { return reserva; }

    /** Tempo estimado formatado, ex.: "1h30min". */
    @Transient
    public String getTempoFormatado() {
        if (tempoEstimadoMin == null) return "";
        int h = tempoEstimadoMin / 60;
        int m = tempoEstimadoMin % 60;
        return (h > 0 ? h + "h" : "") + (m > 0 ? m + "min" : (h > 0 ? "" : "0min"));
    }
}
