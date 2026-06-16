package com.vvv.reservas.controller;

import com.vvv.reservas.service.RegraNegocioException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Traduz erros de regra de negócio (inclusive os vindos de triggers) em uma página amigável. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegraNegocioException.class)
    public String regraNegocio(RegraNegocioException e, Model model) {
        model.addAttribute("mensagem", e.getMessage());
        return "erro";
    }
}
