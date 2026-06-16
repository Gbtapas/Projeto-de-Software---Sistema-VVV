package com.vvv.reservas.security;

import com.vvv.reservas.model.entity.Perfil;
import com.vvv.reservas.model.entity.Usuario;
import com.vvv.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks CustomUserDetailsService service;

    @Test
    @DisplayName("loadUserByUsername lança UsernameNotFoundException quando e-mail não existe")
    void loadUserByUsername_naoEncontrado_lancaUsernameNotFoundException() {
        when(usuarioRepository.findByEmail("desconhecido@vvv.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("desconhecido@vvv.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("desconhecido@vvv.com");
    }

    @Test
    @DisplayName("loadUserByUsername retorna UserDetails com autoridades ROLE_<perfil>")
    void loadUserByUsername_usuarioAtivo_retornaUserDetailsComAuthorities() {
        Perfil perfil = perfil("FUNCIONARIO");
        Usuario u = usuario("func@vvv.com", "$2a$12$hash", true, Set.of(perfil));

        when(usuarioRepository.findByEmail("func@vvv.com")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("func@vvv.com");

        assertThat(details.getUsername()).isEqualTo("func@vvv.com");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_FUNCIONARIO");
    }

    @Test
    @DisplayName("loadUserByUsername retorna UserDetails desabilitado quando ativo=false")
    void loadUserByUsername_usuarioInativo_retornaDesabilitado() {
        Usuario u = usuario("inativo@vvv.com", "$2a$12$hash", false, Set.of());

        when(usuarioRepository.findByEmail("inativo@vvv.com")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("inativo@vvv.com");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("loadUserByUsername mapeia múltiplos perfis corretamente")
    void loadUserByUsername_multiplosPeris_mapeiaTodos() {
        Perfil p1 = perfil("ADMIN");
        Perfil p2 = perfil("GERENTE_PDV");
        Usuario u = usuario("admin@vvv.com", "$2a$12$hash", true, Set.of(p1, p2));

        when(usuarioRepository.findByEmail("admin@vvv.com")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("admin@vvv.com");

        assertThat(details.getAuthorities()).hasSize(2);
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_GERENTE_PDV");
    }

    // ------------------------------------------------------------------ helpers

    private Perfil perfil(String nome) {
        Perfil p = new Perfil();
        try {
            java.lang.reflect.Field f = Perfil.class.getDeclaredField("nome");
            f.setAccessible(true);
            f.set(p, nome);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return p;
    }

    private Usuario usuario(String email, String hash, boolean ativo, Set<Perfil> perfis) {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setSenhaHash(hash);
        u.setAtivo(ativo);
        u.setPerfis(perfis);
        return u;
    }
}
