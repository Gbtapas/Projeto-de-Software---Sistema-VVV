package com.vvv.reservas.controller;

import com.vvv.reservas.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/** UC10 — Gerar Relatórios (`/admin/relatorios`). */
@Controller
@RequestMapping("/admin/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping
    public String relatorios(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Model model) {

        // Defaults: último mês
        if (inicio == null) inicio = LocalDate.now().minusMonths(1);
        if (fim == null) fim = LocalDate.now();
        if (tipo == null) tipo = "reservas";

        model.addAttribute("tipo", tipo);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);

        switch (tipo) {
            case "canais" -> model.addAttribute("dados", relatorioService.vendasPorCanal(inicio, fim));
            case "modais" -> model.addAttribute("dados", relatorioService.ocupacaoModais(inicio, fim));
            case "transferencias" -> model.addAttribute("dados", relatorioService.transferencias());
            default -> model.addAttribute("dados", relatorioService.reservasPorPeriodo(inicio, fim));
        }

        return "admin/relatorios";
    }
}
