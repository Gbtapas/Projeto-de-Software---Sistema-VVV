package com.vvv.reservas.model.entity;

import com.vvv.reservas.model.enums.OperacaoAuditoria;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Registro de auditoria de operações críticas — RnF06 / tabela log_auditoria. */
@Entity
@Table(name = "log_auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long id;

    @Column(name = "tabela", nullable = false, length = 60)
    private String tabela;

    @Column(name = "id_registro", nullable = false)
    private Long idRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "operacao", nullable = false)
    private OperacaoAuditoria operacao;

    @Column(name = "dados_anteriores", columnDefinition = "JSON")
    private String dadosAnteriores;

    @Column(name = "dados_novos", columnDefinition = "JSON")
    private String dadosNovos;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "data_hora", insertable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    public Long getId() { return id; }
    public String getTabela() { return tabela; }
    public void setTabela(String tabela) { this.tabela = tabela; }
    public Long getIdRegistro() { return idRegistro; }
    public void setIdRegistro(Long idRegistro) { this.idRegistro = idRegistro; }
    public OperacaoAuditoria getOperacao() { return operacao; }
    public void setOperacao(OperacaoAuditoria operacao) { this.operacao = operacao; }
    public String getDadosAnteriores() { return dadosAnteriores; }
    public void setDadosAnteriores(String dadosAnteriores) { this.dadosAnteriores = dadosAnteriores; }
    public String getDadosNovos() { return dadosNovos; }
    public void setDadosNovos(String dadosNovos) { this.dadosNovos = dadosNovos; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
