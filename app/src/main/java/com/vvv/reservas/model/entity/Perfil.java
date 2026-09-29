package com.vvv.reservas.model.entity;

import jakarta.persistence.*;

/** Papel de acesso no sistema (tabela perfis). Ex.: CLIENTE, FUNCIONARIO, GERENTE_PDV... */
@Entity
@Table(name = "perfis")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Short id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    public Short getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }

    /** Authority no formato esperado pelo Spring Security (ROLE_<NOME>). */
    @Transient
    public String getAuthority() {
        return "ROLE_" + nome;
    }
}
