-- ============================================================
--  SISTEMA VAI & VOLTA VIAGENS (VVV)
--  Arquivo 03 — Índices e Views
-- ============================================================

USE vvv;


-- ============================================================
-- ÍNDICES DE PERFORMANCE
-- (PKs e FKs já geram índices automaticamente no InnoDB)
-- ============================================================

-- ---- reservas -----------------------------------------------
-- Consultas mais frequentes: por passageiro, por status, por data, por canal
CREATE INDEX idx_reservas_passageiro   ON reservas (id_passageiro);
CREATE INDEX idx_reservas_status       ON reservas (status);
CREATE INDEX idx_reservas_canal        ON reservas (canal);
CREATE INDEX idx_reservas_data_criacao ON reservas (data_criacao);
CREATE INDEX idx_reservas_programacao  ON reservas (id_programacao);

-- ---- programacoes_viagem ------------------------------------
-- Busca de viagens disponíveis: por data e rota (tela de busca — UC02)
CREATE INDEX idx_prog_data_viagem   ON programacoes_viagem (data_viagem);
CREATE INDEX idx_prog_rota_data     ON programacoes_viagem (id_rota, data_viagem);
CREATE INDEX idx_prog_modal_data    ON programacoes_viagem (id_modal, data_viagem);
CREATE INDEX idx_prog_status        ON programacoes_viagem (status);

-- ---- rotas --------------------------------------------------
CREATE INDEX idx_rotas_origem_destino ON rotas (id_cidade_origem, id_cidade_destino);
CREATE INDEX idx_rotas_tipo           ON rotas (tipo);

-- ---- passageiros --------------------------------------------
-- Busca por nome e CPF (cadastro, validação)
CREATE INDEX idx_passageiros_nome      ON passageiros (nome);
CREATE INDEX idx_passageiros_nasc      ON passageiros (data_nascimento)
    COMMENT 'Suporta cálculo de faixa etária (RN03, RN04, RN05)';

-- ---- funcionarios -------------------------------------------
CREATE INDEX idx_funcionarios_tipo ON funcionarios (tipo);
CREATE INDEX idx_funcionarios_nome ON funcionarios (nome);

-- ---- modais -------------------------------------------------
CREATE INDEX idx_modais_tipo           ON modais (tipo);
CREATE INDEX idx_modais_status         ON modais (status);
CREATE INDEX idx_modais_transportadora ON modais (id_transportadora);

-- ---- manutencoes --------------------------------------------
-- Consulta de disponibilidade: modal em manutenção em determinado período
CREATE INDEX idx_manut_modal_periodo ON manutencoes (id_modal, data_inicio, data_fim);
CREATE INDEX idx_manut_status        ON manutencoes (status);

-- ---- tickets ------------------------------------------------
CREATE INDEX idx_tickets_status ON tickets (status);

-- ---- pagamentos ---------------------------------------------
CREATE INDEX idx_pagamentos_status ON pagamentos (status);
CREATE INDEX idx_pagamentos_data   ON pagamentos (data_pagamento);

-- ---- vendas -------------------------------------------------
CREATE INDEX idx_vendas_status ON vendas (status);
CREATE INDEX idx_vendas_data   ON vendas (data_venda);

-- ---- log_auditoria ------------------------------------------
-- Índices já definidos inline na DDL (arquivo 02), mas complementamos:
CREATE INDEX idx_log_tabela_registro ON log_auditoria (tabela, id_registro);
CREATE INDEX idx_log_data_hora       ON log_auditoria (data_hora);
CREATE INDEX idx_log_usuario         ON log_auditoria (id_usuario);
CREATE INDEX idx_log_operacao        ON log_auditoria (operacao);


-- ============================================================
-- VIEWS OPERACIONAIS
-- ============================================================

-- ---- vw_passageiros_com_idade -------------------------------
-- Evita calcular TIMESTAMPDIFF em todo lugar da aplicação
CREATE OR REPLACE VIEW vw_passageiros_com_idade AS
SELECT
    p.*,
    TIMESTAMPDIFF(YEAR, p.data_nascimento, CURDATE()) AS idade
FROM passageiros p
WHERE p.ativo = 1;


