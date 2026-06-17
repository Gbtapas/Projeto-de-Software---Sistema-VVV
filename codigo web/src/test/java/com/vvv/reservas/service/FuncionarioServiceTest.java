package com.vvv.reservas.service;

import com.vvv.reservas.dto.FuncionarioForm;
import com.vvv.reservas.model.entity.Funcionario;
import com.vvv.reservas.model.entity.Perfil;
import com.vvv.reservas.model.entity.Usuario;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.model.enums.TipoFuncionario;
import com.vvv.reservas.repository.FuncionarioRepository;
import com.vvv.reservas.repository.PerfilRepository;
import com.vvv.reservas.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock FuncionarioRepository funcionarioRepo;
    @Mock UsuarioRepository usuarioRepo;
    @Mock PerfilRepository perfilRepo;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditoriaService auditoria;
    @InjectMocks FuncionarioService service;

    // ------------------------------------------------------------------ listar

    @Test
    @DisplayName("listar retorna apenas funcionários ativos ordenados por nome")
    void listar_delegaAoRepositorio() {
        Funcionario f = funcionario("12345678901", "Ana Costa");
        when(funcionarioRepo.findAllByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(f));

        List<Funcionario> resultado = service.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Ana Costa");
    }

    @Test
    @DisplayName("listarTodos retorna todos os funcionários (ativos e inativos)")
    void listarTodos_delegaAoRepositorio() {
        when(funcionarioRepo.findAll()).thenReturn(List.of(new Funcionario(), new Funcionario()));

        List<Funcionario> resultado = service.listarTodos();

        assertThat(resultado).hasSize(2);
    }

    // ------------------------------------------------------------------ salvar

    @Test
    @DisplayName("salvar lança exception quando CPF já cadastrado")
    void salvar_cpfDuplicado_lancaException() {
        FuncionarioForm form = form("11111111111", "Carlos");
        form.setEmail("carlos@vvv.com");
        form.setSenha("senha123");
        when(funcionarioRepo.existsByCpf("11111111111")).thenReturn(true);

        assertThatThrownBy(() -> service.salvar(form))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("CPF");

        verify(funcionarioRepo, never()).save(any());
    }

    @Test
    @DisplayName("salvar persiste funcionário com código gerado e registra auditoria")
    void salvar_cpfNovo_salvaNaBaseERegistraAuditoria() {
        FuncionarioForm form = form("22222222222", "Beatriz Lima");
        form.setEmail("beatriz@vvv.com");
        form.setSenha("senha123");
        Funcionario salvo = funcionario("22222222222", "Beatriz Lima");

        when(funcionarioRepo.existsByCpf("22222222222")).thenReturn(false);
        when(usuarioRepo.findByEmail("beatriz@vvv.com")).thenReturn(Optional.empty());
        
        Perfil perfil = new Perfil();
        when(perfilRepo.findByNome("FUNCIONARIO")).thenReturn(Optional.of(perfil));
        when(passwordEncoder.encode("senha123")).thenReturn("encoded_senha");
        
        Usuario usuario = new Usuario();
        when(usuarioRepo.save(any())).thenReturn(usuario);
        
        when(funcionarioRepo.save(any())).thenReturn(salvo);

        Funcionario resultado = service.salvar(form);

        assertThat(resultado.getCpf()).isEqualTo("22222222222");

        ArgumentCaptor<Funcionario> captor = ArgumentCaptor.forClass(Funcionario.class);
        verify(funcionarioRepo).save(captor.capture());
        assertThat(captor.getValue().getCodigo()).startsWith("FUNC");
        assertThat(captor.getValue().getAtivo()).isTrue();

        verify(auditoria).registrar(eq("funcionarios"), any(), eq(OperacaoAuditoria.INSERT), eq(null), anyString());
    }

    // ------------------------------------------------------------------ desativar

    @Test
    @DisplayName("desativar lança exception quando funcionário não encontrado")
    void desativar_naoEncontrado_lancaException() {
        when(funcionarioRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desativar(99L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    @DisplayName("desativar define ativo=false e registra auditoria")
    void desativar_sucesso_desativaEAudita() {
        Funcionario f = funcionario("33333333333", "Diego Ramos");
        when(funcionarioRepo.findById(1L)).thenReturn(Optional.of(f));
        when(funcionarioRepo.save(any())).thenReturn(f);

        service.desativar(1L);

        assertThat(f.getAtivo()).isFalse();
        verify(auditoria).registrar(eq("funcionarios"), eq(1L), eq(OperacaoAuditoria.UPDATE),
                eq("{\"ativo\":true}"), eq("{\"ativo\":false}"));
    }

    // ------------------------------------------------------------------ helpers

    private Funcionario funcionario(String cpf, String nome) {
        Funcionario f = new Funcionario();
        f.setCpf(cpf);
        f.setNome(nome);
        f.setCodigo("FUNC" + Long.toString(System.currentTimeMillis(), 36).toUpperCase());
        f.setTipo(TipoFuncionario.FUNCIONARIO);
        f.setAtivo(true);
        return f;
    }

    private FuncionarioForm form(String cpf, String nome) {
        FuncionarioForm f = new FuncionarioForm();
        f.setCpf(cpf);
        f.setNome(nome);
        f.setTipo(TipoFuncionario.FUNCIONARIO);
        return f;
    }
}
