package com.vvv.reservas.service;

import com.vvv.reservas.model.entity.Manutencao;
import com.vvv.reservas.model.entity.Modal;
import com.vvv.reservas.model.entity.Transportadora;
import com.vvv.reservas.model.enums.OperacaoAuditoria;
import com.vvv.reservas.model.enums.StatusManutencao;
import com.vvv.reservas.repository.ManutencaoRepository;
import com.vvv.reservas.repository.ModalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManutencaoServiceTest {

    @Mock ManutencaoRepository manutencaoRepo;
    @Mock ModalRepository modalRepo;
    @Mock AuditoriaService auditoria;
    @Mock TransferenciaService transferenciaService;
    @InjectMocks ManutencaoService service;

    // ------------------------------------------------------------------ listar

    @Test
    @DisplayName("listar delega ao repositório ordenado por dataInicio DESC")
    void listar_delegaAoRepositorio() {
        // verificando se ta tudo certo
        Manutencao m = new Manutencao();
        when(manutencaoRepo.findAllByOrderByDataInicioDesc()).thenReturn(List.of(m));

        List<Manutencao> resultado = service.listar();

        assertThat(resultado).hasSize(1);
        verify(manutencaoRepo).findAllByOrderByDataInicioDesc();
    }

    // ------------------------------------------------------------------ agendar

    @Test
    @DisplayName("agendar salva manutenção e registra auditoria e notificação")
    void agendar_sucesso_salvaAuditaENotifica() throws Exception {
        // teste super importante
        LocalDate inicio = LocalDate.of(2026, 7, 1);
        LocalDate fim    = LocalDate.of(2026, 7, 10);

        Transportadora transportadora = new Transportadora();
        Modal modal = new Modal();
        modal.setTransportadora(transportadora);

        Manutencao salva = new Manutencao();
        salva.setModal(modal);
        salva.setDataInicio(inicio);
        salva.setDataFim(fim);
        java.lang.reflect.Field idField = Manutencao.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(salva, 1);

        when(modalRepo.getReferenceById(1)).thenReturn(modal);
        when(manutencaoRepo.save(any())).thenReturn(salva);

        service.agendar(1, inicio, fim, "Revisão completa");

        ArgumentCaptor<Manutencao> captor = ArgumentCaptor.forClass(Manutencao.class);
        verify(manutencaoRepo).save(captor.capture());
        assertThat(captor.getValue().getDataInicio()).isEqualTo(inicio);
        assertThat(captor.getValue().getDataFim()).isEqualTo(fim);
        assertThat(captor.getValue().getDescricao()).isEqualTo("Revisão completa");

        verify(auditoria).registrar(eq("manutencoes"), any(), eq(OperacaoAuditoria.INSERT), eq(null), anyString());
        verify(transferenciaService).notificarManutencao(any(), eq("Revisão completa"));
    }

    @Test
    @DisplayName("agendar usa texto padrão na notificação quando descricao é null")
    void agendar_descricaoNull_usaTextoPadrao() throws Exception {
        // conferindo os valores retornados
        Transportadora transportadora = new Transportadora();
        Modal modal = new Modal();
        modal.setTransportadora(transportadora);

        Manutencao salva = new Manutencao();
        salva.setModal(modal);
        java.lang.reflect.Field idField = Manutencao.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(salva, 2);

        when(modalRepo.getReferenceById(2)).thenReturn(modal);
        when(manutencaoRepo.save(any())).thenReturn(salva);

        service.agendar(2, LocalDate.now(), LocalDate.now().plusDays(5), null);

        verify(transferenciaService).notificarManutencao(any(), eq("Manutenção agendada"));
    }

    // ------------------------------------------------------------------ mudarStatus

    @Test
    @DisplayName("mudarStatus lança exception quando manutenção não encontrada")
    void mudarStatus_naoEncontrado_lancaException() {
        // so pra ter certeza que ta pegando o valor certo
        when(manutencaoRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.mudarStatus(99, StatusManutencao.CONCLUIDA))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não encontrada");
    }

    @Test
    @DisplayName("mudarStatus atualiza o status da manutenção e persiste")
    void mudarStatus_sucesso_atualizaStatus() {
        // se passar isso o resto vai de boa
        Manutencao m = new Manutencao();
        m.setStatus(StatusManutencao.EM_ANDAMENTO);
        when(manutencaoRepo.findById(1)).thenReturn(Optional.of(m));
        when(manutencaoRepo.saveAndFlush(any())).thenReturn(m);

        service.mudarStatus(1, StatusManutencao.CONCLUIDA);

        assertThat(m.getStatus()).isEqualTo(StatusManutencao.CONCLUIDA);
        verify(manutencaoRepo).saveAndFlush(m);
    }
}
