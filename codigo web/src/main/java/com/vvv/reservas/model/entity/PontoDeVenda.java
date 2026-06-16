package com.vvv.reservas.model.entity;

import jakarta.persistence.*;

/** Ponto de venda físico (tabela pontos_de_venda) — RF17. */
@Entity
@Table(name = "pontos_de_venda")
public class PontoDeVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ponto")
    private Integer id;

    @Column(name = "codigo", nullable = false)
    private String codigo;

    @Column(name = "cnpj", nullable = false, length = 14)
    private String cnpj;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "rua")
    private String rua;

    @Column(name = "bairro")
    private String bairro;

    @Column(name = "cep", length = 8)
    private String cep;

    @Column(name = "cidade_endereco")
    private String cidadeEndereco;

    @Column(name = "estado_endereco", length = 2)
    private String estadoEndereco;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_gerente")
    private Funcionario gerente;

    public Integer getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getRua() { return rua; }
    public void setRua(String rua) { this.rua = rua; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getCidadeEndereco() { return cidadeEndereco; }
    public void setCidadeEndereco(String cidade) { this.cidadeEndereco = cidade; }
    public String getEstadoEndereco() { return estadoEndereco; }
    public void setEstadoEndereco(String estado) { this.estadoEndereco = estado; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public Funcionario getGerente() { return gerente; }
    public void setGerente(Funcionario gerente) { this.gerente = gerente; }
}
