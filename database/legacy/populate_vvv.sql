-- =============================================================================
-- SCRIPT DE POPULAÇÃO DO BANCO DE DADOS `vvv`
-- Sistema de Reservas de Viagens Multi-Modal (Avião, Trem, Ônibus, Navio)
-- Versão idempotente: pode ser executado múltiplas vezes sem erro.
--   * INSERT IGNORE nas tabelas de referência (cidades, aeroportos, etc.)
--   * Trechos e vínculos protegidos por INSERT IGNORE na UNIQUE KEY
--   * Reservas/pagamentos/vendas: sempre novos registros (comportamento esperado)
-- =============================================================================
-- ATENÇÃO: execute APÓS o 05_seed.sql (dados iniciais já carregados).
-- NÃO insere diretamente em `tickets` — gerados por trigger após pagamento.
-- Código de reserva gerado por trigger (campo codigo = NULL no INSERT).
-- =============================================================================

USE vvv;

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- ===== SEÇÃO 1: CIDADES (5 brasileiras + 3 internacionais) =====
-- =============================================================================
-- INSERT IGNORE: pula silenciosamente se o identificador já existir.
-- O seed já usa: RIO SAO BSB SSA FOR MAO POA REC CWB BEL EZE LIS MIA CDG LHR

INSERT IGNORE INTO cidades (nome, estado, pais, identificador) VALUES
  ('Florianópolis', 'Santa Catarina',     'Brasil',         'FLN'),
  ('Goiânia',       'Goiás',              'Brasil',         'GYN'),
  ('Vitória',       'Espírito Santo',     'Brasil',         'VIX'),
  ('Natal',         'Rio Grande do Norte','Brasil',         'NAT'),
  ('Cuiabá',        'Mato Grosso',        'Brasil',         'CGB'),
  ('Madrid',        NULL,                 'Espanha',        'MAD'),
  ('Roma',          NULL,                 'Itália',         'ROM'),
  ('Nova York',     'New York',           'Estados Unidos', 'NYC');

-- Captura IDs pelo identificador único (funciona quer o INSERT tenha rodado ou não)
SELECT id_cidade INTO @id_flp FROM cidades WHERE identificador = 'FLN';
SELECT id_cidade INTO @id_gyn FROM cidades WHERE identificador = 'GYN';
SELECT id_cidade INTO @id_vix FROM cidades WHERE identificador = 'VIX';
SELECT id_cidade INTO @id_nat FROM cidades WHERE identificador = 'NAT';
SELECT id_cidade INTO @id_cgb FROM cidades WHERE identificador = 'CGB';
SELECT id_cidade INTO @id_mad FROM cidades WHERE identificador = 'MAD';
SELECT id_cidade INTO @id_rom FROM cidades WHERE identificador = 'ROM';
SELECT id_cidade INTO @id_nyc FROM cidades WHERE identificador = 'NYC';

-- IDs de cidades do seed
SELECT id_cidade INTO @id_rio FROM cidades WHERE identificador = 'RIO';
SELECT id_cidade INTO @id_sao FROM cidades WHERE identificador = 'SAO';
SELECT id_cidade INTO @id_mia FROM cidades WHERE identificador = 'MIA';
SELECT id_cidade INTO @id_lis FROM cidades WHERE identificador = 'LIS';

-- =============================================================================
-- ===== SEÇÃO 2: AEROPORTOS (4 novos) =====
-- =============================================================================
-- codigo_iata é UNIQUE — INSERT IGNORE protege contra reexecução.

INSERT IGNORE INTO aeroportos (codigo_iata, nome, id_cidade) VALUES
  ('FLN', 'Aeroporto Internacional Hercílio Luz',    @id_flp),
  ('GYN', 'Aeroporto de Goiânia - Santa Genoveva',   @id_gyn),
  ('MAD', 'Aeropuerto Adolfo Suárez Madrid-Barajas', @id_mad),
  ('JFK', 'John F. Kennedy International Airport',    @id_nyc);

SELECT id_aeroporto INTO @id_aero_fln FROM aeroportos WHERE codigo_iata = 'FLN';
SELECT id_aeroporto INTO @id_aero_gyn FROM aeroportos WHERE codigo_iata = 'GYN';
SELECT id_aeroporto INTO @id_aero_mad FROM aeroportos WHERE codigo_iata = 'MAD';
SELECT id_aeroporto INTO @id_aero_jfk FROM aeroportos WHERE codigo_iata = 'JFK';

-- Aeroportos do seed
SELECT id_aeroporto INTO @id_aero_gig FROM aeroportos WHERE codigo_iata = 'GIG';
SELECT id_aeroporto INTO @id_aero_gru FROM aeroportos WHERE codigo_iata = 'GRU';
SELECT id_aeroporto INTO @id_aero_rec FROM aeroportos WHERE codigo_iata = 'REC';
SELECT id_aeroporto INTO @id_aero_mia FROM aeroportos WHERE codigo_iata = 'MIA';

-- =============================================================================
-- ===== SEÇÃO 3: TRANSPORTADORAS (1 aérea + 1 rodoviária) =====
-- =============================================================================
-- cnpj é UNIQUE — INSERT IGNORE protege.

INSERT IGNORE INTO transportadoras (cnpj, nome, telefone, email, ativo) VALUES
  ('11222333000181', 'VoeMais Linhas Aéreas',  '1130001111', 'contato@voemais.com.br',      1),
  ('44555666000172', 'RodoExpresso Viações',   '1140002222', 'contato@rodoexpresso.com.br', 1);

SELECT id_transportadora INTO @id_voemais   FROM transportadoras WHERE cnpj = '11222333000181';
SELECT id_transportadora INTO @id_rodoexp   FROM transportadoras WHERE cnpj = '44555666000172';

-- Transportadoras do seed
SELECT id_transportadora INTO @id_latam     FROM transportadoras WHERE cnpj = '02012862000160';
SELECT id_transportadora INTO @id_azul      FROM transportadoras WHERE cnpj = '09296295000160';
SELECT id_transportadora INTO @id_gol       FROM transportadoras WHERE cnpj = '07575651000159';
SELECT id_transportadora INTO @id_rff       FROM transportadoras WHERE cnpj = '33041260064690';
SELECT id_transportadora INTO @id_cruzeiros FROM transportadoras WHERE cnpj = '12345678000199';

-- =============================================================================
-- ===== SEÇÃO 4: MODAIS (6 novos) =====
-- =============================================================================
-- codigo é UNIQUE — INSERT IGNORE protege.

