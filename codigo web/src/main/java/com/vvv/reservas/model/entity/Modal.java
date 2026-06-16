package com.vvv.reservas.model.entity;

import com.vvv.reservas.model.enums.StatusModal;
import com.vvv.reservas.model.enums.TipoModal;
import jakarta.persistence.*;

/** Meio de transporte (tabela modais). Somente leitura no fluxo do cliente. */
@Entity
@Table(name = "modais")
public class Modal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modal")
    private Integer id;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoModal tipo;

    @Column(name = "modelo", nullable = false)
    private String modelo;

    @Column(name = "ano", nullable = false)
    private Integer ano;

    @Column(name = "capacidade", nullable = false)
    private Integer capacidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusModal status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transportadora", nullable = false)
    private Transportadora transportadora;

    /** Mapeado como id simples — não criamos modais no MVP (aeroporto fora de escopo). */
    @Column(name = "id_aeroporto_base")
    private Integer idAeroportoBase;

    public Integer getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public TipoModal getTipo() { return tipo; }
    public void setTipo(TipoModal tipo) { this.tipo = tipo; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
    public StatusModal getStatus() { return status; }
    public void setStatus(StatusModal status) { this.status = status; }
    public Transportadora getTransportadora() { return transportadora; }
    public void setTransportadora(Transportadora transportadora) { this.transportadora = transportadora; }
    public Integer getIdAeroportoBase() { return idAeroportoBase; }
    public void setIdAeroportoBase(Integer idAeroportoBase) { this.idAeroportoBase = idAeroportoBase; }
}
