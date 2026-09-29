package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.Manutencao;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.model.enums.StatusManutencao;
import com.vvv.reservas.repository.ManutencaoRepository;
import com.vvv.reservas.repository.ModalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Manutenção de modais (RF04 / UC07). As triggers do banco cuidam de bloquear o
 * modal (RN18) e liberá-lo ao concluir/cancelar; aqui só registramos os períodos.
 */
@Service
public class ManutencaoService {

    private final ManutencaoRepository manutencaoRepo;
    private final ModalRepository modalRepo;
    private final AuditoriaService auditoria;
    private final TransferenciaService transferenciaService;

    public ManutencaoService(ManutencaoRepository manutencaoRepo, ModalRepository modalRepo,
                              AuditoriaService auditoria, TransferenciaService transferenciaService) {
        this.manutencaoRepo = manutencaoRepo;
        this.modalRepo = modalRepo;
        this.auditoria = auditoria;
        this.transferenciaService = transferenciaService;
    }

    @Transactional(readOnly = true)
    public List<Manutencao> listar() {
        return manutencaoRepo.findAllByOrderByDataInicioDesc();
    }

    @Transactional
    public void agendar(Integer idModal, LocalDate inicio, LocalDate fim, String descricao) {
        if (inicio == null || inicio.isBefore(LocalDate.now()))
            throw new RegraNegocioException("A data de início da manutenção não pode ser no passado.");
        if (fim == null || fim.isBefore(inicio))
            throw new RegraNegocioException("A data de fim da manutenção deve ser igual ou posterior à data de início.");
        try {
            Manutencao m = new Manutencao();
            m.setModal(modalRepo.getReferenceById(idModal));
            m.setDataInicio(inicio);
            m.setDataFim(fim);
            m.setDescricao(descricao);
            Manutencao salva = manutencaoRepo.save(m); // trigger marca modal como MANUTENCAO se início <= hoje
            // RnF06: auditoria
            auditoria.registrar("manutencoes", Long.valueOf(salva.getId()), OperacaoAuditoria.INSERT,
                    null, "{\"idModal\":" + idModal + ",\"inicio\":\"" + inicio + "\",\"fim\":\"" + fim + "\"}");
            // RF15: notifica transportadora sobre manutenção agendada
            Integer idTransportadora = salva.getModal().getTransportadora().getId();
            transferenciaService.notificarManutencao(idTransportadora,
                    descricao != null ? descricao : "Manutenção agendada");
        } catch (RuntimeException e) {
            throw RegraNegocioException.de(e);
        }
    }

    @Transactional
    public void mudarStatus(Integer id, StatusManutencao novo) {
        try {
            Manutencao m = manutencaoRepo.findById(id)
                    .orElseThrow(() -> new RegraNegocioException("Manutenção não encontrada."));
            m.setStatus(novo); // trigger libera o modal ao CONCLUIR/CANCELAR (se não houver outra ativa)
            manutencaoRepo.saveAndFlush(m);
        } catch (RegraNegocioException e) {
            throw e;
        } catch (RuntimeException e) {
            throw RegraNegocioException.de(e);
        }
    }
}
