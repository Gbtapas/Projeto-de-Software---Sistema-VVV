package com.vvv.reservas.service;

import com.vvv.reservas.dto.BuscaViagemForm;
import com.vvv.reservas.model.entity.Cidade;
import com.vvv.reservas.model.view.ProgramacaoDisponivel;
import com.vvv.reservas.repository.CidadeRepository;
import com.vvv.reservas.repository.ProgramacaoDisponivelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Consulta de viagens disponíveis (UC02 / RF07). Reaproveita a view do banco. */
@Service
public class ViagemService {

    private final ProgramacaoDisponivelRepository disponiveisRepository;
    private final CidadeRepository cidadeRepository;

    public ViagemService(ProgramacaoDisponivelRepository disponiveisRepository,
                         CidadeRepository cidadeRepository) {
        this.disponiveisRepository = disponiveisRepository;
        this.cidadeRepository = cidadeRepository;
    }

    @Transactional(readOnly = true)
    public List<ProgramacaoDisponivel> buscar(BuscaViagemForm form) {
        return disponiveisRepository.buscar(form.getOrigem(), form.getDestino(), form.getData());
    }

    @Transactional(readOnly = true)
    public List<Cidade> listarCidades() {
        return cidadeRepository.findAllByOrderByNomeAsc();
    }
}
