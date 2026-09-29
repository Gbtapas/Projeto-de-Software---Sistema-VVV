package com.vvv.reservas.service;

import com.vvv.reservas.dto.PassageiroForm;
import com.vvv.reservas.model.entity.Passageiro;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.repository.ClienteRepository;
import com.vvv.reservas.repository.PassageiroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Regras de aplicação do cadastro de passageiros (UC01 / RF01). */
@Service
public class PassageiroService {

    private final PassageiroRepository passageiroRepository;
    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoria;

    public PassageiroService(PassageiroRepository passageiroRepository, ClienteRepository clienteRepository, AuditoriaService auditoria) {
        this.passageiroRepository = passageiroRepository;
        this.clienteRepository = clienteRepository;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<Passageiro> listarAtivos() {
        return passageiroRepository.findAllByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<Passageiro> listarPorUsuario(String email) {
        return passageiroRepository.findByCliente_Usuario_EmailAndAtivoTrueOrderByNomeAsc(email);
    }

    @Transactional(readOnly = true)
    public Passageiro buscar(Long id) {
        return passageiroRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Passageiro não encontrado."));
    }

    @Transactional
    public Passageiro cadastrar(PassageiroForm form) {
        return cadastrar(form, null);
    }

    @Transactional
    public Passageiro cadastrar(PassageiroForm form, String emailUsuario) {
        if (passageiroRepository.existsByCpf(form.getCpf())) {
            throw new RegraNegocioException("Já existe um passageiro cadastrado com este CPF.");
        }
        if (form.getDataNascimento() != null) {
            LocalDate minima = LocalDate.now().minusYears(120);
            if (form.getDataNascimento().isBefore(minima)) {
                throw new RegraNegocioException("Data de nascimento inválida: idade máxima permitida é 120 anos.");
            }
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

        if (emailUsuario != null) {
            clienteRepository.findByUsuario_Email(emailUsuario).ifPresent(p::setCliente);
        }

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
