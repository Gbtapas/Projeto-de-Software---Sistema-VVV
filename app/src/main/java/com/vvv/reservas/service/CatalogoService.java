package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.*;
import com.vvv.reservas.model.enums.StatusModal;
import com.vvv.reservas.model.enums.StatusProgramacao;
import com.vvv.reservas.model.enums.TipoModal;
import com.vvv.reservas.model.enums.TipoRota;
import com.vvv.reservas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Cadastros base administrativos (RF02/RF03/RF05/RF06 + rotas/programação).
 * Confia em constraints/triggers do banco (ex.: AVIAO exige aeroporto, origem≠destino,
 * unicidade de códigos) e traduz erros via {@link RegraNegocioException}.
 */
@Service
public class CatalogoService {

    private final CidadeRepository cidadeRepo;
    private final AeroportoRepository aeroportoRepo;
    private final TransportadoraRepository transportadoraRepo;
    private final ModalRepository modalRepo;
    private final RotaRepository rotaRepo;
    private final TrechoRotaRepository trechoRepo;
    private final ProgramacaoViagemRepository programacaoRepo;

    public CatalogoService(CidadeRepository cidadeRepo, AeroportoRepository aeroportoRepo,
                           TransportadoraRepository transportadoraRepo, ModalRepository modalRepo,
                           RotaRepository rotaRepo, TrechoRotaRepository trechoRepo,
                           ProgramacaoViagemRepository programacaoRepo) {
        this.cidadeRepo = cidadeRepo;
        this.aeroportoRepo = aeroportoRepo;
        this.transportadoraRepo = transportadoraRepo;
        this.modalRepo = modalRepo;
        this.rotaRepo = rotaRepo;
        this.trechoRepo = trechoRepo;
        this.programacaoRepo = programacaoRepo;
    }

    // ---------- Listagens ----------
    @Transactional(readOnly = true) public List<Cidade> cidades() { return cidadeRepo.findAllByOrderByNomeAsc(); }
    @Transactional(readOnly = true) public List<Aeroporto> aeroportos() { return aeroportoRepo.findAllByOrderByCodigoIataAsc(); }
    @Transactional(readOnly = true) public List<Transportadora> transportadoras() { return transportadoraRepo.findAllByOrderByNomeAsc(); }
    @Transactional(readOnly = true) public List<Modal> modais() { return modalRepo.findAllByOrderByCodigoAsc(); }
    @Transactional(readOnly = true) public List<Rota> rotas() { return rotaRepo.findAllByOrderByCodigoAsc(); }
    @Transactional(readOnly = true) public List<ProgramacaoViagem> programacoes() { return programacaoRepo.findAll(); }

    // ---------- Cidade (RF05 / UC12) ----------
    @Transactional
    public void salvarCidade(String nome, String estado, String pais, String identificador) {
        String id3 = identificador == null ? "" : identificador.trim().toUpperCase();
        if (!id3.matches("[A-Z]{3}"))
            throw new RegraNegocioException("Identificador deve ter exatamente 3 letras maiúsculas (ex: RIO, SAO).");
        Cidade c = new Cidade();
        c.setNome(nome); c.setEstado(estado);
        c.setPais((pais == null || pais.isBlank()) ? "Brasil" : pais);
        c.setIdentificador(id3);
        salvar(() -> cidadeRepo.save(c));
    }

    // ---------- Aeroporto (RF06 / UC13) ----------
    @Transactional
    public void salvarAeroporto(String iata, String nome, Integer idCidade) {
        String iataUp = iata == null ? "" : iata.trim().toUpperCase();
        if (!iataUp.matches("[A-Z]{3}"))
            throw new RegraNegocioException("Código IATA deve ter exatamente 3 letras maiúsculas (ex: GRU, CGH).");
        Aeroporto a = new Aeroporto();
        a.setCodigoIata(iataUp);
        a.setNome(nome);
        a.setCidade(cidadeRepo.getReferenceById(idCidade));
        salvar(() -> aeroportoRepo.save(a));
    }

    // ---------- Transportadora (RF02 / UC11) ----------
    @Transactional
    public void salvarTransportadora(String cnpj, String nome) {
        String cnpjDigits = cnpj == null ? "" : cnpj.replaceAll("\\D", "");
        if (cnpjDigits.length() != 14)
            throw new RegraNegocioException("CNPJ deve ter exatamente 14 dígitos numéricos.");
        Transportadora t = new Transportadora();
        t.setCnpj(cnpjDigits);
        t.setNome(nome);
        salvar(() -> transportadoraRepo.save(t));
    }

