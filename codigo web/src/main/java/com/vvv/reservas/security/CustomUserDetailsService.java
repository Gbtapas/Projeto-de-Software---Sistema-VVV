package com.vvv.reservas.security;

import com.vvv.reservas.model.entity.Usuario;
import com.vvv.reservas.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Autentica usuários a partir da tabela usuarios, com authorities vindas de perfis. */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        boolean bloqueado = u.getBloqueadoAte() != null && u.getBloqueadoAte().isAfter(LocalDateTime.now());

        List<SimpleGrantedAuthority> authorities = u.getPerfis().stream()
                .map(p -> new SimpleGrantedAuthority(p.getAuthority()))
                .toList();

        return User.withUsername(u.getEmail())
                .password(u.getSenhaHash())
                .authorities(authorities)
                .disabled(!Boolean.TRUE.equals(u.getAtivo()))
                .accountLocked(bloqueado)
                .build();
    }
}
