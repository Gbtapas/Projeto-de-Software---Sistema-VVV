package com.vvv.reservas.controller;

import com.vvv.reservas.dto.BuscaViagemForm;
import com.vvv.reservas.dto.ReservaForm;
import com.vvv.reservas.service.PassageiroService;
import com.vvv.reservas.service.ViagemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Resultado da consulta de viagens disponíveis (UC02). */
@Controller
public class ViagemController {

    private final ViagemService viagemService;
    private final PassageiroService passageiroService;

    public ViagemController(ViagemService viagemService, PassageiroService passageiroService) {
        this.viagemService = viagemService;
        this.passageiroService = passageiroService;
    }

    @GetMapping("/viagens")
    public String viagens(@ModelAttribute("busca") BuscaViagemForm busca, Model model) {
        model.addAttribute("cidades", viagemService.listarCidades());
        model.addAttribute("viagens", viagemService.buscar(busca));
        model.addAttribute("passageiros", passageiroService.listarAtivos());
        model.addAttribute("reservaForm", new ReservaForm());
        return "viagens";
    }
}
