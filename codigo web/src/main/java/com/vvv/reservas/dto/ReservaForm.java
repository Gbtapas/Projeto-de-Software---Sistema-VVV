package com.vvv.reservas.dto;

import jakarta.validation.constraints.NotNull;

/** Form de criação de reserva (UC03 / RF07). */
public class ReservaForm {

    @NotNull(message = "Selecione a viagem")
    private Integer idProgramacao;

    @NotNull(message = "Selecione o passageiro")
    private Long idPassageiro;

    /** Acompanhante obrigatório para passageiros entre 2 e 10 anos (RN04). Nulo para adultos. */
    private Long idAcompanhante;

    public Integer getIdProgramacao() { return idProgramacao; }
    public void setIdProgramacao(Integer idProgramacao) { this.idProgramacao = idProgramacao; }
    public Long getIdPassageiro() { return idPassageiro; }
    public void setIdPassageiro(Long idPassageiro) { this.idPassageiro = idPassageiro; }
    public Long getIdAcompanhante() { return idAcompanhante; }
    public void setIdAcompanhante(Long idAcompanhante) { this.idAcompanhante = idAcompanhante; }
}
