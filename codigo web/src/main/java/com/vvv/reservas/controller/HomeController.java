package com.vvv.reservas.controller;

import com.vvv.reservas.dto.BuscaViagemForm;
import com.vvv.reservas.service.ViagemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Página inicial com o formulário de busca de viagens (UC02). */
@Controller
public class HomeController {

    private final ViagemService viagemService;

    public HomeController(ViagemService viagemService) {
        this.viagemService = viagemService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("busca", new BuscaViagemForm());
        model.addAttribute("cidades", viagemService.listarCidades());
        return "index";
    }
}