INSERT IGNORE INTO modais (codigo, tipo, modelo, ano, capacidade, status, id_transportadora, id_aeroporto_base, ativo) VALUES
  ('VOEMAIS-A321',  'AVIAO',  'Airbus A321',            2021, 220,  'DISPONIVEL', @id_voemais,   @id_aero_fln, 1),
  ('VOEMAIS-B738',  'AVIAO',  'Boeing 737-800',          2019, 186,  'DISPONIVEL', @id_voemais,   @id_aero_mad, 1),
  ('RODO-LEITO-01', 'ONIBUS', 'Marcopolo Paradiso G8',   2022,  46,  'DISPONIVEL', @id_rodoexp,   NULL,         1),
  ('RFF-TREM-002',  'TREM',   'Trem Regional TR-200',    2020, 300,  'DISPONIVEL', @id_rff,       NULL,         1),
  ('CRUZEIRO-002',  'NAVIO',  'MS Atlântico Sul',        2018, 1200, 'DISPONIVEL', @id_cruzeiros, NULL,         1),
  ('LATAM-A319-M',  'AVIAO',  'Airbus A319 (manutencao)',2017, 144,  'MANUTENCAO', @id_latam,     @id_aero_gru, 1);

SELECT id_modal INTO @id_m_a321   FROM modais WHERE codigo = 'VOEMAIS-A321';
SELECT id_modal INTO @id_m_b738   FROM modais WHERE codigo = 'VOEMAIS-B738';
SELECT id_modal INTO @id_m_rodo   FROM modais WHERE codigo = 'RODO-LEITO-01';
SELECT id_modal INTO @id_m_trem2  FROM modais WHERE codigo = 'RFF-TREM-002';
SELECT id_modal INTO @id_m_navio2 FROM modais WHERE codigo = 'CRUZEIRO-002';
SELECT id_modal INTO @id_m_a319m  FROM modais WHERE codigo = 'LATAM-A319-M';

-- Modais do seed
SELECT id_modal INTO @id_m_latam_a320 FROM modais WHERE codigo = 'LATAM-A320-001';
SELECT id_modal INTO @id_m_latam_b777 FROM modais WHERE codigo = 'LATAM-B777-001';
SELECT id_modal INTO @id_m_azul_e195  FROM modais WHERE codigo = 'AZUL-E195-001';
SELECT id_modal INTO @id_m_gol_b737   FROM modais WHERE codigo = 'GOL-B737-001';
SELECT id_modal INTO @id_m_cbus1      FROM modais WHERE codigo = 'CBUS-001';
SELECT id_modal INTO @id_m_trem1      FROM modais WHERE codigo = 'RFF-TREM-001';

-- =============================================================================
-- ===== SEÇÃO 5: MANUTENÇÕES (1 CONCLUIDA + 1 AGENDADA) =====
-- =============================================================================
-- Sem UNIQUE key natural — guarda por data_inicio para idempotência.

INSERT IGNORE INTO manutencoes (id_modal, data_inicio, data_fim, descricao, status)
  SELECT @id_m_a319m, '2026-05-10', '2026-05-25',
         'Revisao geral de motor e inspecao estrutural.', 'CONCLUIDA'
   WHERE NOT EXISTS (
     SELECT 1 FROM manutencoes WHERE id_modal=@id_m_a319m AND data_inicio='2026-05-10');

INSERT IGNORE INTO manutencoes (id_modal, data_inicio, data_fim, descricao, status)
  SELECT @id_m_a319m, '2026-08-01', '2026-08-15',
         'Manutencao programada de avionicos e trem de pouso.', 'AGENDADA'
   WHERE NOT EXISTS (
     SELECT 1 FROM manutencoes WHERE id_modal=@id_m_a319m AND data_inicio='2026-08-01');

-- =============================================================================
-- ===== SEÇÃO 6: ROTAS (6 novas) =====
-- =============================================================================
-- codigo é UNIQUE — INSERT IGNORE protege.

INSERT IGNORE INTO rotas (codigo, descricao, id_cidade_origem, id_cidade_destino, tipo, ativo) VALUES
  ('RT-RIO-FLN-01', 'Rio de Janeiro -> Florianopolis',           @id_rio, @id_flp, 'DIRETA',     1),
  ('RT-SAO-GYN-01', 'Sao Paulo -> Goiania',                      @id_sao, @id_gyn, 'DIRETA',     1),
  ('RT-RIO-NAT-01', 'Rio de Janeiro -> Natal',                   @id_rio, @id_nat, 'DIRETA',     1),
  ('RT-SAO-MAD-01', 'Sao Paulo -> Madrid',                       @id_sao, @id_mad, 'DIRETA',     1),
  ('RT-RIO-NYC-01', 'Rio de Janeiro -> Nova York (escala Miami)', @id_rio, @id_nyc, 'COM_ESCALA', 1),
  ('RT-SAO-VIX-01', 'Sao Paulo -> Vitoria',                      @id_sao, @id_vix, 'DIRETA',     1);

SELECT id_rota INTO @id_r_fln FROM rotas WHERE codigo = 'RT-RIO-FLN-01';
SELECT id_rota INTO @id_r_gyn FROM rotas WHERE codigo = 'RT-SAO-GYN-01';
SELECT id_rota INTO @id_r_nat FROM rotas WHERE codigo = 'RT-RIO-NAT-01';
SELECT id_rota INTO @id_r_mad FROM rotas WHERE codigo = 'RT-SAO-MAD-01';
SELECT id_rota INTO @id_r_nyc FROM rotas WHERE codigo = 'RT-RIO-NYC-01';
SELECT id_rota INTO @id_r_vix FROM rotas WHERE codigo = 'RT-SAO-VIX-01';

-- =============================================================================
-- ===== SEÇÃO 7: TRECHOS DE ROTA =====
-- =============================================================================
-- UNIQUE KEY uq_trechos_rota_ordem (id_rota, ordem) — INSERT IGNORE protege.

