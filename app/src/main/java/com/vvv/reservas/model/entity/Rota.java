package com.vvv.reservas.model.entity;

import com.vvv.reservas.model.enums.TipoRota;
import jakarta.persistence.*;

/** Trajeto entre duas cidades (tabela rotas). Somente leitura no fluxo do cliente. */
@Entity
@Table(name = "rotas")
public class Rota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rota")
    private Integer id;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "descricao")
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cidade_origem", nullable = false)
    private Cidade cidadeOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cidade_destino", nullable = false)
    private Cidade cidadeDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoRota tipo;

    public Integer getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Cidade getCidadeOrigem() { return cidadeOrigem; }
    public void setCidadeOrigem(Cidade cidadeOrigem) { this.cidadeOrigem = cidadeOrigem; }
    public Cidade getCidadeDestino() { return cidadeDestino; }
    public void setCidadeDestino(Cidade cidadeDestino) { this.cidadeDestino = cidadeDestino; }
    public TipoRota getTipo() { return tipo; }
    public void setTipo(TipoRota tipo) { this.tipo = tipo; }
}
