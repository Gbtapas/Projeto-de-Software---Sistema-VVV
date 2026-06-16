package com.vvv.reservas.model.entity;

import jakarta.persistence.*;

/** Empresa proprietária dos modais (tabela transportadoras). Somente leitura no MVP. */
@Entity
@Table(name = "transportadoras")
public class Transportadora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transportadora")
    private Integer id;

    @Column(name = "cnpj", nullable = false, length = 14)
    private String cnpj;

    @Column(name = "nome", nullable = false)
    private String nome;

    public Integer getId() { return id; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
