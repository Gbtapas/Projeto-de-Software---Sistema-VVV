package com.vvv.reservas.dto;

import jakarta.validation.constraints.NotNull;

/** Form de criação de reserva (UC03 / RF07). */
public class ReservaForm {

    @NotNull(message = "Selecione a viagem")
    private Integer idProgramacao;

    @NotNull(message = "Selecione o passageiro")
    private Long idPassageiro;

    public Integer getIdProgramacao() { return idProgramacao; }
    public void setIdProgramacao(Integer idProgramacao) { this.idProgramacao = idProgramacao; }
    public Long getIdPassageiro() { return idPassageiro; }
    public void setIdPassageiro(Long idPassageiro) { this.idPassageiro = idPassageiro; }
}