INSERT IGNORE INTO trechos_rota (id_rota, ordem, id_cidade_origem, id_cidade_destino, id_aeroporto_origem, id_aeroporto_destino, hora_partida, hora_chegada, tempo_estimado_min) VALUES
  -- RIO->FLN direto (aéreo)
  (@id_r_fln, 1, @id_rio, @id_flp, @id_aero_gig, @id_aero_fln, '08:00:00', '09:30:00',  90),
  -- SAO->GYN direto (aéreo)
  (@id_r_gyn, 1, @id_sao, @id_gyn, @id_aero_gru, @id_aero_gyn, '10:00:00', '11:45:00', 105),
  -- RIO->NAT direto (aéreo — sem aeroporto de chegada próprio para Natal)
  (@id_r_nat, 1, @id_rio, @id_nat, @id_aero_gig, NULL,          '07:30:00', '10:30:00', 180),
  -- SAO->MAD direto (aéreo internacional)
  (@id_r_mad, 1, @id_sao, @id_mad, @id_aero_gru, @id_aero_mad, '22:00:00', '14:00:00', 600),
  -- RIO->NYC com escala Miami — trecho 1: RIO->MIA
  (@id_r_nyc, 1, @id_rio, @id_mia, @id_aero_gig, @id_aero_mia, '21:00:00', '05:00:00', 480),
  -- RIO->NYC com escala Miami — trecho 2: MIA->NYC
  (@id_r_nyc, 2, @id_mia, @id_nyc, @id_aero_mia, @id_aero_jfk, '07:00:00', '10:00:00', 180),
  -- SAO->VIX direto (ônibus — sem aeroporto)
  (@id_r_vix, 1, @id_sao, @id_vix, NULL,          NULL,          '08:00:00', '14:00:00', 360);

-- =============================================================================
-- ===== SEÇÃO 8: PROGRAMAÇÕES DE VIAGEM (16, datas >= 2026-07-01) =====
-- =============================================================================
-- Sem UNIQUE natural — guarda por (id_rota, id_modal, data_viagem).
-- NÃO usa o modal em MANUTENCAO (@id_m_a319m).

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_fln, @id_m_a321,      '2026-07-05',  220,  450.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_fln AND id_modal=@id_m_a321     AND data_viagem='2026-07-05');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_gyn, @id_m_azul_e195, '2026-07-12',  118,  520.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_gyn AND id_modal=@id_m_azul_e195 AND data_viagem='2026-07-12');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_nat, @id_m_b738,      '2026-07-20',  186,  680.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_nat AND id_modal=@id_m_b738      AND data_viagem='2026-07-20');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_mad, @id_m_latam_b777,'2026-08-03',  396, 3800.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_mad AND id_modal=@id_m_latam_b777 AND data_viagem='2026-08-03');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_nyc, @id_m_latam_a320,'2026-08-15',  186, 4200.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_nyc AND id_modal=@id_m_latam_a320 AND data_viagem='2026-08-15');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_vix, @id_m_rodo,      '2026-08-22',   46,  180.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_vix AND id_modal=@id_m_rodo      AND data_viagem='2026-08-22');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_fln, @id_m_rodo,      '2026-09-01',   46,  220.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_fln AND id_modal=@id_m_rodo      AND data_viagem='2026-09-01');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_gyn, @id_m_trem2,     '2026-09-10',  300,  150.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_gyn AND id_modal=@id_m_trem2     AND data_viagem='2026-09-10');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_nyc, @id_m_navio2,    '2026-09-25', 1200, 2500.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_nyc AND id_modal=@id_m_navio2    AND data_viagem='2026-09-25');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_nat, @id_m_a321,      '2026-10-05',  220,  720.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_nat AND id_modal=@id_m_a321      AND data_viagem='2026-10-05');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_mad, @id_m_b738,      '2026-10-18',  186, 3950.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_mad AND id_modal=@id_m_b738      AND data_viagem='2026-10-18');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_vix, @id_m_gol_b737,  '2026-11-02',  189,  210.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_vix AND id_modal=@id_m_gol_b737  AND data_viagem='2026-11-02');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_fln, @id_m_b738,      '2026-11-15',  186,  480.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_fln AND id_modal=@id_m_b738      AND data_viagem='2026-11-15');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_gyn, @id_m_a321,      '2026-12-01',  220,  540.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_gyn AND id_modal=@id_m_a321      AND data_viagem='2026-12-01');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_nat, @id_m_b738,      '2026-12-12',  186,  790.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_nat AND id_modal=@id_m_b738      AND data_viagem='2026-12-12');

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status)
  SELECT t.* FROM (SELECT @id_r_nyc, @id_m_latam_b777,'2026-12-22',  396, 4500.00, 'ATIVO') t
  WHERE NOT EXISTS (SELECT 1 FROM programacoes_viagem WHERE id_rota=@id_r_nyc AND id_modal=@id_m_latam_b777 AND data_viagem='2026-12-22');

-- Captura IDs das programações (funciona independente de INSERT ter ocorrido agora ou antes)
SELECT id_programacao INTO @id_prog_A FROM programacoes_viagem WHERE id_rota=@id_r_fln AND id_modal=@id_m_a321      AND data_viagem='2026-07-05';
SELECT id_programacao INTO @id_prog_B FROM programacoes_viagem WHERE id_rota=@id_r_gyn AND id_modal=@id_m_azul_e195 AND data_viagem='2026-07-12';
SELECT id_programacao INTO @id_prog_C FROM programacoes_viagem WHERE id_rota=@id_r_nat AND id_modal=@id_m_b738      AND data_viagem='2026-07-20';
SELECT id_programacao INTO @id_prog_D FROM programacoes_viagem WHERE id_rota=@id_r_mad AND id_modal=@id_m_latam_b777 AND data_viagem='2026-08-03';
SELECT id_programacao INTO @id_prog_E FROM programacoes_viagem WHERE id_rota=@id_r_nyc AND id_modal=@id_m_latam_a320 AND data_viagem='2026-08-15';
SELECT id_programacao INTO @id_prog_F FROM programacoes_viagem WHERE id_rota=@id_r_vix AND id_modal=@id_m_rodo      AND data_viagem='2026-08-22';
SELECT id_programacao INTO @id_prog_G FROM programacoes_viagem WHERE id_rota=@id_r_fln AND id_modal=@id_m_rodo      AND data_viagem='2026-09-01';
SELECT id_programacao INTO @id_prog_H FROM programacoes_viagem WHERE id_rota=@id_r_gyn AND id_modal=@id_m_trem2     AND data_viagem='2026-09-10';
SELECT id_programacao INTO @id_prog_I FROM programacoes_viagem WHERE id_rota=@id_r_nyc AND id_modal=@id_m_navio2    AND data_viagem='2026-09-25';
SELECT id_programacao INTO @id_prog_J FROM programacoes_viagem WHERE id_rota=@id_r_nat AND id_modal=@id_m_a321      AND data_viagem='2026-10-05';
SELECT id_programacao INTO @id_prog_K FROM programacoes_viagem WHERE id_rota=@id_r_mad AND id_modal=@id_m_b738      AND data_viagem='2026-10-18';
SELECT id_programacao INTO @id_prog_L FROM programacoes_viagem WHERE id_rota=@id_r_vix AND id_modal=@id_m_gol_b737  AND data_viagem='2026-11-02';
SELECT id_programacao INTO @id_prog_M FROM programacoes_viagem WHERE id_rota=@id_r_fln AND id_modal=@id_m_b738      AND data_viagem='2026-11-15';
SELECT id_programacao INTO @id_prog_N FROM programacoes_viagem WHERE id_rota=@id_r_gyn AND id_modal=@id_m_a321      AND data_viagem='2026-12-01';
SELECT id_programacao INTO @id_prog_O FROM programacoes_viagem WHERE id_rota=@id_r_nat AND id_modal=@id_m_b738      AND data_viagem='2026-12-12';
SELECT id_programacao INTO @id_prog_P FROM programacoes_viagem WHERE id_rota=@id_r_nyc AND id_modal=@id_m_latam_b777 AND data_viagem='2026-12-22';

