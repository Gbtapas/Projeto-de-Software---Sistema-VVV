package com.vvv.reservas.controller;

import com.vvv.reservas.dto.PagamentoForm;
import com.vvv.reservas.model.entity.Ticket;
import com.vvv.reservas.model.enums.TipoPagamento;
import com.vvv.reservas.service.PagamentoService;
import com.vvv.reservas.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/** Checkout e processamento de pagamento (UC04 / RF10–RF12). */
@Controller
public class PagamentoController {

    private final PagamentoService pagamentoService;
    private final ReservaService reservaService;

    public PagamentoController(PagamentoService pagamentoService, ReservaService reservaService) {
        this.pagamentoService = pagamentoService;
        this.reservaService = reservaService;
    }

    @GetMapping("/checkout/{idReserva}")
    public String checkout(@PathVariable Long idReserva, Model model) {
        PagamentoForm form = new PagamentoForm();
        form.setIdReserva(idReserva);
        model.addAttribute("reserva", reservaService.buscar(idReserva));
        model.addAttribute("pagamentoForm", form);
        model.addAttribute("tipos", TipoPagamento.values());
        return "checkout";
    }

    @PostMapping("/pagamento")
    public String pagar(@Valid @ModelAttribute("pagamentoForm") PagamentoForm form,
                        BindingResult br,
                        Model model) {
        if (br.hasErrors()) {
            model.addAttribute("reserva", reservaService.buscar(form.getIdReserva()));
            model.addAttribute("tipos", TipoPagamento.values());
            return "checkout";
        }
        // Simula a operadora de cartão: cria pagamento e aprova.
        // A aprovação dispara a trigger que confirma a reserva e emite o ticket (UC05).
        pagamentoService.processarEAprovar(form.getIdReserva(), form.getTipo(), form.getParcelas());
        Ticket ticket = pagamentoService.buscarTicketDaReserva(form.getIdReserva());
        return "redirect:/ticket/" + ticket.getId();
    }
}
