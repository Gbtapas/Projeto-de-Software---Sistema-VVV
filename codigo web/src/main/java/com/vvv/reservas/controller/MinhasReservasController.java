package com.vvv.reservas.controller;

import com.vvv.reservas.service.ReservaService;
import com.vvv.reservas.service.RegraNegocioException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/minhas-reservas")
public class MinhasReservasController {

    private final ReservaService reservaService;

    public MinhasReservasController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public String listar(Authentication auth, Model model) {
        model.addAttribute("reservas", reservaService.listarPorUsuario(auth.getName()));
        return "minhas-reservas";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            reservaService.cancelar(id, auth.getName());
            ra.addFlashAttribute("sucesso", "Reserva cancelada com sucesso.");
        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/minhas-reservas";
    }
}