-- =============================================================================
-- ===== SEÇÃO 9: USUÁRIOS (8 clientes + 3 funcionários) =====
-- =============================================================================
-- email é UNIQUE — INSERT IGNORE protege.

INSERT IGNORE INTO usuarios (email, senha_hash, ativo, tentativas_falhas) VALUES
  ('ana.silva@email.com',      '$2a$12$PLACEHOLDER_HASH_CLIENTE_CHANGE_IN_PROD', 1, 0),
  ('bruno.costa@email.com',    '$2a$12$PLACEHOLDER_HASH_CLIENTE_CHANGE_IN_PROD', 1, 0),
  ('carla.souza@email.com',    '$2a$12$PLACEHOLDER_HASH_CLIENTE_CHANGE_IN_PROD', 1, 0),
  ('diego.lima@email.com',     '$2a$12$PLACEHOLDER_HASH_CLIENTE_CHANGE_IN_PROD', 1, 0),
  ('elena.rocha@email.com',    '$2a$12$PLACEHOLDER_HASH_CLIENTE_CHANGE_IN_PROD', 1, 0),
  ('felipe.alves@email.com',   '$2a$12$PLACEHOLDER_HASH_CLIENTE_CHANGE_IN_PROD', 1, 0),
  ('gabriela.dias@email.com',  '$2a$12$PLACEHOLDER_HASH_CLIENTE_CHANGE_IN_PROD', 1, 0),
  ('henrique.melo@email.com',  '$2a$12$PLACEHOLDER_HASH_CLIENTE_CHANGE_IN_PROD', 1, 0),
  ('func.pedro@vvv.com',       '$2a$12$PLACEHOLDER_HASH_CLIENTE_CHANGE_IN_PROD', 1, 0),
  ('gerente.pdv2@vvv.com',     '$2a$12$PLACEHOLDER_HASH_CLIENTE_CHANGE_IN_PROD', 1, 0),
  ('gerente.virtual2@vvv.com', '$2a$12$PLACEHOLDER_HASH_CLIENTE_CHANGE_IN_PROD', 1, 0);

SELECT id_usuario INTO @u_ana    FROM usuarios WHERE email = 'ana.silva@email.com';
SELECT id_usuario INTO @u_bruno  FROM usuarios WHERE email = 'bruno.costa@email.com';
SELECT id_usuario INTO @u_carla  FROM usuarios WHERE email = 'carla.souza@email.com';
SELECT id_usuario INTO @u_diego  FROM usuarios WHERE email = 'diego.lima@email.com';
SELECT id_usuario INTO @u_elena  FROM usuarios WHERE email = 'elena.rocha@email.com';
SELECT id_usuario INTO @u_felipe FROM usuarios WHERE email = 'felipe.alves@email.com';
SELECT id_usuario INTO @u_gabi   FROM usuarios WHERE email = 'gabriela.dias@email.com';
SELECT id_usuario INTO @u_henri  FROM usuarios WHERE email = 'henrique.melo@email.com';
SELECT id_usuario INTO @u_pedro  FROM usuarios WHERE email = 'func.pedro@vvv.com';
SELECT id_usuario INTO @u_mpdv2  FROM usuarios WHERE email = 'gerente.pdv2@vvv.com';
SELECT id_usuario INTO @u_gvirt2 FROM usuarios WHERE email = 'gerente.virtual2@vvv.com';

-- Perfis (por nome, não por id hardcoded)
SELECT id_perfil INTO @perfil_cliente FROM perfis WHERE nome = 'CLIENTE';
SELECT id_perfil INTO @perfil_func    FROM perfis WHERE nome = 'FUNCIONARIO';
SELECT id_perfil INTO @perfil_gpdv    FROM perfis WHERE nome = 'GERENTE_PDV';
SELECT id_perfil INTO @perfil_gvirt   FROM perfis WHERE nome = 'GERENTE_VIRTUAL';

-- PK de usuarios_perfis é (id_usuario, id_perfil) — INSERT IGNORE protege.
INSERT IGNORE INTO usuarios_perfis (id_usuario, id_perfil) VALUES
  (@u_ana,    @perfil_cliente),
  (@u_bruno,  @perfil_cliente),
  (@u_carla,  @perfil_cliente),
  (@u_diego,  @perfil_cliente),
  (@u_elena,  @perfil_cliente),
  (@u_felipe, @perfil_cliente),
  (@u_gabi,   @perfil_cliente),
  (@u_henri,  @perfil_cliente),
  (@u_pedro,  @perfil_func),
  (@u_mpdv2,  @perfil_gpdv),
  (@u_gvirt2, @perfil_gvirt);

-- =============================================================================
-- ===== SEÇÃO 10: CLIENTES (8) =====
-- =============================================================================
-- cpf é UNIQUE — INSERT IGNORE protege.

INSERT IGNORE INTO clientes (codigo, nome, cpf, email, telefone, ativo, id_usuario) VALUES
  ('CLI-002', 'Ana Silva',     '11144477735', 'ana.silva@email.com',     '21988880001', 1, @u_ana),
  ('CLI-003', 'Bruno Costa',   '22255588846', 'bruno.costa@email.com',   '11988880002', 1, @u_bruno),
  ('CLI-004', 'Carla Souza',   '33366699957', 'carla.souza@email.com',   '21988880003', 1, @u_carla),
  ('CLI-005', 'Diego Lima',    '44477700068', 'diego.lima@email.com',    '11988880004', 1, @u_diego),
  ('CLI-006', 'Elena Rocha',   '55588811179', 'elena.rocha@email.com',   '85988880005', 1, @u_elena),
  ('CLI-007', 'Felipe Alves',  '66699922280', 'felipe.alves@email.com',  '51988880006', 1, @u_felipe),
  ('CLI-008', 'Gabriela Dias', '77700033391', 'gabriela.dias@email.com', '81988880007', 1, @u_gabi),
  ('CLI-009', 'Henrique Melo', '88811144402', 'henrique.melo@email.com', '41988880008', 1, @u_henri);

