package com.vvv.reservas.service;

import com.vvv.reservas.dto.PassageiroForm;
import com.vvv.reservas.model.entity.Passageiro;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.repository.PassageiroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Regras de aplicação do cadastro de passageiros (UC01 / RF01). */
@Service
public class PassageiroService {

    private final PassageiroRepository passageiroRepository;
    private final AuditoriaService auditoria;

    public PassageiroService(PassageiroRepository passageiroRepository, AuditoriaService auditoria) {
        this.passageiroRepository = passageiroRepository;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<Passageiro> listarAtivos() {
        return passageiroRepository.findAllByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Passageiro buscar(Long id) {
        return passageiroRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Passageiro não encontrado."));
    }

    @Transactional
    public Passageiro cadastrar(PassageiroForm form) {
        if (passageiroRepository.existsByCpf(form.getCpf())) {
            throw new RegraNegocioException("Já existe um passageiro cadastrado com este CPF.");
        }

        Passageiro p = new Passageiro();
        p.setCpf(form.getCpf());
        p.setNome(form.getNome());
        p.setDataNascimento(form.getDataNascimento());
        p.setTelefone(form.getTelefone());
        p.setProfissao(form.getProfissao());
        p.setRua(form.getRua());
        p.setNumero(form.getNumero());
        p.setComplemento(form.getComplemento());
        p.setBairro(form.getBairro());
        p.setCep(form.getCep());
        p.setCidadeEndereco(form.getCidadeEndereco());
        p.setEstadoEndereco(form.getEstadoEndereco());
        p.setAtivo(true);
        p.setCodigo(gerarCodigo());

        Passageiro salvo = passageiroRepository.save(p);
        auditoria.registrar("passageiros", salvo.getId(), OperacaoAuditoria.INSERT,
                null, "{\"cpf\":\"" + salvo.getCpf() + "\",\"nome\":\"" + salvo.getNome() + "\"}");
        return salvo;
    }

    /** Código único de negócio do passageiro (coluna codigo, UNIQUE). */
    private String gerarCodigo() {
        return "PAS" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();
    }
}
