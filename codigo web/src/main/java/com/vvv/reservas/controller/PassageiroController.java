package com.vvv.reservas.controller;

import com.vvv.reservas.dto.PassageiroForm;
import com.vvv.reservas.model.entity.Passageiro;
import com.vvv.reservas.service.PassageiroService;
import com.vvv.reservas.service.RegraNegocioException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Cadastro de passageiro (UC01 / RF01). */
@Controller
public class PassageiroController {

    private final PassageiroService passageiroService;

    public PassageiroController(PassageiroService passageiroService) {
        this.passageiroService = passageiroService;
    }

    @GetMapping("/passageiro/novo")
    public String formulario(Model model) {
        if (!model.containsAttribute("passageiroForm")) {
            model.addAttribute("passageiroForm", new PassageiroForm());
        }
        return "cadastro";
    }

    @PostMapping("/passageiro/novo")
    public String salvar(@Valid @ModelAttribute("passageiroForm") PassageiroForm form,
                         BindingResult br,
                         Principal principal,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "cadastro";
        }
        try {
            String email = principal != null ? principal.getName() : null;
            Passageiro p = passageiroService.cadastrar(form, email);
            ra.addFlashAttribute("msgSucesso",
                    "Passageiro cadastrado: " + p.getNome() + " (código " + p.getCodigo() + ").");
            return "redirect:/viagens";
        } catch (RegraNegocioException e) {
            br.rejectValue("cpf", "duplicado", e.getMessage());
            return "cadastro";
        }
    }
}