-- ---- vw_programacoes_disponiveis ----------------------------
-- Tela de busca de viagens (UC02): já filtra vagas > 0, status ATIVO, data futura, modal OK
CREATE OR REPLACE VIEW vw_programacoes_disponiveis AS
SELECT
    pv.id_programacao,
    ro.codigo           AS codigo_rota,
    ro.descricao        AS descricao_rota,
    ro.tipo             AS tipo_rota,
    c_orig.nome         AS cidade_origem,
    c_orig.identificador AS cod_origem,
    c_dest.nome         AS cidade_destino,
    c_dest.identificador AS cod_destino,
    pv.data_viagem,
    m.tipo              AS tipo_modal,
    m.modelo            AS modelo_modal,
    m.capacidade        AS capacidade_total,
    pv.vagas_disponiveis,
    pv.valor_base,
    t.nome              AS transportadora
FROM  programacoes_viagem pv
JOIN  rotas           ro     ON ro.id_rota           = pv.id_rota
JOIN  cidades         c_orig ON c_orig.id_cidade      = ro.id_cidade_origem
JOIN  cidades         c_dest ON c_dest.id_cidade      = ro.id_cidade_destino
JOIN  modais          m      ON m.id_modal             = pv.id_modal
JOIN  transportadoras t      ON t.id_transportadora    = m.id_transportadora
WHERE pv.status          = 'ATIVO'
  AND pv.data_viagem    >= CURDATE()
  AND pv.vagas_disponiveis > 0
  AND m.status           = 'DISPONIVEL'
  AND ro.ativo           = 1;


-- ---- vw_reservas_completas ----------------------------------
-- Visão gerencial de reservas com todos os dados relevantes
CREATE OR REPLACE VIEW vw_reservas_completas AS
SELECT
    r.id_reserva,
    r.codigo              AS codigo_reserva,
    r.data_criacao,
    r.status              AS status_reserva,
    r.canal,
    r.valor_bruto,
    r.valor_desconto,
    r.valor_total,
    -- Passageiro
    p.nome                AS nome_passageiro,
    p.cpf                 AS cpf_passageiro,
    TIMESTAMPDIFF(YEAR, p.data_nascimento, CURDATE()) AS idade_passageiro,
    -- Acompanhante (se houver)
    ac.nome               AS nome_acompanhante,
    ac.cpf                AS cpf_acompanhante,
    -- Viagem
    pv.data_viagem,
    ro.descricao          AS rota_descricao,
    c_orig.nome           AS cidade_origem,
    c_orig.identificador  AS cod_origem,
    c_dest.nome           AS cidade_destino,
    c_dest.identificador  AS cod_destino,
    -- Modal
    m.tipo                AS tipo_modal,
    m.modelo              AS modelo_modal,
    trans.nome            AS transportadora,
    -- Pagamento
    pg.tipo               AS tipo_pagamento,
    pg.parcelas,
    pg.valor_juros,
    pg.valor_total        AS valor_pago,
    pg.status             AS status_pagamento,
    -- Ticket
    tk.codigo_ticket,
    tk.localizador,
    tk.status             AS status_ticket
FROM  reservas            r
JOIN  passageiros         p      ON p.id_passageiro      = r.id_passageiro
JOIN  programacoes_viagem pv     ON pv.id_programacao    = r.id_programacao
JOIN  rotas               ro     ON ro.id_rota            = pv.id_rota
JOIN  cidades             c_orig ON c_orig.id_cidade      = ro.id_cidade_origem
JOIN  cidades             c_dest ON c_dest.id_cidade      = ro.id_cidade_destino
JOIN  modais              m      ON m.id_modal             = pv.id_modal
JOIN  transportadoras     trans  ON trans.id_transportadora = m.id_transportadora
LEFT JOIN passageiros     ac     ON ac.id_passageiro       = r.id_acompanhante
LEFT JOIN pagamentos      pg     ON pg.id_reserva          = r.id_reserva
LEFT JOIN tickets         tk     ON tk.id_reserva          = r.id_reserva;


