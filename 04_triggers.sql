-- ============================================================
--  SISTEMA VAI & VOLTA VIAGENS (VVV)
--  Arquivo 04 — Triggers de Regras de Negócio e Auditoria
-- ============================================================

USE vvv;

DELIMITER //


-- ============================================================
-- BLOCO 1: CONTROLE DE VÍNCULOS (FUNCIONÁRIOS / PDV)
-- ============================================================

-- RN28: Funcionário não pode estar vinculado a mais de 2 PDVs
DROP TRIGGER IF EXISTS trg_fpv_max_dois_pontos //
CREATE TRIGGER trg_fpv_max_dois_pontos
BEFORE INSERT ON funcionarios_pontos_de_venda
FOR EACH ROW
BEGIN
    DECLARE v_count TINYINT DEFAULT 0;

    SELECT COUNT(*) INTO v_count
    FROM   funcionarios_pontos_de_venda
    WHERE  id_funcionario = NEW.id_funcionario
      AND  ativo = 1;

    IF v_count >= 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RN28: Funcionário já está vinculado ao máximo de 2 pontos de venda.';
    END IF;
END //


-- RN26: Gerente designado para PDV deve ser do tipo GERENTE_PDV
DROP TRIGGER IF EXISTS trg_pdv_valida_gerente_ins //
CREATE TRIGGER trg_pdv_valida_gerente_ins
BEFORE INSERT ON pontos_de_venda
FOR EACH ROW
BEGIN
    DECLARE v_tipo VARCHAR(30);

    IF NEW.id_gerente IS NOT NULL THEN
        SELECT tipo INTO v_tipo
        FROM   funcionarios
        WHERE  id_funcionario = NEW.id_gerente;

        IF v_tipo <> 'GERENTE_PDV' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'RN26: O gerente designado deve ter tipo GERENTE_PDV.';
        END IF;
    END IF;
END //

DROP TRIGGER IF EXISTS trg_pdv_valida_gerente_upd //
CREATE TRIGGER trg_pdv_valida_gerente_upd
BEFORE UPDATE ON pontos_de_venda
FOR EACH ROW
BEGIN
    DECLARE v_tipo VARCHAR(30);

    IF NEW.id_gerente IS NOT NULL AND
       (OLD.id_gerente IS NULL OR NEW.id_gerente <> OLD.id_gerente) THEN

        SELECT tipo INTO v_tipo
        FROM   funcionarios
        WHERE  id_funcionario = NEW.id_gerente;

        IF v_tipo <> 'GERENTE_PDV' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'RN26: O gerente designado deve ter tipo GERENTE_PDV.';
        END IF;
    END IF;
END //


-- RN31: Gerente virtual em vendas_online deve ser do tipo GERENTE_VIRTUAL
DROP TRIGGER IF EXISTS trg_vo_valida_gerente_ins //
CREATE TRIGGER trg_vo_valida_gerente_ins
BEFORE INSERT ON vendas_online
FOR EACH ROW
BEGIN
    DECLARE v_tipo VARCHAR(30);

    SELECT tipo INTO v_tipo
    FROM   funcionarios
    WHERE  id_funcionario = NEW.id_gerente_virtual;

    IF v_tipo <> 'GERENTE_VIRTUAL' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RN31: O responsável por vendas online deve ser do tipo GERENTE_VIRTUAL.';
    END IF;
END //


-- ============================================================
-- BLOCO 2: VALIDAÇÕES DE RESERVA
-- (RN03, RN04, RN05, RN07, RN18, RI01)
-- ============================================================

