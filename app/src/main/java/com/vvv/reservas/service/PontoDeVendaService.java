package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.FuncionarioPontoDeVenda;
import com.vvv.reservas.model.entity.PontoDeVenda;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.repository.FuncionarioPdvRepository;
import com.vvv.reservas.repository.FuncionarioRepository;
import com.vvv.reservas.repository.PontoDeVendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Gestão de pontos de venda e vínculos com funcionários — RF17 e RF18. */
@Service
public class PontoDeVendaService {

    private final PontoDeVendaRepository pdvRepo;
    private final FuncionarioRepository funcionarioRepo;
    private final FuncionarioPdvRepository fpvRepo;
    private final AuditoriaService auditoria;

    public PontoDeVendaService(PontoDeVendaRepository pdvRepo, FuncionarioRepository funcionarioRepo,
                                FuncionarioPdvRepository fpvRepo, AuditoriaService auditoria) {
        this.pdvRepo = pdvRepo;
        this.funcionarioRepo = funcionarioRepo;
        this.fpvRepo = fpvRepo;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<PontoDeVenda> listar() {
        return pdvRepo.findAllByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<FuncionarioPontoDeVenda> listarVinculos() {
        return fpvRepo.findByAtivoTrueOrderByDataInicioDesc();
    }

    @Transactional
    public PontoDeVenda salvar(String cnpj, String nome, String rua, String bairro,
                               String cep, String cidade, String estado, String telefone,
                               Long idGerente) {
        PontoDeVenda pdv = new PontoDeVenda();
        pdv.setCodigo(gerarCodigo());
        pdv.setCnpj(cnpj);
        pdv.setNome(nome);
        pdv.setRua(rua);
        pdv.setBairro(bairro);
        pdv.setCep(cep);
        pdv.setCidadeEndereco(cidade);
        pdv.setEstadoEndereco(estado);
        pdv.setTelefone(telefone);
        pdv.setAtivo(true);
        if (idGerente != null) {
            pdv.setGerente(funcionarioRepo.getReferenceById(idGerente));
        }

        try {
            PontoDeVenda salvo = pdvRepo.save(pdv);
            auditoria.registrar("pontos_de_venda", Long.valueOf(salvo.getId()), OperacaoAuditoria.INSERT,
                    null, "{\"cnpj\":\"" + salvo.getCnpj() + "\",\"nome\":\"" + salvo.getNome() + "\"}");
            return salvo;
        } catch (RuntimeException e) {
            throw RegraNegocioException.de(e);
        }
    }

    @Transactional
    public void vincular(Long idFuncionario, Integer idPonto, LocalDate dataInicio) {
        FuncionarioPontoDeVenda fpv = new FuncionarioPontoDeVenda();
        fpv.setFuncionario(funcionarioRepo.getReferenceById(idFuncionario));
        fpv.setPonto(pdvRepo.getReferenceById(idPonto));
        fpv.setDataInicio(dataInicio != null ? dataInicio : LocalDate.now());
        fpv.setAtivo(true);

        try {
            FuncionarioPontoDeVenda salvo = fpvRepo.save(fpv);
            auditoria.registrar("funcionarios_pontos_de_venda", salvo.getId(), OperacaoAuditoria.INSERT,
                    null, "{\"idFuncionario\":" + idFuncionario + ",\"idPonto\":" + idPonto + "}");
        } catch (RuntimeException e) {
            throw RegraNegocioException.de(e);
        }
    }

    @Transactional
    public void desvincular(Long idFpv) {
        FuncionarioPontoDeVenda fpv = fpvRepo.findById(idFpv)
                .orElseThrow(() -> new RegraNegocioException("Vínculo não encontrado."));
        fpv.setAtivo(false);
        fpv.setDataFim(LocalDate.now());
        fpvRepo.save(fpv);
        auditoria.registrar("funcionarios_pontos_de_venda", idFpv, OperacaoAuditoria.UPDATE,
                "{\"ativo\":true}", "{\"ativo\":false}");
    }

    private String gerarCodigo() {
        return "PDV" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();
    }
}
