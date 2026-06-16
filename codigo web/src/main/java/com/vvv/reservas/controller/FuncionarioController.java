package com.vvv.reservas.controller;

import com.vvv.reservas.dto.FuncionarioForm;
import com.vvv.reservas.model.enums.TipoFuncionario;
import com.vvv.reservas.service.FuncionarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Gestão de funcionários — RF16. Restrito a ADMIN. */
@Controller
@RequestMapping("/admin/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("funcionarios", funcionarioService.listar());
        model.addAttribute("tipos", TipoFuncionario.values());
        return "admin/funcionarios";
    }

    @PostMapping
    public String salvar(@ModelAttribute FuncionarioForm form, RedirectAttributes ra) {
        funcionarioService.salvar(form);
        ra.addFlashAttribute("msgSucesso", "Funcionário cadastrado com sucesso.");
        return "redirect:/admin/funcionarios";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes ra) {
        funcionarioService.desativar(id);
        ra.addFlashAttribute("msgSucesso", "Funcionário desativado.");
        return "redirect:/admin/funcionarios";
    }
}
