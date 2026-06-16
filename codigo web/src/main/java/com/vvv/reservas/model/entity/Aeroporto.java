package com.vvv.reservas.model.entity;

import jakarta.persistence.*;

/** Aeroporto vinculado a uma cidade (tabela aeroportos) — RN12. */
@Entity
@Table(name = "aeroportos")
public class Aeroporto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aeroporto")
    private Integer id;

    @Column(name = "codigo_iata", nullable = false, length = 3)
    private String codigoIata;

    @Column(name = "nome", nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cidade", nullable = false)
    private Cidade cidade;

    public Integer getId() { return id; }
    public String getCodigoIata() { return codigoIata; }
    public void setCodigoIata(String codigoIata) { this.codigoIata = codigoIata; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Cidade getCidade() { return cidade; }
    public void setCidade(Cidade cidade) { this.cidade = cidade; }
}