-- ---- vw_funcionarios_pontos_de_venda ------------------------
-- Exibe funcionários com seus PDVs ativos (facilita validação RN28)
-- Corrigido: subquery para total_pontos_ativos — o LEFT JOIN duplicado causava
-- contagem errada no window function (produto cartesiano 2×2 = 4 ao invés de 2)
CREATE OR REPLACE VIEW vw_funcionarios_pontos_de_venda AS
SELECT
    f.id_funcionario,
    f.codigo                                    AS codigo_funcionario,
    f.nome                                      AS nome_funcionario,
    f.tipo,
    pdv.id_ponto,
    pdv.codigo                                  AS codigo_ponto,
    pdv.nome                                    AS nome_ponto,
    fpv.data_inicio,
    fpv.data_fim,
    fpv.ativo                                   AS vinculo_ativo,
    (
        SELECT COUNT(*)
        FROM   funcionarios_pontos_de_venda x
        WHERE  x.id_funcionario = f.id_funcionario
          AND  x.ativo = 1
    )                                           AS total_pontos_ativos
FROM  funcionarios                    f
JOIN  funcionarios_pontos_de_venda    fpv ON fpv.id_funcionario = f.id_funcionario
                                        AND fpv.ativo = 1
JOIN  pontos_de_venda                 pdv ON pdv.id_ponto        = fpv.id_ponto
WHERE f.ativo = 1;


-- ---- vw_modais_em_manutencao --------------------------------
-- Modais que estão em manutenção hoje ou têm manutenção agendada para hoje
CREATE OR REPLACE VIEW vw_modais_em_manutencao AS
SELECT
    m.id_modal,
    m.codigo,
    m.tipo,
    m.modelo,
    t.nome      AS transportadora,
    mn.id_manutencao,
    mn.data_inicio,
    mn.data_fim,
    mn.descricao,
    mn.status   AS status_manutencao
FROM  modais         m
JOIN  transportadoras t  ON t.id_transportadora = m.id_transportadora
JOIN  manutencoes    mn  ON mn.id_modal          = m.id_modal
WHERE mn.status IN ('AGENDADA', 'EM_ANDAMENTO')
  AND mn.data_inicio <= CURDATE()
  AND mn.data_fim    >= CURDATE();


-- ---- vw_relatorio_vendas ------------------------------------
-- Base para relatórios gerenciais (UC10)
CREATE OR REPLACE VIEW vw_relatorio_vendas AS
SELECT
    v.id_venda,
    v.data_venda,
    v.status        AS status_venda,
    v.valor_total,
    r.canal,
    r.codigo        AS codigo_reserva,
    r.status        AS status_reserva,
    p.nome          AS nome_passageiro,
    pv_prog.data_viagem,
    ro.descricao    AS rota,
    c_orig.nome     AS origem,
    c_dest.nome     AS destino,
    m.tipo          AS modal,
    -- Presencial
    vp.id_ponto,
    pdv.nome        AS ponto_de_venda,
    fp.nome         AS nome_funcionario,
    vp.data_confirmacao,
    -- Online
    vo.status_aprovacao,
    vo.data_aprovacao,
    fgv.nome        AS gerente_virtual
FROM  vendas               v
JOIN  reservas             r      ON r.id_reserva         = v.id_reserva
JOIN  passageiros          p      ON p.id_passageiro       = r.id_passageiro
JOIN  programacoes_viagem  pv_prog ON pv_prog.id_programacao = r.id_programacao
JOIN  rotas                ro     ON ro.id_rota             = pv_prog.id_rota
JOIN  cidades              c_orig ON c_orig.id_cidade       = ro.id_cidade_origem
JOIN  cidades              c_dest ON c_dest.id_cidade       = ro.id_cidade_destino
JOIN  modais               m      ON m.id_modal              = pv_prog.id_modal
LEFT JOIN vendas_presenciais vp   ON vp.id_venda             = v.id_venda
LEFT JOIN pontos_de_venda   pdv   ON pdv.id_ponto             = vp.id_ponto
LEFT JOIN funcionarios      fp    ON fp.id_funcionario        = vp.id_funcionario
LEFT JOIN vendas_online     vo    ON vo.id_venda              = v.id_venda
LEFT JOIN funcionarios      fgv   ON fgv.id_funcionario       = vo.id_gerente_virtual;
