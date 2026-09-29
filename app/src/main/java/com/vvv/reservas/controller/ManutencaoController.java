package com.vvv.reservas.controller;

import com.vvv.reservas.model.enums.StatusManutencao;
import com.vvv.reservas.service.CatalogoService;
import com.vvv.reservas.service.ManutencaoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Manutenção de modais (RF04 / UC07). Restrito a ADMIN/GERENTE_PDV. */
@Controller
@RequestMapping("/admin/manutencoes")
public class ManutencaoController {

    private final ManutencaoService manutencaoService;
    private final CatalogoService catalogo;

    public ManutencaoController(ManutencaoService manutencaoService, CatalogoService catalogo) {
        this.manutencaoService = manutencaoService;
        this.catalogo = catalogo;
    }

    @GetMapping
    public String listar(Model m) {
        m.addAttribute("manutencoes", manutencaoService.listar());
        m.addAttribute("modais", catalogo.modais());
        return "admin/manutencoes";
    }

    @PostMapping
    public String agendar(@RequestParam Integer idModal,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                          @RequestParam(required = false) String descricao, RedirectAttributes ra) {
        manutencaoService.agendar(idModal, dataInicio, dataFim, descricao);
        ra.addFlashAttribute("msgSucesso", "Manutenção agendada. Modal bloqueado se o período já começou (RN18).");
        return "redirect:/admin/manutencoes";
    }

    @PostMapping("/{id}/status")
    public String mudarStatus(@PathVariable Integer id, @RequestParam StatusManutencao status,
                              RedirectAttributes ra) {
        manutencaoService.mudarStatus(id, status);
        ra.addFlashAttribute("msgSucesso", "Status da manutenção atualizado.");
        return "redirect:/admin/manutencoes";
    }
}
