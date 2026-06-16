package com.vvv.reservas.controller;

import com.vvv.reservas.dto.ReservaForm;
import com.vvv.reservas.model.entity.Reserva;
import com.vvv.reservas.model.enums.CanalReserva;
import com.vvv.reservas.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Criação de reserva (UC03 / RF07). */
@Controller
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping("/reserva/nova")
    public String criar(@Valid @ModelAttribute("reservaForm") ReservaForm form,
                        BindingResult br,
                        RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("msgErro", "Selecione a viagem e o passageiro.");
            return "redirect:/viagens";
        }
        // Canal ONLINE: este é o fluxo do cliente pela web.
        Reserva reserva = reservaService.criar(form.getIdProgramacao(), form.getIdPassageiro(), CanalReserva.ONLINE);
        return "redirect:/checkout/" + reserva.getId();
    }
}