SELECT id_cliente INTO @c_ana    FROM clientes WHERE cpf = '11144477735';
SELECT id_cliente INTO @c_bruno  FROM clientes WHERE cpf = '22255588846';
SELECT id_cliente INTO @c_carla  FROM clientes WHERE cpf = '33366699957';
SELECT id_cliente INTO @c_diego  FROM clientes WHERE cpf = '44477700068';
SELECT id_cliente INTO @c_elena  FROM clientes WHERE cpf = '55588811179';
SELECT id_cliente INTO @c_felipe FROM clientes WHERE cpf = '66699922280';
SELECT id_cliente INTO @c_gabi   FROM clientes WHERE cpf = '77700033391';
SELECT id_cliente INTO @c_henri  FROM clientes WHERE cpf = '88811144402';

-- =============================================================================
-- ===== SEÇÃO 11: PASSAGEIROS (8 adultos + 1 menor) =====
-- =============================================================================
-- cpf é UNIQUE — INSERT IGNORE protege.
-- Menor Lucas Silva (2021-02-14, ~5 anos) — testa trigger de criança + acompanhante.

INSERT IGNORE INTO passageiros
  (codigo, cpf, nome, data_nascimento, telefone, profissao,
   rua, numero, complemento, bairro, cep,
   cidade_endereco, estado_endereco, ativo, id_usuario, id_cliente) VALUES
  ('PAS-002','11144477735','Ana Silva',     '1988-03-15','21988880001','Engenheira',
   'Rua das Flores',     '100', NULL,       'Centro',     '20000000','Rio de Janeiro','RJ',1,@u_ana,   @c_ana),
  ('PAS-003','22255588846','Bruno Costa',   '1979-07-22','11988880002','Advogado',
   'Av. Paulista',       '2000','Apto 50',  'Bela Vista', '01310000','Sao Paulo',     'SP',1,@u_bruno, @c_bruno),
  ('PAS-004','33366699957','Carla Souza',   '1992-11-30','21988880003','Medica',
   'Rua do Catete',      '300', NULL,       'Catete',     '22220000','Rio de Janeiro','RJ',1,@u_carla, @c_carla),
  ('PAS-005','44477700068','Diego Lima',    '1985-01-10','11988880004','Arquiteto',
   'Rua Augusta',        '500', 'Casa 2',   'Consolacao', '01305000','Sao Paulo',     'SP',1,@u_diego, @c_diego),
  ('PAS-006','55588811179','Elena Rocha',   '1995-06-05','85988880005','Professora',
   'Av. Beira Mar',      '750', NULL,       'Meireles',   '60165000','Fortaleza',     'CE',1,@u_elena, @c_elena),
  ('PAS-007','66699922280','Felipe Alves',  '1972-09-18','51988880006','Contador',
   'Rua dos Andradas',   '120', 'Sala 3',   'Centro',     '90020000','Porto Alegre',  'RS',1,@u_felipe,@c_felipe),
  ('PAS-008','77700033391','Gabriela Dias', '1999-12-25','81988880007','Designer',
   'Av. Boa Viagem',     '900', NULL,       'Boa Viagem', '51020000','Recife',        'PE',1,@u_gabi,  @c_gabi),
  ('PAS-009','88811144402','Henrique Melo', '1983-04-08','41988880008','Empresario',
   'Rua XV de Novembro', '600', 'Cobertura','Centro',     '80020000','Curitiba',      'PR',1,@u_henri, @c_henri),
  ('PAS-010','99900055513','Lucas Silva',   '2021-02-14','21988880001','Estudante',
   'Rua das Flores',     '100', NULL,       'Centro',     '20000000','Rio de Janeiro','RJ',1,@u_ana,   @c_ana);

SELECT id_passageiro INTO @p_ana    FROM passageiros WHERE cpf = '11144477735';
SELECT id_passageiro INTO @p_bruno  FROM passageiros WHERE cpf = '22255588846';
SELECT id_passageiro INTO @p_carla  FROM passageiros WHERE cpf = '33366699957';
SELECT id_passageiro INTO @p_diego  FROM passageiros WHERE cpf = '44477700068';
SELECT id_passageiro INTO @p_elena  FROM passageiros WHERE cpf = '55588811179';
SELECT id_passageiro INTO @p_felipe FROM passageiros WHERE cpf = '66699922280';
SELECT id_passageiro INTO @p_gabi   FROM passageiros WHERE cpf = '77700033391';
SELECT id_passageiro INTO @p_henri  FROM passageiros WHERE cpf = '88811144402';
SELECT id_passageiro INTO @p_lucas  FROM passageiros WHERE cpf = '99900055513';

-- =============================================================================
-- ===== SEÇÃO 12: FUNCIONÁRIOS (3 novos) =====
-- =============================================================================
-- cpf é UNIQUE — INSERT IGNORE protege.

INSERT IGNORE INTO funcionarios
  (codigo, cpf, nome, rua, numero, complemento, bairro, cep,
   cidade_endereco, estado_endereco, tipo, ativo, id_usuario) VALUES
  ('FUNC003', '10120230340', 'Pedro Santos',     'Rua A', '10', NULL,     'Centro', '20010000', 'Rio de Janeiro', 'RJ', 'FUNCIONARIO',    1, @u_pedro),
  ('GPDV002', '40450460470', 'Mariana Oliveira', 'Rua B', '20', 'Sala 1', 'Centro', '01020000', 'Sao Paulo',      'SP', 'GERENTE_PDV',    1, @u_mpdv2),
  ('GVIRT002','70780790800', 'Rafael Nunes',     'Rua C', '30', NULL,     'Centro', '70020000', 'Brasilia',       'DF', 'GERENTE_VIRTUAL',1, @u_gvirt2);

SELECT id_funcionario INTO @f_pedro   FROM funcionarios WHERE cpf = '10120230340';
SELECT id_funcionario INTO @f_mariana FROM funcionarios WHERE cpf = '40450460470';
SELECT id_funcionario INTO @f_rafael  FROM funcionarios WHERE cpf = '70780790800';

-- Funcionários do seed
SELECT id_funcionario INTO @f_carlos  FROM funcionarios WHERE codigo = 'GVIRT001';
SELECT id_funcionario INTO @f_ana_pdv FROM funcionarios WHERE codigo = 'GPDV001';
SELECT id_funcionario INTO @f_joao    FROM funcionarios WHERE codigo = 'FUNC001';
SELECT id_funcionario INTO @f_maria   FROM funcionarios WHERE codigo = 'FUNC002';

