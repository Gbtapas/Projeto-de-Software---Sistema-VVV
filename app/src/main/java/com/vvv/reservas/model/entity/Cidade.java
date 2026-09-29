package com.vvv.reservas.model.entity;

import jakarta.persistence.*;

/** Cidade de origem/destino das viagens (tabela cidades). Somente leitura no fluxo do cliente. */
@Entity
@Table(name = "cidades")
public class Cidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cidade")
    private Integer id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "pais", nullable = false)
    private String pais;

    /** Identificador de 3 letras maiúsculas, ex.: RIO, SAO (RN11). */
    @Column(name = "identificador", nullable = false, length = 3)
    private String identificador;

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
}