DROP TRIGGER IF EXISTS trg_reserva_before_insert //
CREATE TRIGGER trg_reserva_before_insert
BEFORE INSERT ON reservas
FOR EACH ROW
BEGIN
    DECLARE v_nasc_passageiro   DATE;
    DECLARE v_idade             INT  DEFAULT 0;
    DECLARE v_nasc_acompanhante DATE;
    DECLARE v_idade_acomp       INT  DEFAULT 0;
    DECLARE v_vagas             SMALLINT DEFAULT 0;
    DECLARE v_status_prog       VARCHAR(20);
    DECLARE v_status_modal      VARCHAR(20);

    -- -------------------------------------------------------
    -- Valida idade do passageiro
    -- -------------------------------------------------------
    SELECT data_nascimento INTO v_nasc_passageiro
    FROM   passageiros
    WHERE  id_passageiro = NEW.id_passageiro;

    SET v_idade = TIMESTAMPDIFF(YEAR, v_nasc_passageiro, CURDATE());

    -- RN03: menores de 2 anos não podem viajar
    IF v_idade < 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RN03: Passageiros com menos de 2 anos não podem realizar viagens.';
    END IF;

    -- RN04: entre 2 e 10 anos exige acompanhante adulto (> 21 anos)
    IF v_idade BETWEEN 2 AND 10 THEN
        IF NEW.id_acompanhante IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'RN04: Passageiros entre 2 e 10 anos precisam de acompanhante cadastrado no sistema.';
        END IF;

        SELECT data_nascimento INTO v_nasc_acompanhante
        FROM   passageiros
        WHERE  id_passageiro = NEW.id_acompanhante;

        SET v_idade_acomp = TIMESTAMPDIFF(YEAR, v_nasc_acompanhante, CURDATE());

        IF v_idade_acomp < 21 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'RN04: O acompanhante deve ter mais de 21 anos.';
        END IF;

        -- RN04: aplica desconto de 40% automaticamente
        SET NEW.valor_desconto = ROUND(NEW.valor_bruto * 0.40, 2);
        SET NEW.valor_total    = ROUND(NEW.valor_bruto - NEW.valor_desconto, 2);
    ELSE
        -- RN05: maiores de 10 anos pagam valor integral
        -- Recalcula total respeitando desconto informado (ex: promoção manual)
        SET NEW.valor_total = ROUND(NEW.valor_bruto - NEW.valor_desconto, 2);
    END IF;

    -- -------------------------------------------------------
    -- Valida capacidade e disponibilidade da programação
    -- -------------------------------------------------------
    SELECT pv.vagas_disponiveis, pv.status, m.status
    INTO   v_vagas, v_status_prog, v_status_modal
    FROM   programacoes_viagem pv
    JOIN   modais m ON m.id_modal = pv.id_modal
    WHERE  pv.id_programacao = NEW.id_programacao;

    -- RN18: modal em manutenção bloqueia novas reservas
    IF v_status_modal = 'MANUTENCAO' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RN18: O modal está em manutenção e não aceita reservas.';
    END IF;

    IF v_status_modal = 'INATIVO' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Modal inativo. Não é possível realizar reservas.';
    END IF;

    IF v_status_prog <> 'ATIVO' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RN07: A programação de viagem selecionada não está disponível.';
    END IF;

    -- RI01 / RN07: sem vagas = overbooking prevenido
    IF v_vagas <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RI01/RN07: Não há vagas disponíveis. Overbooking prevenido.';
    END IF;

    -- -------------------------------------------------------
    -- Auto-gera código da reserva se não fornecido
    -- -------------------------------------------------------
    IF NEW.codigo IS NULL OR TRIM(NEW.codigo) = '' THEN
        SET NEW.codigo = CONCAT(
            'RES',
            DATE_FORMAT(NOW(), '%Y%m%d'),
            LPAD(FLOOR(RAND() * 999999), 6, '0')
        );
    END IF;
END //


-- Decrementa vagas após criação de reserva (a reserva pendente já ocupa a vaga — RI01)
DROP TRIGGER IF EXISTS trg_reserva_after_insert //
CREATE TRIGGER trg_reserva_after_insert
AFTER INSERT ON reservas
FOR EACH ROW
BEGIN
    UPDATE programacoes_viagem
    SET    vagas_disponiveis = vagas_disponiveis - 1
    WHERE  id_programacao = NEW.id_programacao;
END //


-- Libera vaga quando reserva é cancelada
DROP TRIGGER IF EXISTS trg_reserva_after_update //
CREATE TRIGGER trg_reserva_after_update
AFTER UPDATE ON reservas
FOR EACH ROW
BEGIN
    IF NEW.status = 'CANCELADA' AND OLD.status <> 'CANCELADA' THEN
        UPDATE programacoes_viagem
        SET    vagas_disponiveis = vagas_disponiveis + 1
        WHERE  id_programacao = NEW.id_programacao;
    END IF;
END //