-- =============================================================================
-- ===== SEÇÃO 13: PONTOS DE VENDA (2 novos) =====
-- =============================================================================
-- cnpj é UNIQUE — INSERT IGNORE protege.

INSERT IGNORE INTO pontos_de_venda
  (codigo, cnpj, nome, rua, numero, complemento, bairro, cep,
   cidade_endereco, estado_endereco, telefone, ativo, id_gerente) VALUES
  ('PDV-CE-001', '12121212000101', 'VVV Fortaleza Centro', 'Av. Santos Dumont', '100', NULL,
   'Centro',      '60010000', 'Fortaleza', 'CE', '85933330001', 1, @f_ana_pdv),
  ('PDV-SP-002', '13131313000102', 'VVV Sao Paulo Sul',    'Av. Santo Amaro',   '200', NULL,
   'Santo Amaro', '04700000', 'Sao Paulo', 'SP', '11933330002', 1, @f_mariana);

SELECT id_ponto INTO @pdv_ce  FROM pontos_de_venda WHERE codigo = 'PDV-CE-001';
SELECT id_ponto INTO @pdv_sp2 FROM pontos_de_venda WHERE codigo = 'PDV-SP-002';
SELECT id_ponto INTO @pdv_rj  FROM pontos_de_venda WHERE codigo = 'PDV-RJ-001';
SELECT id_ponto INTO @pdv_sp1 FROM pontos_de_venda WHERE codigo = 'PDV-SP-001';

-- =============================================================================
-- ===== SEÇÃO 14: VÍNCULOS FUNCIONÁRIO x PONTO DE VENDA =====
-- =============================================================================
-- Seed criou: (João,PDV-RJ), (João,PDV-SP), (Maria,PDV-RJ) — João já tem 2 ativos.
-- Guarda por (id_funcionario, id_ponto) para idempotência.

INSERT INTO funcionarios_pontos_de_venda (id_funcionario, id_ponto, data_inicio, ativo)
  SELECT @f_pedro,   @pdv_ce,  '2026-06-01', 1
   WHERE NOT EXISTS (SELECT 1 FROM funcionarios_pontos_de_venda WHERE id_funcionario=@f_pedro   AND id_ponto=@pdv_ce  AND ativo=1);

INSERT INTO funcionarios_pontos_de_venda (id_funcionario, id_ponto, data_inicio, ativo)
  SELECT @f_pedro,   @pdv_sp2, '2026-06-01', 1
   WHERE NOT EXISTS (SELECT 1 FROM funcionarios_pontos_de_venda WHERE id_funcionario=@f_pedro   AND id_ponto=@pdv_sp2 AND ativo=1);

INSERT INTO funcionarios_pontos_de_venda (id_funcionario, id_ponto, data_inicio, ativo)
  SELECT @f_mariana, @pdv_sp2, '2026-06-01', 1
   WHERE NOT EXISTS (SELECT 1 FROM funcionarios_pontos_de_venda WHERE id_funcionario=@f_mariana AND id_ponto=@pdv_sp2 AND ativo=1);

INSERT INTO funcionarios_pontos_de_venda (id_funcionario, id_ponto, data_inicio, ativo)
  SELECT @f_maria,   @pdv_ce,  '2026-06-01', 1
   WHERE NOT EXISTS (SELECT 1 FROM funcionarios_pontos_de_venda WHERE id_funcionario=@f_maria   AND id_ponto=@pdv_ce  AND ativo=1);

-- =============================================================================
-- ===== SEÇÃO 15: RESERVAS CONFIRMADAS — CANAL ONLINE (10) =====
-- =============================================================================
-- Fluxo: INSERT reserva -> INSERT pagamento PENDENTE -> UPDATE pagamento APROVADO.
-- Reservas/pagamentos sempre geram novos registros (sem guarda de idempotência
-- aqui — re-executar este script gerará reservas duplicadas, que é o esperado
-- num seed de demonstração executado uma única vez após limpeza).
-- codigo = NULL (trigger gera). Tickets criados automaticamente pela trigger.

-- ---- Reserva ONLINE 1 — Ana Silva, RIO->FLN (aviao), CREDITO 1x ----
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 450.00, 0.00, 450.00, @p_ana, @id_prog_A, NULL, @c_ana);
SET @res1 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res1, 'CREDITO', 1, 450.00, 0.00, 450.00, 450.00, 'PENDENTE', NULL);
SET @pag1 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000001', data_pagamento=NOW() WHERE id_pagamento=@pag1;

-- ---- Reserva ONLINE 2 — Bruno Costa, SAO->GYN, DEBITO ----
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 520.00, 0.00, 520.00, @p_bruno, @id_prog_B, NULL, @c_bruno);
SET @res2 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res2, 'DEBITO', 1, 520.00, 0.00, 520.00, 520.00, 'PENDENTE', NULL);
SET @pag2 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000002', data_pagamento=NOW() WHERE id_pagamento=@pag2;

-- ---- Reserva ONLINE 3 — Carla Souza, RIO->NAT, CREDITO 3x (sem juros <= 4x) ----
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 680.00, 0.00, 680.00, @p_carla, @id_prog_C, NULL, @c_carla);
SET @res3 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res3, 'CREDITO', 3, 680.00, 0.00, 680.00, 226.67, 'PENDENTE', NULL);
SET @pag3 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000003', data_pagamento=NOW() WHERE id_pagamento=@pag3;

-- ---- Reserva ONLINE 4 — Diego Lima, SAO->MAD, CREDITO 6x (5% juros > 4x) ----
-- 3800.00 x 1.05 = 3990.00; parcela = 665.00
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 3800.00, 0.00, 3800.00, @p_diego, @id_prog_D, NULL, @c_diego);
SET @res4 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res4, 'CREDITO', 6, 3800.00, 190.00, 3990.00, 665.00, 'PENDENTE', NULL);
SET @pag4 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000004', data_pagamento=NOW() WHERE id_pagamento=@pag4;

-- ---- Reserva ONLINE 5 — Elena Rocha, RIO->NYC, CREDITO 5x (5% juros) ----
-- 4200.00 x 1.05 = 4410.00; parcela = 882.00
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 4200.00, 0.00, 4200.00, @p_elena, @id_prog_E, NULL, @c_elena);
SET @res5 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res5, 'CREDITO', 5, 4200.00, 210.00, 4410.00, 882.00, 'PENDENTE', NULL);
SET @pag5 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000005', data_pagamento=NOW() WHERE id_pagamento=@pag5;

