package com.vvv.reservas.model.view;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projeção read-only mapeada para a VIEW vw_programacoes_disponiveis do banco.
 * A view já filtra vagas > 0, status ATIVO, data futura e modal disponível (UC02),
 * reaproveitando a lógica de disponibilidade que vive no banco.
 */
@Entity
@Immutable
@Table(name = "vw_programacoes_disponiveis")
public class ProgramacaoDisponivel {

    @Id
    @Column(name = "id_programacao")
    private Integer idProgramacao;

    @Column(name = "codigo_rota")     private String codigoRota;
    @Column(name = "descricao_rota")  private String descricaoRota;
    @Column(name = "tipo_rota")       private String tipoRota;
    @Column(name = "cidade_origem")   private String cidadeOrigem;
    @Column(name = "cod_origem")      private String codOrigem;
    @Column(name = "cidade_destino")  private String cidadeDestino;
    @Column(name = "cod_destino")     private String codDestino;
    @Column(name = "data_viagem")     private LocalDate dataViagem;
    @Column(name = "tipo_modal")      private String tipoModal;
    @Column(name = "modelo_modal")    private String modeloModal;
    @Column(name = "capacidade_total") private Integer capacidadeTotal;
    @Column(name = "vagas_disponiveis") private Integer vagasDisponiveis;
    @Column(name = "valor_base")      private BigDecimal valorBase;
    @Column(name = "transportadora")  private String transportadora;

    public Integer getIdProgramacao() { return idProgramacao; }
    public String getCodigoRota() { return codigoRota; }
    public String getDescricaoRota() { return descricaoRota; }
    public String getTipoRota() { return tipoRota; }
    public String getCidadeOrigem() { return cidadeOrigem; }
    public String getCodOrigem() { return codOrigem; }
    public String getCidadeDestino() { return cidadeDestino; }
    public String getCodDestino() { return codDestino; }
    public LocalDate getDataViagem() { return dataViagem; }
    public String getTipoModal() { return tipoModal; }
    public String getModeloModal() { return modeloModal; }
    public Integer getCapacidadeTotal() { return capacidadeTotal; }
    public Integer getVagasDisponiveis() { return vagasDisponiveis; }
    public BigDecimal getValorBase() { return valorBase; }
    public String getTransportadora() { return transportadora; }
}
