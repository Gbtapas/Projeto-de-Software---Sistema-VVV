package com.vvv.reservas.service;

import com.vvv.reservas.dto.BuscaViagemForm;
import com.vvv.reservas.model.entity.Cidade;
import com.vvv.reservas.model.view.ProgramacaoDisponivel;
import com.vvv.reservas.repository.CidadeRepository;
import com.vvv.reservas.repository.ProgramacaoDisponivelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViagemServiceTest {

    @Mock ProgramacaoDisponivelRepository disponiveisRepository;
    @Mock CidadeRepository cidadeRepository;
    @InjectMocks ViagemService service;

    // ------------------------------------------------------------------ buscar

    @Test
    @DisplayName("buscar delega ao repositório com os parâmetros do form")
    void buscar_delegaAoRepositorioComParametrosDoForm() {
        // checando o comportamento esperado
        LocalDate data = LocalDate.of(2026, 8, 15);
        BuscaViagemForm form = new BuscaViagemForm();
        form.setOrigem("sao");
        form.setDestino("rjo");
        form.setData(data);

        when(disponiveisRepository.buscar("SAO", "RJO", data)).thenReturn(List.of());

        List<ProgramacaoDisponivel> resultado = service.buscar(form);

        assertThat(resultado).isEmpty();
        verify(disponiveisRepository).buscar("SAO", "RJO", data);
    }

    @Test
    @DisplayName("buscar converte origem/destino em maiúsculas via BuscaViagemForm")
    void buscar_origemDestinoEmMinusculas_convertidosParaMaiusculas() {
        // mais uma checagem de rotina
        LocalDate data = LocalDate.of(2026, 9, 1);
        BuscaViagemForm form = new BuscaViagemForm();
        form.setOrigem("gru");
        form.setDestino("cwb");
        form.setData(data);

        when(disponiveisRepository.buscar("GRU", "CWB", data)).thenReturn(List.of());

        service.buscar(form);

        verify(disponiveisRepository).buscar("GRU", "CWB", data);
    }

    @Test
    @DisplayName("buscar passa null ao repositório quando origem está em branco")
    void buscar_origemEmBranco_passaNull() {
        // so pra ter certeza que ta pegando o valor certo
        LocalDate data = LocalDate.of(2026, 10, 1);
        BuscaViagemForm form = new BuscaViagemForm();
        form.setOrigem("   ");
        form.setDestino("CWB");
        form.setData(data);

        when(disponiveisRepository.buscar(null, "CWB", data)).thenReturn(List.of());

        service.buscar(form);

        verify(disponiveisRepository).buscar(null, "CWB", data);
    }

    // ------------------------------------------------------------------ listarCidades

    @Test
    @DisplayName("listarCidades retorna cidades ordenadas por nome")
    void listarCidades_delegaAoRepositorio() {
        // so pra ter certeza que ta pegando o valor certo
        Cidade c = new Cidade();
        when(cidadeRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(c));

        List<Cidade> resultado = service.listarCidades();

        assertThat(resultado).hasSize(1);
        verify(cidadeRepository).findAllByOrderByNomeAsc();
    }
}