-- ---- Reserva ONLINE 6 — Felipe Alves, SAO->VIX (onibus), DEBITO ----
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 180.00, 0.00, 180.00, @p_felipe, @id_prog_F, NULL, @c_felipe);
SET @res6 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res6, 'DEBITO', 1, 180.00, 0.00, 180.00, 180.00, 'PENDENTE', NULL);
SET @pag6 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000006', data_pagamento=NOW() WHERE id_pagamento=@pag6;

-- ---- Reserva ONLINE 7 — Gabriela Dias, RIO->FLN (onibus), CREDITO 2x ----
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 220.00, 0.00, 220.00, @p_gabi, @id_prog_G, NULL, @c_gabi);
SET @res7 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res7, 'CREDITO', 2, 220.00, 0.00, 220.00, 110.00, 'PENDENTE', NULL);
SET @pag7 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000007', data_pagamento=NOW() WHERE id_pagamento=@pag7;

-- ---- Reserva ONLINE 8 — Henrique Melo, SAO->GYN (trem), CREDITO 4x (sem juros) ----
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 150.00, 0.00, 150.00, @p_henri, @id_prog_H, NULL, @c_henri);
SET @res8 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res8, 'CREDITO', 4, 150.00, 0.00, 150.00, 37.50, 'PENDENTE', NULL);
SET @pag8 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000008', data_pagamento=NOW() WHERE id_pagamento=@pag8;

-- ---- Reserva ONLINE 9 — Ana Silva, RIO->NYC (navio), DEBITO ----
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 2500.00, 0.00, 2500.00, @p_ana, @id_prog_I, NULL, @c_ana);
SET @res9 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res9, 'DEBITO', 1, 2500.00, 0.00, 2500.00, 2500.00, 'PENDENTE', NULL);
SET @pag9 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000009', data_pagamento=NOW() WHERE id_pagamento=@pag9;

-- ---- Reserva ONLINE 10 — MENOR Lucas Silva + acompanhante Ana Silva ----
-- Passageiro entre 2-10 anos: trigger aplica 40% desconto.
-- valor_bruto=2500.00 -> trigger seta desconto=1000.00 e total=1500.00.
-- Pagamento usa o valor pos-desconto: 1500.00.
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 2500.00, 0.00, 2500.00, @p_lucas, @id_prog_I, @p_ana, @c_ana);
SET @res10 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res10, 'CREDITO', 3, 1500.00, 0.00, 1500.00, 500.00, 'PENDENTE', NULL);
SET @pag10 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000010', data_pagamento=NOW() WHERE id_pagamento=@pag10;

-- =============================================================================
-- ===== SEÇÃO 16: RESERVAS CONFIRMADAS — CANAL PRESENCIAL (5) =====
-- =============================================================================

-- ---- Reserva PRESENCIAL 1 — Bruno Costa, RIO->NAT, DEBITO ----
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'PRESENCIAL', 720.00, 0.00, 720.00, @p_bruno, @id_prog_J, NULL, @c_bruno);
SET @res11 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res11, 'DEBITO', 1, 720.00, 0.00, 720.00, 720.00, 'PENDENTE', NULL);
SET @pag11 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000011', data_pagamento=NOW() WHERE id_pagamento=@pag11;

-- ---- Reserva PRESENCIAL 2 — Carla Souza, SAO->MAD, CREDITO 6x (5% juros) ----
-- 3950.00 x 1.05 = 4147.50; parcela = 691.25
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'PRESENCIAL', 3950.00, 0.00, 3950.00, @p_carla, @id_prog_K, NULL, @c_carla);
SET @res12 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res12, 'CREDITO', 6, 3950.00, 197.50, 4147.50, 691.25, 'PENDENTE', NULL);
SET @pag12 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000012', data_pagamento=NOW() WHERE id_pagamento=@pag12;

-- ---- Reserva PRESENCIAL 3 — Diego Lima, SAO->VIX (aviao), DEBITO ----
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'PRESENCIAL', 210.00, 0.00, 210.00, @p_diego, @id_prog_L, NULL, @c_diego);
SET @res13 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res13, 'DEBITO', 1, 210.00, 0.00, 210.00, 210.00, 'PENDENTE', NULL);
SET @pag13 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000013', data_pagamento=NOW() WHERE id_pagamento=@pag13;

-- ---- Reserva PRESENCIAL 4 — Elena Rocha, RIO->FLN, CREDITO 2x ----
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'PRESENCIAL', 480.00, 0.00, 480.00, @p_elena, @id_prog_M, NULL, @c_elena);
SET @res14 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res14, 'CREDITO', 2, 480.00, 0.00, 480.00, 240.00, 'PENDENTE', NULL);
SET @pag14 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000014', data_pagamento=NOW() WHERE id_pagamento=@pag14;

-- ---- Reserva PRESENCIAL 5 — Felipe Alves, SAO->GYN, DEBITO ----
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'PRESENCIAL', 540.00, 0.00, 540.00, @p_felipe, @id_prog_N, NULL, @c_felipe);
SET @res15 = LAST_INSERT_ID();
INSERT INTO pagamentos (id_reserva, tipo, parcelas, valor_bruto, valor_juros, valor_total, valor_parcela, status, data_pagamento)
  VALUES (@res15, 'DEBITO', 1, 540.00, 0.00, 540.00, 540.00, 'PENDENTE', NULL);
SET @pag15 = LAST_INSERT_ID();
UPDATE pagamentos SET status='APROVADO', codigo_autorizacao='AUTH00000015', data_pagamento=NOW() WHERE id_pagamento=@pag15;

-- =============================================================================
-- ===== SEÇÃO 17: RESERVAS PENDENTES (3, sem pagamento) =====
-- =============================================================================
INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 720.00, 0.00, 720.00, @p_gabi, @id_prog_J, NULL, @c_gabi);
SET @res16 = LAST_INSERT_ID();

INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'PRESENCIAL', 480.00, 0.00, 480.00, @p_henri, @id_prog_M, NULL, @c_henri);
SET @res17 = LAST_INSERT_ID();

INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 790.00, 0.00, 790.00, @p_ana, @id_prog_O, NULL, @c_ana);
SET @res18 = LAST_INSERT_ID();

-- =============================================================================
-- ===== SEÇÃO 18: RESERVAS CANCELADAS (2) =====
-- =============================================================================
-- Trigger devolve vaga ao cancelar.

INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'ONLINE', 450.00, 0.00, 450.00, @p_bruno, @id_prog_A, NULL, @c_bruno);
SET @res19 = LAST_INSERT_ID();
UPDATE reservas SET status='CANCELADA' WHERE id_reserva=@res19;

