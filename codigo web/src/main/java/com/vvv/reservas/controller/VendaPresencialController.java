package com.vvv.reservas.controller;

import com.vvv.reservas.dto.BuscaViagemForm;
import com.vvv.reservas.service.PassageiroService;
import com.vvv.reservas.service.VendaService;
import com.vvv.reservas.service.ViagemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** Registro de venda presencial (RF13 / RN29). Restrito a FUNCIONARIO. */
@Controller
@RequestMapping("/vendas/presencial")
public class VendaPresencialController {

    private final VendaService vendaService;
    private final ViagemService viagemService;
    private final PassageiroService passageiroService;

    public VendaPresencialController(VendaService vendaService, ViagemService viagemService,
                                     PassageiroService passageiroService) {
        this.vendaService = vendaService;
        this.viagemService = viagemService;
        this.passageiroService = passageiroService;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("viagens", viagemService.buscar(new BuscaViagemForm()));
        model.addAttribute("passageiros", passageiroService.listarAtivos());
        model.addAttribute("pontos", vendaService.pontos());
        return "vendas/presencial";
    }

    @PostMapping
    public String registrar(@RequestParam Integer idProgramacao, @RequestParam Long idPassageiro,
                            @RequestParam(required = false) Long idAcompanhante,
                            @RequestParam Integer idPonto, Principal principal) {
        Long idReserva = vendaService.registrarPresencial(idProgramacao, idPassageiro, idAcompanhante, idPonto, principal.getName());
        // Encaminha para o checkout para coletar o pagamento e emitir o ticket.
        return "redirect:/checkout/" + idReserva;
    }
}
