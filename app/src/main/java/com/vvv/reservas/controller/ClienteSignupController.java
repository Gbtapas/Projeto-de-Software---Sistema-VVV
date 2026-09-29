package com.vvv.reservas.controller;

import com.vvv.reservas.dto.ClienteSignupForm;
import com.vvv.reservas.service.ClienteService;
import com.vvv.reservas.service.RegraNegocioException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ClienteSignupController {

    private final ClienteService clienteService;

    public ClienteSignupController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        if (!model.containsAttribute("clienteSignupForm")) {
            model.addAttribute("clienteSignupForm", new ClienteSignupForm());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String registerCliente(@Valid @ModelAttribute("clienteSignupForm") ClienteSignupForm form,
                                  BindingResult br,
                                  RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "signup";
        }
        try {
            clienteService.cadastrar(form);
            ra.addFlashAttribute("msgSucesso", "Cadastro realizado com sucesso! Faça login para continuar.");
            return "redirect:/login";
        } catch (RegraNegocioException e) {
            br.rejectValue("email", "duplicado", e.getMessage());
            return "signup";
        }
    }
}
