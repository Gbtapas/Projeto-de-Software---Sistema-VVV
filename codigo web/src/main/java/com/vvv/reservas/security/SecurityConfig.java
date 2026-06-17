package com.vvv.reservas.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Configuração do Spring Security. As regras refletem a generalização de atores:
 * - área administrativa: ADMIN / GERENTE_PDV
 * - vendas online: GERENTE_VIRTUAL
 * - vendas presenciais: FUNCIONARIO
 * - fluxo do cliente (compra): público nesta fase (será atrelado ao CLIENTE no Increment B).
 */
@Configuration
public class SecurityConfig {

    private final LoginAttemptService loginAttemptService;

    public SecurityConfig(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos, login e cadastro
                .requestMatchers("/css/**", "/js/**", "/login", "/signup", "/error").permitAll()
                // Fluxo público do cliente (UC01–UC05)
                .requestMatchers("/", "/viagens", "/passageiro/**", "/reserva/**",
                                 "/checkout/**", "/pagamento", "/ticket/**").permitAll()
                // Áreas protegidas por papel
                .requestMatchers("/admin/funcionarios/**").hasRole("ADMIN")
                .requestMatchers("/admin/relatorios/**").hasAnyRole("ADMIN", "GERENTE_PDV")
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "GERENTE_PDV")
                .requestMatchers("/vendas/online/**").hasRole("GERENTE_VIRTUAL")
                .requestMatchers("/vendas/presencial/**").hasRole("FUNCIONARIO")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler())
                .failureHandler(failureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout")
                .permitAll()
            );
        return http.build();
    }

    private AuthenticationSuccessHandler successHandler() {
        // UC18 passo 6: zera contador e registra acesso em log
        SavedRequestAwareAuthenticationSuccessHandler delegate =
                new SavedRequestAwareAuthenticationSuccessHandler();
        delegate.setDefaultTargetUrl("/");
        delegate.setAlwaysUseDefaultTargetUrl(true);

        return (HttpServletRequest req, HttpServletResponse res, Authentication auth) -> {
            loginAttemptService.registrarSucesso(auth.getName());
            delegate.onAuthenticationSuccess(req, res, auth);
        };
    }

    private AuthenticationFailureHandler failureHandler() {
        // UC18 fluxo alternativo: registra falha, bloqueia após 3 tentativas
        return (HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) -> {
            String email = req.getParameter("username");
            if (ex instanceof LockedException) {
                // Conta já bloqueada — não incrementa o contador novamente
                res.sendRedirect(req.getContextPath() + "/login?bloqueado");
                return;
            }
            if (email != null && !email.isBlank()) {
                loginAttemptService.registrarFalha(email);
            }
            res.sendRedirect(req.getContextPath() + "/login?error");
        };
    }
}
