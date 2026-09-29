package com.vvv.reservas.controller;

import com.vvv.reservas.model.enums.TipoModal;
import com.vvv.reservas.model.enums.TipoRota;
import com.vvv.reservas.service.CatalogoService;
import com.vvv.reservas.service.ViagemService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Área administrativa (RF02/RF03/RF05/RF06 + rotas/programação).
 * Acesso restrito a ADMIN/GERENTE_PDV (ver SecurityConfig).
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CatalogoService catalogo;
    private final ViagemService viagemService;

    public AdminController(CatalogoService catalogo, ViagemService viagemService) {
        this.catalogo = catalogo;
        this.viagemService = viagemService;
    }

    @GetMapping
    public String dashboard() {
        return "admin/dashboard";
    }

    // ---------- Cidades (RF05) ----------
    @GetMapping("/cidades")
    public String cidades(Model m) {
        m.addAttribute("cidades", catalogo.cidades());
        return "admin/cidades";
    }

    @PostMapping("/cidades")
    public String salvarCidade(@RequestParam String nome, @RequestParam String estado,
                               @RequestParam(required = false) String pais,
                               @RequestParam String identificador, RedirectAttributes ra) {
        catalogo.salvarCidade(nome, estado, pais, identificador);
        ra.addFlashAttribute("msgSucesso", "Cidade cadastrada.");
        return "redirect:/admin/cidades";
    }

    @PostMapping("/cidades/{id}/excluir")
    public String excluirCidade(@PathVariable Integer id, RedirectAttributes ra) {
        catalogo.excluirCidade(id);
        ra.addFlashAttribute("msgSucesso", "Cidade excluída.");
        return "redirect:/admin/cidades";
    }

    // ---------- Aeroportos (RF06) ----------
    @GetMapping("/aeroportos")
    public String aeroportos(Model m) {
        m.addAttribute("aeroportos", catalogo.aeroportos());
        m.addAttribute("cidades", catalogo.cidades());
        return "admin/aeroportos";
    }

    @PostMapping("/aeroportos")
    public String salvarAeroporto(@RequestParam String iata, @RequestParam String nome,
                                  @RequestParam Integer idCidade, RedirectAttributes ra) {
        catalogo.salvarAeroporto(iata, nome, idCidade);
        ra.addFlashAttribute("msgSucesso", "Aeroporto cadastrado.");
        return "redirect:/admin/aeroportos";
    }

    @PostMapping("/aeroportos/{id}/excluir")
    public String excluirAeroporto(@PathVariable Integer id, RedirectAttributes ra) {
        catalogo.excluirAeroporto(id);
        ra.addFlashAttribute("msgSucesso", "Aeroporto excluído.");
        return "redirect:/admin/aeroportos";
    }

    // ---------- Transportadoras (RF02) ----------
    @GetMapping("/transportadoras")
    public String transportadoras(Model m) {
        m.addAttribute("transportadoras", catalogo.transportadoras());
        return "admin/transportadoras";
    }

    @PostMapping("/transportadoras")
    public String salvarTransportadora(@RequestParam String cnpj, @RequestParam String nome,
                                       RedirectAttributes ra) {
        catalogo.salvarTransportadora(cnpj, nome);
        ra.addFlashAttribute("msgSucesso", "Transportadora cadastrada.");
        return "redirect:/admin/transportadoras";
    }

    @PostMapping("/transportadoras/{id}/excluir")
    public String excluirTransportadora(@PathVariable Integer id, RedirectAttributes ra) {
        catalogo.excluirTransportadora(id);
        ra.addFlashAttribute("msgSucesso", "Transportadora excluída.");
        return "redirect:/admin/transportadoras";
    }

    // ---------- Modais (RF03) ----------
    @GetMapping("/modais")
    public String modais(Model m) {
        m.addAttribute("modais", catalogo.modais());
        m.addAttribute("transportadoras", catalogo.transportadoras());
        m.addAttribute("aeroportos", catalogo.aeroportos());
        m.addAttribute("tipos", TipoModal.values());
        return "admin/modais";
    }

    @PostMapping("/modais")
    public String salvarModal(@RequestParam(required = false) String codigo, @RequestParam TipoModal tipo,
                              @RequestParam String modelo, @RequestParam Integer ano,
                              @RequestParam Integer capacidade, @RequestParam Integer idTransportadora,
                              @RequestParam(required = false) Integer idAeroportoBase, RedirectAttributes ra) {
        catalogo.salvarModal(codigo, tipo, modelo, ano, capacidade, idTransportadora, idAeroportoBase);
        ra.addFlashAttribute("msgSucesso", "Modal cadastrado.");
        return "redirect:/admin/modais";
    }

    @PostMapping("/modais/{id}/excluir")
    public String excluirModal(@PathVariable Integer id, RedirectAttributes ra) {
        catalogo.excluirModal(id);
        ra.addFlashAttribute("msgSucesso", "Modal excluído.");
        return "redirect:/admin/modais";
    }

    // ---------- Rotas (+ trecho) ----------
    @GetMapping("/rotas")
    public String rotas(Model m) {
        m.addAttribute("rotas", catalogo.rotas());
        m.addAttribute("cidades", catalogo.cidades());
        m.addAttribute("tipos", TipoRota.values());
        return "admin/rotas";
    }

    @PostMapping("/rotas")
    public String salvarRota(@RequestParam(required = false) String codigo, @RequestParam String descricao,
                             @RequestParam Integer idOrigem, @RequestParam Integer idDestino,
                             @RequestParam TipoRota tipo,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaPartida,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaChegada,
                             @RequestParam Integer tempoMin, RedirectAttributes ra) {
        catalogo.salvarRota(codigo, descricao, idOrigem, idDestino, tipo, horaPartida, horaChegada, tempoMin);
        ra.addFlashAttribute("msgSucesso", "Rota cadastrada.");
        return "redirect:/admin/rotas";
    }

    @PostMapping("/rotas/{id}/excluir")
    public String excluirRota(@PathVariable Integer id, RedirectAttributes ra) {
        catalogo.excluirRota(id);
        ra.addFlashAttribute("msgSucesso", "Rota excluída.");
        return "redirect:/admin/rotas";
    }

    // ---------- Programações de viagem (viagens vendáveis) ----------
    @GetMapping("/programacoes")
    public String programacoes(Model m) {
        m.addAttribute("programacoes", catalogo.programacoes());
        m.addAttribute("rotas", catalogo.rotas());
        m.addAttribute("modais", catalogo.modais());
        return "admin/programacoes";
    }

    @PostMapping("/programacoes")
    public String salvarProgramacao(@RequestParam Integer idRota, @RequestParam Integer idModal,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                                    @RequestParam Integer vagas, @RequestParam BigDecimal valor,
                                    RedirectAttributes ra) {
        catalogo.salvarProgramacao(idRota, idModal, data, vagas, valor);
        ra.addFlashAttribute("msgSucesso", "Programação de viagem cadastrada.");
        return "redirect:/admin/programacoes";
    }

    @PostMapping("/programacoes/{id}/excluir")
    public String excluirProgramacao(@PathVariable Integer id, RedirectAttributes ra) {
        catalogo.excluirProgramacao(id);
        ra.addFlashAttribute("msgSucesso", "Programação excluída.");
        return "redirect:/admin/programacoes";
    }
}