INSERT INTO reservas (codigo, data_criacao, status, canal, valor_bruto, valor_desconto, valor_total,
                      id_passageiro, id_programacao, id_acompanhante, id_cliente)
  VALUES (NULL, NOW(), 'PENDENTE', 'PRESENCIAL', 520.00, 0.00, 520.00, @p_carla, @id_prog_B, NULL, @c_carla);
SET @res20 = LAST_INSERT_ID();
UPDATE reservas SET status='CANCELADA' WHERE id_reserva=@res20;

-- =============================================================================
-- ===== SEÇÃO 19: VENDAS =====
-- =============================================================================
INSERT INTO vendas (id_reserva, data_venda, valor_total, status) VALUES
  (@res1,  NOW(),  450.00, 'CONFIRMADA'),
  (@res2,  NOW(),  520.00, 'CONFIRMADA'),
  (@res3,  NOW(),  680.00, 'CONFIRMADA'),
  (@res4,  NOW(), 3990.00, 'CONFIRMADA'),
  (@res5,  NOW(), 4410.00, 'CONFIRMADA'),
  (@res6,  NOW(),  180.00, 'CONFIRMADA'),
  (@res7,  NOW(),  220.00, 'CONFIRMADA'),
  (@res8,  NOW(),  150.00, 'CONFIRMADA'),
  (@res9,  NOW(), 2500.00, 'CONFIRMADA'),
  (@res10, NOW(), 1500.00, 'CONFIRMADA'),
  (@res11, NOW(),  720.00, 'CONFIRMADA'),
  (@res12, NOW(), 4147.50, 'CONFIRMADA'),
  (@res13, NOW(),  210.00, 'CONFIRMADA'),
  (@res14, NOW(),  480.00, 'CONFIRMADA'),
  (@res15, NOW(),  540.00, 'CONFIRMADA');

SELECT id_venda INTO @v1  FROM vendas WHERE id_reserva=@res1;
SELECT id_venda INTO @v2  FROM vendas WHERE id_reserva=@res2;
SELECT id_venda INTO @v3  FROM vendas WHERE id_reserva=@res3;
SELECT id_venda INTO @v4  FROM vendas WHERE id_reserva=@res4;
SELECT id_venda INTO @v5  FROM vendas WHERE id_reserva=@res5;
SELECT id_venda INTO @v6  FROM vendas WHERE id_reserva=@res6;
SELECT id_venda INTO @v7  FROM vendas WHERE id_reserva=@res7;
SELECT id_venda INTO @v8  FROM vendas WHERE id_reserva=@res8;
SELECT id_venda INTO @v9  FROM vendas WHERE id_reserva=@res9;
SELECT id_venda INTO @v10 FROM vendas WHERE id_reserva=@res10;
SELECT id_venda INTO @v11 FROM vendas WHERE id_reserva=@res11;
SELECT id_venda INTO @v12 FROM vendas WHERE id_reserva=@res12;
SELECT id_venda INTO @v13 FROM vendas WHERE id_reserva=@res13;
SELECT id_venda INTO @v14 FROM vendas WHERE id_reserva=@res14;
SELECT id_venda INTO @v15 FROM vendas WHERE id_reserva=@res15;

-- ---- Vendas ONLINE aprovadas pelo gerente virtual ----
INSERT INTO vendas_online (id_venda, id_gerente_virtual, data_aprovacao, status_aprovacao) VALUES
  (@v1,  @f_carlos, NOW(), 'APROVADA'),
  (@v2,  @f_carlos, NOW(), 'APROVADA'),
  (@v3,  @f_rafael, NOW(), 'APROVADA'),
  (@v4,  @f_rafael, NOW(), 'APROVADA'),
  (@v5,  @f_carlos, NOW(), 'APROVADA'),
  (@v6,  @f_carlos, NOW(), 'APROVADA'),
  (@v7,  @f_rafael, NOW(), 'APROVADA'),
  (@v8,  @f_rafael, NOW(), 'APROVADA'),
  (@v9,  @f_carlos, NOW(), 'APROVADA'),
  (@v10, @f_carlos, NOW(), 'APROVADA');

-- ---- Vendas PRESENCIAIS com funcionario e ponto ----
INSERT INTO vendas_presenciais (id_venda, id_funcionario, id_ponto, confirmado_por, data_confirmacao) VALUES
  (@v11, @f_joao,    @pdv_rj,  @f_ana_pdv, NOW()),
  (@v12, @f_maria,   @pdv_sp1, @f_ana_pdv, NOW()),
  (@v13, @f_pedro,   @pdv_ce,  @f_ana_pdv, NOW()),
  (@v14, @f_mariana, @pdv_sp2, @f_mariana, NOW()),
  (@v15, @f_pedro,   @pdv_sp2, @f_mariana, NOW());

-- =============================================================================
-- ===== SEÇÃO 20: LOG DE AUDITORIA (exemplos) =====
-- =============================================================================
INSERT INTO log_auditoria (tabela, id_registro, operacao, dados_anteriores, dados_novos, id_usuario, data_hora, ip_address) VALUES
  ('reservas',          @res1,       'INSERT', NULL,
   JSON_OBJECT('status','PENDENTE','canal','ONLINE','valor_total',450.00),
   @u_ana,   NOW(), '192.168.0.10'),
  ('pagamentos',        @pag1,       'UPDATE',
   JSON_OBJECT('status','PENDENTE'),
   JSON_OBJECT('status','APROVADO','codigo_autorizacao','AUTH00000001'),
   @u_ana,   NOW(), '192.168.0.10'),
  ('reservas',          @res1,       'UPDATE',
   JSON_OBJECT('status','PENDENTE'),
   JSON_OBJECT('status','CONFIRMADA'),
   1,        NOW(), '10.0.0.1'),
  ('reservas',          @res19,      'UPDATE',
   JSON_OBJECT('status','PENDENTE'),
   JSON_OBJECT('status','CANCELADA'),
   @u_bruno, NOW(), '192.168.0.20'),
  ('modais',            @id_m_a319m, 'UPDATE',
   JSON_OBJECT('status','DISPONIVEL'),
   JSON_OBJECT('status','MANUTENCAO'),
   1,        NOW(), '10.0.0.1'),
  ('vendas_presenciais',@v13,        'INSERT', NULL,
   JSON_OBJECT('id_venda',@v13,'id_funcionario',@f_pedro,'id_ponto',@pdv_ce),
   @u_pedro, NOW(), '192.168.1.5');

-- =============================================================================
-- FIM DO SCRIPT
-- =============================================================================
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'populate_vvv.sql executado com sucesso!' AS status;
