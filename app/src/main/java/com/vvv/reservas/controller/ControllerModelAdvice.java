package com.vvv.reservas.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expõe o usuário autenticado e seus papéis para TODAS as views (sem depender da
 * biblioteca thymeleaf-extras-security). Permite mostrar/ocultar menus por perfil.
 */
@ControllerAdvice
public class ControllerModelAdvice {

    @ModelAttribute("usuarioLogado")
    public String usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    @ModelAttribute("papeis")
    public java.util.List<String> papeis() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return java.util.List.of();
        return auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
    }
}
