package com.vvv.reservas.dto;

import com.vvv.reservas.model.enums.TipoFuncionario;

public class FuncionarioForm {

    private String cpf;
    private String nome;
    private TipoFuncionario tipo;
    private String rua;
    private String bairro;
    private String cep;
    private String cidadeEndereco;
    private String estadoEndereco;
    private String email;
    private String senha;

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public TipoFuncionario getTipo() { return tipo; }
    public void setTipo(TipoFuncionario tipo) { this.tipo = tipo; }
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
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
