package com.vvv.reservas.controller;

import com.vvv.reservas.model.entity.Ticket;
import com.vvv.reservas.service.PagamentoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Exibição do ticket emitido automaticamente após o pagamento (UC05 / RF12). */
@Controller
public class TicketController {

    private final PagamentoService pagamentoService;

    public TicketController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/ticket/{id}")
    public String ticket(@PathVariable Long id, Model model) {
        Ticket ticket = pagamentoService.buscarTicket(id);
        model.addAttribute("ticket", ticket);
        model.addAttribute("reserva", ticket.getReserva());
        model.addAttribute("pagamento",
                pagamentoService.buscarPagamentoDaReserva(ticket.getReserva().getId()));
        return "ticket";
    }
}
