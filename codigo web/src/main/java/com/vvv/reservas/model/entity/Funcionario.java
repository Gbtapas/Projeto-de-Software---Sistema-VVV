package com.vvv.reservas.model.entity;

import com.vvv.reservas.model.enums.TipoFuncionario;
import jakarta.persistence.*;

/** Funcionário (tabela funcionarios) — RF16/RF17/RF18. */
@Entity
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_funcionario")
    private Long id;

    @Column(name = "codigo", nullable = false)
    private String codigo;

    @Column(name = "cpf", nullable = false)
    private String cpf;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoFuncionario tipo;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getRua() { return rua; }
    public void setRua(String rua) { this.rua = rua; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getCidadeEndereco() { return cidadeEndereco; }
    public void setCidadeEndereco(String cidadeEndereco) { this.cidadeEndereco = cidadeEndereco; }
    public String getEstadoEndereco() { return estadoEndereco; }
    public void setEstadoEndereco(String estadoEndereco) { this.estadoEndereco = estadoEndereco; }
    public TipoFuncionario getTipo() { return tipo; }
    public void setTipo(TipoFuncionario tipo) { this.tipo = tipo; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
