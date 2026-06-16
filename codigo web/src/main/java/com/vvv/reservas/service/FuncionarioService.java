package com.vvv.reservas.service;

import com.vvv.reservas.dto.FuncionarioForm;
import com.vvv.reservas.model.entity.Funcionario;
import com.vvv.reservas.model.entity.Perfil;
import com.vvv.reservas.model.entity.Usuario;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.repository.FuncionarioRepository;
import com.vvv.reservas.repository.PerfilRepository;
import com.vvv.reservas.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Gestão de funcionários — RF16. */
@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepo;
    private final UsuarioRepository usuarioRepo;
    private final PerfilRepository perfilRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoria;

    public FuncionarioService(FuncionarioRepository funcionarioRepo,
                              UsuarioRepository usuarioRepo,
                              PerfilRepository perfilRepo,
                              PasswordEncoder passwordEncoder,
                              AuditoriaService auditoria) {
        this.funcionarioRepo = funcionarioRepo;
        this.usuarioRepo = usuarioRepo;
        this.perfilRepo = perfilRepo;
        this.passwordEncoder = passwordEncoder;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<Funcionario> listar() {
        return funcionarioRepo.findAllByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<Funcionario> listarTodos() {
        return funcionarioRepo.findAll();
    }

    @Transactional
    public Funcionario salvar(FuncionarioForm form) {
        if (funcionarioRepo.existsByCpf(form.getCpf()))
            throw new RegraNegocioException("Já existe um funcionário cadastrado com este CPF.");
        if (usuarioRepo.findByEmail(form.getEmail()).isPresent())
            throw new RegraNegocioException("Já existe um usuário com este e-mail.");

        // Cria credencial de acesso vinculada ao perfil correspondente ao tipo do funcionário
        String nomeRole = form.getTipo().name(); // ex: "GERENTE_VIRTUAL", "FUNCIONARIO"
        Perfil perfil = perfilRepo.findByNome(nomeRole)
                .orElseThrow(() -> new RegraNegocioException("Perfil não encontrado: " + nomeRole));
        Usuario u = new Usuario();
        u.setEmail(form.getEmail());
        u.setSenhaHash(passwordEncoder.encode(form.getSenha()));
        u.setAtivo(true);
        u.getPerfis().add(perfil);
        u = usuarioRepo.save(u);

        Funcionario f = new Funcionario();
        f.setCodigo(gerarCodigo());
        f.setCpf(form.getCpf());
        f.setNome(form.getNome());
        f.setTipo(form.getTipo());
        f.setRua(form.getRua());
        f.setBairro(form.getBairro());
        f.setCep(form.getCep());
        f.setCidadeEndereco(form.getCidadeEndereco());
        f.setEstadoEndereco(form.getEstadoEndereco());
        f.setAtivo(true);
        f.setUsuario(u);

        try {
            Funcionario salvo = funcionarioRepo.save(f);
            auditoria.registrar("funcionarios", salvo.getId(), OperacaoAuditoria.INSERT,
                    null, "{\"cpf\":\"" + salvo.getCpf() + "\",\"nome\":\"" + salvo.getNome() + "\",\"tipo\":\"" + salvo.getTipo() + "\",\"email\":\"" + u.getEmail() + "\"}");
            return salvo;
        } catch (RuntimeException e) {
            throw RegraNegocioException.de(e);
        }
    }

    @Transactional
    public void desativar(Long id) {
        Funcionario f = funcionarioRepo.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Funcionário não encontrado."));
        f.setAtivo(false);
        funcionarioRepo.save(f);
        auditoria.registrar("funcionarios", id, OperacaoAuditoria.UPDATE,
                "{\"ativo\":true}", "{\"ativo\":false}");
    }

    private String gerarCodigo() {
        return "FUNC" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();
    }
}