-- ============================================================
-- BLOCO 3: PAGAMENTOS
-- (RN20, RN21 — juros de parcelamento)
-- ============================================================

DROP TRIGGER IF EXISTS trg_pagamento_before_insert //
CREATE TRIGGER trg_pagamento_before_insert
BEFORE INSERT ON pagamentos
FOR EACH ROW
BEGIN
    IF NEW.tipo = 'DEBITO' THEN
        -- RN19: débito não pode ser parcelado
        SET NEW.parcelas     = 1;
        SET NEW.valor_juros  = 0.00;
        SET NEW.valor_total  = ROUND(NEW.valor_bruto, 2);
        SET NEW.valor_parcela = ROUND(NEW.valor_bruto, 2);

    ELSEIF NEW.tipo = 'CREDITO' AND NEW.parcelas > 4 THEN
        -- RN21: acima de 4 parcelas, aplica 5% de juros
        SET NEW.valor_juros   = ROUND(NEW.valor_bruto * 0.05, 2);
        SET NEW.valor_total   = ROUND(NEW.valor_bruto + NEW.valor_juros, 2);
        SET NEW.valor_parcela = ROUND(NEW.valor_total / NEW.parcelas, 2);

    ELSE
        -- RN20: até 4 parcelas no crédito, sem juros
        SET NEW.valor_juros   = 0.00;
        SET NEW.valor_total   = ROUND(NEW.valor_bruto, 2);
        SET NEW.valor_parcela = ROUND(NEW.valor_bruto / NEW.parcelas, 2);
    END IF;
END //


-- ============================================================
-- BLOCO 4: CONFIRMAÇÃO DE RESERVA E EMISSÃO DE TICKET
-- (RN22, RN23)
-- ============================================================

DROP TRIGGER IF EXISTS trg_pagamento_after_update //
CREATE TRIGGER trg_pagamento_after_update
AFTER UPDATE ON pagamentos
FOR EACH ROW
BEGIN
    DECLARE v_id_rota         INT;
    DECLARE v_hora_partida    TIME;
    DECLARE v_hora_chegada    TIME;
    DECLARE v_tempo_est       SMALLINT;
    DECLARE v_codigo_ticket   VARCHAR(30);
    DECLARE v_localizador     CHAR(8);

    IF NEW.status = 'APROVADO' AND OLD.status <> 'APROVADO' THEN

        -- RN22: confirma a reserva automaticamente
        UPDATE reservas
        SET    status = 'CONFIRMADA'
        WHERE  id_reserva = NEW.id_reserva;

        -- Busca dados de horário da rota para compor o ticket
        SELECT
            pv.id_rota,
            (SELECT hora_partida
             FROM   trechos_rota
             WHERE  id_rota = pv.id_rota
             ORDER BY ordem ASC
             LIMIT 1),
            (SELECT hora_chegada
             FROM   trechos_rota
             WHERE  id_rota = pv.id_rota
             ORDER BY ordem DESC
             LIMIT 1),
            (SELECT COALESCE(SUM(tempo_estimado_min), 0)
             FROM   trechos_rota
             WHERE  id_rota = pv.id_rota)
        INTO v_id_rota, v_hora_partida, v_hora_chegada, v_tempo_est
        FROM  reservas r
        JOIN  programacoes_viagem pv ON pv.id_programacao = r.id_programacao
        WHERE r.id_reserva = NEW.id_reserva;

        -- Gera código do ticket e localizador únicos
        SET v_codigo_ticket = CONCAT('TKT', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(NEW.id_pagamento, 8, '0'));
        SET v_localizador   = UPPER(LEFT(MD5(CONCAT(NEW.id_reserva, RAND(), NOW())), 8));

        -- RN23: emite ticket automaticamente
        INSERT INTO tickets (
            codigo_ticket,
            localizador,
            data_emissao,
            tipo_passagem,
            hora_partida,
            hora_chegada,
            tempo_estimado_min,
            status,
            id_reserva,
            id_pagamento
        ) VALUES (
            v_codigo_ticket,
            v_localizador,
            NOW(),
            'ECONOMICA',        -- tipo padrão; app pode atualizar depois
            v_hora_partida,
            v_hora_chegada,
            v_tempo_est,
            'ATIVO',
            NEW.id_reserva,
            NEW.id_pagamento
        );
    END IF;

    -- Cancela reserva quando pagamento é definitivamente recusado (após 3 tentativas, por ex.)
    -- Isso é opcional e depende da lógica de negócio; deixado como UPDATE manual pela app.
