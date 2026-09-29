package com.vvv.reservas.controller;

import com.vvv.reservas.dto.SupervisaoItem;
import com.vvv.reservas.service.RegraNegocioException;
import com.vvv.reservas.service.VendaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/** Supervisão de vendas online (RF14 / UC09). Restrito a GERENTE_VIRTUAL. */
@Controller
@RequestMapping("/vendas/online")
public class VendaOnlineController {

    private final VendaService vendaService;

    public VendaOnlineController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @GetMapping
    public String listar(Model model) {
        List<SupervisaoItem> itens = vendaService.reservasOnline().stream()
                .map(r -> new SupervisaoItem(r, vendaService.vendaDaReserva(r.getId())))
                .toList();
        model.addAttribute("itens", itens);
        return "vendas/online";
    }

    @PostMapping("/{idReserva}/decidir")
    public String decidir(@PathVariable Long idReserva, @RequestParam boolean aprovar,
                          Principal principal, RedirectAttributes ra) {
        try {
            vendaService.supervisionarOnline(idReserva, aprovar, principal.getName());
            ra.addFlashAttribute("msgSucesso", aprovar ? "Venda aprovada." : "Venda recusada e reserva cancelada.");
        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("msgErro", e.getMessage());
        }
        return "redirect:/vendas/online";
    }
}
