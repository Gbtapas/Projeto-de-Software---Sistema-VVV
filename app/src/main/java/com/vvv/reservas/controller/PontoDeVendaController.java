package com.vvv.reservas.controller;

import com.vvv.reservas.service.FuncionarioService;
import com.vvv.reservas.service.PontoDeVendaService;
import com.vvv.reservas.service.RegraNegocioException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Gestão de PDVs e vínculos — RF17 e RF18. Restrito a ADMIN/GERENTE_PDV. */
@Controller
@RequestMapping("/admin/pontos")
public class PontoDeVendaController {

    private final PontoDeVendaService pdvService;
    private final FuncionarioService funcionarioService;

    public PontoDeVendaController(PontoDeVendaService pdvService, FuncionarioService funcionarioService) {
        this.pdvService = pdvService;
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pontos", pdvService.listar());
        model.addAttribute("vinculos", pdvService.listarVinculos());
        model.addAttribute("funcionarios", funcionarioService.listar());
        return "admin/pontos";
    }

    @PostMapping
    public String salvar(@RequestParam String cnpj, @RequestParam String nome,
                         @RequestParam(required = false) String rua,
                         @RequestParam(required = false) String bairro,
                         @RequestParam(required = false) String cep,
                         @RequestParam(required = false) String cidadeEndereco,
                         @RequestParam(required = false) String estadoEndereco,
                         @RequestParam(required = false) String telefone,
                         @RequestParam(required = false) Long idGerente,
                         RedirectAttributes ra) {
        try {
            pdvService.salvar(cnpj, nome, rua, bairro, cep, cidadeEndereco, estadoEndereco, telefone, idGerente);
            ra.addFlashAttribute("msgSucesso", "Ponto de venda cadastrado.");
        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("msgErro", e.getMessage());
        }
        return "redirect:/admin/pontos";
    }

    @PostMapping("/vincular")
    public String vincular(@RequestParam Long idFuncionario, @RequestParam Integer idPonto,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                           RedirectAttributes ra) {
        try {
            pdvService.vincular(idFuncionario, idPonto, dataInicio);
            ra.addFlashAttribute("msgSucesso", "Funcionário vinculado ao ponto de venda.");
        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("msgErro", e.getMessage());
        }
        return "redirect:/admin/pontos";
    }

    @PostMapping("/{idFpv}/desvincular")
    public String desvincular(@PathVariable Long idFpv, RedirectAttributes ra) {
        try {
            pdvService.desvincular(idFpv);
            ra.addFlashAttribute("msgSucesso", "Vínculo encerrado.");
        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("msgErro", e.getMessage());
        }
        return "redirect:/admin/pontos";
    }
}