    // ---------- Modal (RF03) ----------
    @Transactional
    public void salvarModal(String codigo, TipoModal tipo, String modelo, Integer ano,
                            Integer capacidade, Integer idTransportadora, Integer idAeroportoBase) {
        int anoAtual = LocalDate.now().getYear();
        if (ano == null || ano < 1950 || ano > anoAtual)
            throw new RegraNegocioException("Ano de fabricação do modal deve estar entre 1950 e " + anoAtual + ".");
        if (capacidade == null || capacidade < 1)
            throw new RegraNegocioException("Capacidade do modal deve ser de pelo menos 1 passageiro.");
        Modal m = new Modal();
        m.setCodigo((codigo == null || codigo.isBlank()) ? "MOD" + System.currentTimeMillis() : codigo);
        m.setTipo(tipo);
        m.setModelo(modelo);
        m.setAno(ano);
        m.setCapacidade(capacidade);
        m.setStatus(StatusModal.DISPONIVEL);
        m.setTransportadora(transportadoraRepo.getReferenceById(idTransportadora));
        m.setIdAeroportoBase(idAeroportoBase); // CHECK do banco garante AVIAO → aeroporto
        salvar(() -> modalRepo.save(m));
    }

    // ---------- Rota + trecho (RF + RN10/RN14) ----------
    @Transactional
    public void salvarRota(String codigo, String descricao, Integer idOrigem, Integer idDestino,
                           TipoRota tipo, LocalTime horaPartida, LocalTime horaChegada, Integer tempoMin) {
        salvar(() -> {
            Rota r = new Rota();
            r.setCodigo((codigo == null || codigo.isBlank()) ? "RT" + System.currentTimeMillis() : codigo);
            r.setDescricao(descricao);
            r.setCidadeOrigem(cidadeRepo.getReferenceById(idOrigem));
            r.setCidadeDestino(cidadeRepo.getReferenceById(idDestino));
            r.setTipo(tipo == null ? TipoRota.DIRETA : tipo);
            Rota salva = rotaRepo.saveAndFlush(r);

            // Trecho único (ordem 1) com os horários — usados pela trigger de emissão de ticket
            TrechoRota t = new TrechoRota();
            t.setRota(salva);
            t.setOrdem(1);
            t.setCidadeOrigem(cidadeRepo.getReferenceById(idOrigem));
            t.setCidadeDestino(cidadeRepo.getReferenceById(idDestino));
            if (tempoMin == null || tempoMin < 1)
                throw new RegraNegocioException("Tempo estimado da rota deve ser de pelo menos 1 minuto.");
            t.setHoraPartida(horaPartida);
            t.setHoraChegada(horaChegada);
            t.setTempoEstimadoMin(tempoMin);
            trechoRepo.save(t);
            return salva;
        });
    }

    // ---------- Programação de viagem (cria a viagem vendável) ----------
    @Transactional
    public void salvarProgramacao(Integer idRota, Integer idModal, LocalDate data,
                                  Integer vagas, BigDecimal valor) {
        if (data == null || data.isBefore(LocalDate.now()))
            throw new RegraNegocioException("A data da viagem não pode ser no passado.");
        if (vagas == null || vagas < 1)
            throw new RegraNegocioException("A programação deve ter pelo menos 1 vaga disponível.");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0)
            throw new RegraNegocioException("O valor base da viagem deve ser maior que zero.");
        Modal modal = modalRepo.findById(idModal)
                .orElseThrow(() -> new RegraNegocioException("Modal não encontrado."));
        if (vagas > modal.getCapacidade()) {
            throw new RegraNegocioException("O número de vagas (" + vagas + ") não pode exceder a capacidade do modal (" + modal.getCapacidade() + ").");
        }

        ProgramacaoViagem p = new ProgramacaoViagem();
        p.setRota(rotaRepo.getReferenceById(idRota));
        p.setModal(modal);
        p.setDataViagem(data);
        p.setVagasDisponiveis(vagas);
        p.setValorBase(valor);
        p.setStatus(StatusProgramacao.ATIVO);
        salvar(() -> programacaoRepo.save(p));
    }

    // ---------- Exclusões ----------
    @Transactional public void excluirCidade(Integer id) { salvar(() -> { cidadeRepo.deleteById(id); return null; }); }
    @Transactional public void excluirAeroporto(Integer id) { salvar(() -> { aeroportoRepo.deleteById(id); return null; }); }
    @Transactional public void excluirTransportadora(Integer id) { salvar(() -> { transportadoraRepo.deleteById(id); return null; }); }
    @Transactional public void excluirModal(Integer id) { salvar(() -> { modalRepo.deleteById(id); return null; }); }
    @Transactional public void excluirRota(Integer id) { salvar(() -> { rotaRepo.deleteById(id); return null; }); }
    @Transactional public void excluirProgramacao(Integer id) { salvar(() -> { programacaoRepo.deleteById(id); return null; }); }

    /** Executa a operação traduzindo violações de constraint/trigger em RegraNegocioException. */
    private void salvar(java.util.function.Supplier<?> op) {
        try {
            op.get();
        } catch (RuntimeException e) {
            throw RegraNegocioException.de(e);
        }
    }
}