END //


-- ============================================================
-- BLOCO 5: MANUTENÇÃO DE MODAIS
-- (RN17, RN18)
-- ============================================================

-- Ao registrar manutenção com início hoje ou passado, bloqueia o modal imediatamente
DROP TRIGGER IF EXISTS trg_manutencao_after_insert //
CREATE TRIGGER trg_manutencao_after_insert
AFTER INSERT ON manutencoes
FOR EACH ROW
BEGIN
    IF NEW.status IN ('AGENDADA', 'EM_ANDAMENTO') AND NEW.data_inicio <= CURDATE() THEN
        UPDATE modais
        SET    status = 'MANUTENCAO'
        WHERE  id_modal = NEW.id_modal;
    END IF;
END //


-- Ao concluir ou cancelar manutenção, verifica se há outra ativa antes de liberar o modal
DROP TRIGGER IF EXISTS trg_manutencao_after_update //
CREATE TRIGGER trg_manutencao_after_update
AFTER UPDATE ON manutencoes
FOR EACH ROW
BEGIN
    DECLARE v_outras_ativas INT DEFAULT 0;

    IF NEW.status IN ('CONCLUIDA', 'CANCELADA') AND
       OLD.status NOT IN ('CONCLUIDA', 'CANCELADA') THEN

        -- Verifica se existe outra manutenção ativa para o mesmo modal
        SELECT COUNT(*) INTO v_outras_ativas
        FROM   manutencoes
        WHERE  id_modal  = NEW.id_modal
          AND  status    IN ('AGENDADA', 'EM_ANDAMENTO')
          AND  id_manutencao <> NEW.id_manutencao;

        IF v_outras_ativas = 0 THEN
            -- Sem outras manutenções ativas: libera o modal
            UPDATE modais
            SET    status = 'DISPONIVEL'
            WHERE  id_modal = NEW.id_modal;
        END IF;
    END IF;
END //


-- ============================================================
-- BLOCO 6: INTEGRIDADE DE VENDAS
-- (RI12)
-- ============================================================

-- RI12: impede exclusão física de vendas confirmadas (deve ser cancelamento)
DROP TRIGGER IF EXISTS trg_vendas_no_delete_confirmada //
CREATE TRIGGER trg_vendas_no_delete_confirmada
BEFORE DELETE ON vendas
FOR EACH ROW
BEGIN
    IF OLD.status = 'CONFIRMADA' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'RI12: Vendas confirmadas não podem ser excluídas. Registre um cancelamento.';
    END IF;
END //


-- ============================================================
-- BLOCO 7: TRIGGERS DE AUDITORIA
-- Tabelas críticas: reservas, pagamentos, tickets, vendas, modais, funcionarios
-- ============================================================

-- ---- reservas -----------------------------------------------
DROP TRIGGER IF EXISTS trg_audit_reservas_ins //
CREATE TRIGGER trg_audit_reservas_ins
AFTER INSERT ON reservas
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
    VALUES (
        'reservas', NEW.id_reserva, 'INSERT', NULL,
        JSON_OBJECT(
            'codigo',     NEW.codigo,
            'status',     NEW.status,
            'canal',      NEW.canal,
            'valor_total', NEW.valor_total,
            'id_passageiro', NEW.id_passageiro,
            'id_programacao', NEW.id_programacao
        )
    );
END //

DROP TRIGGER IF EXISTS trg_audit_reservas_upd //
CREATE TRIGGER trg_audit_reservas_upd
AFTER UPDATE ON reservas
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status OR OLD.valor_total <> NEW.valor_total THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES (
            'reservas', NEW.id_reserva, 'UPDATE',
            JSON_OBJECT('status', OLD.status, 'valor_total', OLD.valor_total),
            JSON_OBJECT('status', NEW.status, 'valor_total', NEW.valor_total)
        );
    END IF;
END //

DROP TRIGGER IF EXISTS trg_audit_reservas_del //
CREATE TRIGGER trg_audit_reservas_del
BEFORE DELETE ON reservas
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
    VALUES (
        'reservas', OLD.id_reserva, 'DELETE',
        JSON_OBJECT('codigo', OLD.codigo, 'status', OLD.status),
        NULL
    );
