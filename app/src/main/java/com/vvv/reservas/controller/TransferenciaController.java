package com.vvv.reservas.controller;

import com.vvv.reservas.service.TransferenciaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Transferência de informações para transportadoras — RF15. Restrito a ADMIN/GERENTE_PDV. */
@Controller
@RequestMapping("/admin/transferencias")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @GetMapping
    public String painel(Model model) {
        model.addAttribute("pendentes", transferenciaService.listarPendentes());
        model.addAttribute("historico", transferenciaService.historico());
        return "admin/transferencias";
    }

    @PostMapping("/{idTransportadora}/enviar")
    public String enviar(@PathVariable Integer idTransportadora, RedirectAttributes ra) {
        transferenciaService.transferir(idTransportadora);
        ra.addFlashAttribute("msgSucesso", "Dados transferidos à transportadora com sucesso.");
        return "redirect:/admin/transferencias";
    }
}
