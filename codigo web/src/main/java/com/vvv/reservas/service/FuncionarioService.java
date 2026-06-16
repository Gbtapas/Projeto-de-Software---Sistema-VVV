package com.vvv.reservas.service;

import com.vvv.reservas.dto.FuncionarioForm;
import com.vvv.reservas.model.entity.Funcionario;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.repository.FuncionarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Gestão de funcionários — RF16. */
@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepo;
    private final AuditoriaService auditoria;

    public FuncionarioService(FuncionarioRepository funcionarioRepo, AuditoriaService auditoria) {
        this.funcionarioRepo = funcionarioRepo;
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
        if (funcionarioRepo.existsByCpf(form.getCpf())) {
            throw new RegraNegocioException("Já existe um funcionário cadastrado com este CPF.");
        }

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

        try {
            Funcionario salvo = funcionarioRepo.save(f);
            auditoria.registrar("funcionarios", salvo.getId(), OperacaoAuditoria.INSERT,
                    null, "{\"cpf\":\"" + salvo.getCpf() + "\",\"nome\":\"" + salvo.getNome() + "\",\"tipo\":\"" + salvo.getTipo() + "\"}");
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