END //


-- ---- pagamentos ---------------------------------------------
DROP TRIGGER IF EXISTS trg_audit_pagamentos_ins //
CREATE TRIGGER trg_audit_pagamentos_ins
AFTER INSERT ON pagamentos
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
    VALUES (
        'pagamentos', NEW.id_pagamento, 'INSERT', NULL,
        JSON_OBJECT(
            'tipo',        NEW.tipo,
            'parcelas',    NEW.parcelas,
            'valor_total', NEW.valor_total,
            'status',      NEW.status,
            'id_reserva',  NEW.id_reserva
        )
    );
END //

DROP TRIGGER IF EXISTS trg_audit_pagamentos_upd //
CREATE TRIGGER trg_audit_pagamentos_upd
AFTER UPDATE ON pagamentos
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES (
            'pagamentos', NEW.id_pagamento, 'UPDATE',
            JSON_OBJECT('status', OLD.status, 'codigo_autorizacao', OLD.codigo_autorizacao),
            JSON_OBJECT('status', NEW.status, 'codigo_autorizacao', NEW.codigo_autorizacao,
                        'data_pagamento', NEW.data_pagamento)
        );
    END IF;
END //


-- ---- tickets ------------------------------------------------
DROP TRIGGER IF EXISTS trg_audit_tickets_ins //
CREATE TRIGGER trg_audit_tickets_ins
AFTER INSERT ON tickets
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
    VALUES (
        'tickets', NEW.id_ticket, 'INSERT', NULL,
        JSON_OBJECT(
            'codigo_ticket', NEW.codigo_ticket,
            'localizador',   NEW.localizador,
            'status',        NEW.status,
            'id_reserva',    NEW.id_reserva
        )
    );
END //

DROP TRIGGER IF EXISTS trg_audit_tickets_upd //
CREATE TRIGGER trg_audit_tickets_upd
AFTER UPDATE ON tickets
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES (
            'tickets', NEW.id_ticket, 'UPDATE',
            JSON_OBJECT('status', OLD.status),
            JSON_OBJECT('status', NEW.status)
        );
    END IF;
END //


-- ---- vendas -------------------------------------------------
DROP TRIGGER IF EXISTS trg_audit_vendas_upd //
CREATE TRIGGER trg_audit_vendas_upd
AFTER UPDATE ON vendas
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES (
            'vendas', NEW.id_venda, 'UPDATE',
            JSON_OBJECT('status', OLD.status),
            JSON_OBJECT('status', NEW.status)
        );
    END IF;
END //


-- ---- modais -------------------------------------------------
DROP TRIGGER IF EXISTS trg_audit_modais_upd //
CREATE TRIGGER trg_audit_modais_upd
AFTER UPDATE ON modais
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES (
            'modais', NEW.id_modal, 'UPDATE',
            JSON_OBJECT('status', OLD.status, 'capacidade', OLD.capacidade),
            JSON_OBJECT('status', NEW.status, 'capacidade', NEW.capacidade)
        );
    END IF;
END //


-- ---- funcionarios -------------------------------------------
DROP TRIGGER IF EXISTS trg_audit_funcionarios_ins //
CREATE TRIGGER trg_audit_funcionarios_ins
AFTER INSERT ON funcionarios
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
    VALUES (
        'funcionarios', NEW.id_funcionario, 'INSERT', NULL,
        JSON_OBJECT('codigo', NEW.codigo, 'tipo', NEW.tipo, 'nome', NEW.nome)
    );
END //

DROP TRIGGER IF EXISTS trg_audit_funcionarios_upd //
CREATE TRIGGER trg_audit_funcionarios_upd
AFTER UPDATE ON funcionarios
FOR EACH ROW
BEGIN
    IF OLD.tipo <> NEW.tipo OR OLD.ativo <> NEW.ativo THEN
        INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos)
        VALUES (
            'funcionarios', NEW.id_funcionario, 'UPDATE',
            JSON_OBJECT('tipo', OLD.tipo, 'ativo', OLD.ativo),
            JSON_OBJECT('tipo', NEW.tipo, 'ativo', NEW.ativo)
        );
    END IF;
END //


DELIMITER ;
