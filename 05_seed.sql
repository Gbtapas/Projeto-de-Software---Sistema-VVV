-- ============================================================
--  SISTEMA VAI & VOLTA VIAGENS (VVV)
--  Arquivo 05 — Dados Iniciais (Seed)
--  Contém: perfis, cidades, aeroportos, transportadoras,
--          usuário admin, funcionários de exemplo e rotas base
-- ============================================================

USE vvv;

-- Garante que o cliente MySQL interpreta o arquivo como UTF-8 (evita corrupção de caracteres)
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- PERFIS
-- ============================================================
INSERT INTO perfis (nome, descricao) VALUES
    ('ADMIN',            'Administrador do sistema com acesso total'),
    ('FUNCIONARIO',      'Atendente de ponto de venda físico'),
    ('GERENTE_PDV',      'Gerente responsável por ponto de venda físico'),
    ('GERENTE_VIRTUAL',  'Gerente de negócios virtuais — supervisiona vendas online'),
    ('CLIENTE',          'Cliente final que realiza reservas online');


-- ============================================================
-- CIDADES
-- RN11: identificador obrigatório com 3 letras maiúsculas
-- ============================================================
INSERT INTO cidades (nome, estado, pais, identificador) VALUES
    -- Brasil
    ('Rio de Janeiro',  'Rio de Janeiro',   'Brasil',         'RIO'),
    ('São Paulo',       'São Paulo',         'Brasil',         'SAO'),
    ('Brasília',        'Distrito Federal',  'Brasil',         'BSB'),
    ('Salvador',        'Bahia',             'Brasil',         'SSA'),
    ('Fortaleza',       'Ceará',             'Brasil',         'FOR'),
    ('Manaus',          'Amazonas',          'Brasil',         'MAO'),
    ('Porto Alegre',    'Rio Grande do Sul', 'Brasil',         'POA'),
    ('Recife',          'Pernambuco',        'Brasil',         'REC'),
    ('Curitiba',        'Paraná',            'Brasil',         'CWB'),
    ('Belém',           'Pará',              'Brasil',         'BEL'),
    -- Internacional
    ('Buenos Aires',    'Buenos Aires',      'Argentina',      'EZE'),
    ('Lisboa',          'Lisboa',            'Portugal',       'LIS'),
    ('Miami',           'Florida',           'Estados Unidos', 'MIA'),
    ('Paris',           'Île-de-France',     'França',         'CDG'),
    ('Londres',         'Inglaterra',        'Reino Unido',    'LHR');


-- ============================================================
-- AEROPORTOS
-- RN12: código IATA obrigatório para viagens aéreas
-- ============================================================
INSERT INTO aeroportos (codigo_iata, nome, id_cidade) VALUES
    ('GIG', 'Aeroporto Internacional do Galeão',               1),   -- Rio
    ('SDU', 'Aeroporto Santos Dumont',                         1),   -- Rio
    ('GRU', 'Aeroporto Internacional de Guarulhos',            2),   -- São Paulo
    ('CGH', 'Aeroporto de Congonhas',                          2),   -- São Paulo
    ('BSB', 'Aeroporto Internacional de Brasília',             3),   -- Brasília
    ('SSA', 'Aeroporto Internacional Dep. Luís Eduardo Magalhães', 4), -- Salvador
    ('FOR', 'Aeroporto Internacional Pinto Martins',           5),   -- Fortaleza
    ('MAO', 'Aeroporto Internacional Eduardo Gomes',           6),   -- Manaus
    ('POA', 'Aeroporto Internacional Salgado Filho',           7),   -- Porto Alegre
    ('REC', 'Aeroporto Internacional do Recife',               8),   -- Recife
    ('CWB', 'Aeroporto Internacional Afonso Pena',             9),   -- Curitiba
    ('BEL', 'Aeroporto Internacional de Belém',               10),   -- Belém
    ('EZE', 'Aeroporto Internacional Ministro Pistarini',     11),   -- Buenos Aires
    ('LIS', 'Aeroporto Internacional Humberto Delgado',       12),   -- Lisboa
    ('MIA', 'Miami International Airport',                    13),   -- Miami
    ('CDG', 'Aeroporto Charles de Gaulle',                    14),   -- Paris
    ('LHR', 'Aeroporto de Heathrow',                          15);   -- Londres


-- ============================================================
-- TRANSPORTADORAS
-- ============================================================
INSERT INTO transportadoras (cnpj, nome, telefone, email) VALUES
    ('02012862000160', 'LATAM Airlines Brasil',     '08007278228', 'suporte@latam.com'),
    ('09296295000160', 'Azul Linhas Aéreas',         '08008800848', 'contato@azul.com.br'),
    ('07575651000159', 'Gol Linhas Aéreas',          '08009890009', 'falecomagol@gollinhasaereas.com.br'),
    ('33041260064690', 'Rede Ferroviária Federal',   '08007000800', 'ferroviaria@rff.gov.br'),
    ('60742928000175', 'Comfortbus Transporte',      '01133334444', 'contato@comfortbus.com.br'),
    ('12345678000199', 'Cruzeiros do Brasil S.A.',   '02199998888', 'cruzeiros@cruzeiros.com.br');


-- ============================================================
-- MODAIS DE EXEMPLO
-- ============================================================
INSERT INTO modais (codigo, tipo, modelo, ano, capacidade, status, id_transportadora, id_aeroporto_base) VALUES
    -- Aviões (id_aeroporto_base obrigatório — RN12)
    ('LATAM-A320-001', 'AVIAO', 'Airbus A320',    2019, 186, 'DISPONIVEL', 1, 1),   -- GIG
    ('LATAM-B777-001', 'AVIAO', 'Boeing 777-300', 2018, 396, 'DISPONIVEL', 1, 3),   -- GRU
    ('AZUL-E195-001',  'AVIAO', 'Embraer E195',   2021, 118, 'DISPONIVEL', 2, 3),   -- GRU
    ('GOL-B737-001',   'AVIAO', 'Boeing 737 MAX', 2022, 189, 'DISPONIVEL', 3, 1),   -- GIG
    -- Ônibus (sem aeroporto)
    ('CBUS-001',       'ONIBUS', 'Mercedes Benz O-400',   2020, 46, 'DISPONIVEL', 5, NULL),
    ('CBUS-002',       'ONIBUS', 'Marcopolo Paradiso G7', 2021, 50, 'DISPONIVEL', 5, NULL),
    -- Trem
    ('RFF-TREM-001',   'TREM',  'Trem Regional IC',       2015, 300,'DISPONIVEL', 4, NULL),
    -- Navio / Cruzeiro
    ('CRUZEIRO-001',   'NAVIO', 'MSC Armonia',            2010, 1200,'DISPONIVEL',6, NULL);


-- ============================================================
-- USUÁRIO ADMINISTRADOR DO SISTEMA
-- Senha: Admin@2025 (hash bcrypt — substitua em produção!)
-- ============================================================
INSERT INTO usuarios (email, senha_hash) VALUES
    ('admin@vvv.com.br',         '$2a$12$PLACEHOLDER_HASH_ADMIN_CHANGE_IN_PROD'),
    ('gerente.virtual@vvv.com.br','$2a$12$PLACEHOLDER_HASH_GVIRTUAL_CHANGE_IN_PROD'),
    ('gerente.pdv1@vvv.com.br',  '$2a$12$PLACEHOLDER_HASH_GPDV1_CHANGE_IN_PROD'),
    ('func.joao@vvv.com.br',     '$2a$12$PLACEHOLDER_HASH_JOAO_CHANGE_IN_PROD'),
    ('func.maria@vvv.com.br',    '$2a$12$PLACEHOLDER_HASH_MARIA_CHANGE_IN_PROD');

-- Perfis dos usuários
INSERT INTO usuarios_perfis (id_usuario, id_perfil) VALUES
    (1, 1),  -- admin → ADMIN
    (2, 4),  -- gerente virtual → GERENTE_VIRTUAL
    (3, 3),  -- gerente pdv1 → GERENTE_PDV
    (4, 2),  -- func. João → FUNCIONARIO
    (5, 2);  -- func. Maria → FUNCIONARIO


-- ============================================================
-- FUNCIONÁRIOS DE EXEMPLO
-- ============================================================
INSERT INTO funcionarios (codigo, cpf, nome, tipo, id_usuario) VALUES
    ('ADMIN001',  '00000000000', 'Administrador VVV',    'FUNCIONARIO',      1),
    ('GVIRT001',  '11111111111', 'Carlos Negócios',      'GERENTE_VIRTUAL',  2),
    ('GPDV001',   '22222222222', 'Ana Gerente PDV',      'GERENTE_PDV',      3),
    ('FUNC001',   '33333333333', 'João Atendente',       'FUNCIONARIO',      4),
    ('FUNC002',   '44444444444', 'Maria Atendente',      'FUNCIONARIO',      5);


-- ============================================================
-- PONTOS DE VENDA
-- RN25: dados completos obrigatórios
-- RN26: deve ter gerente responsável (GERENTE_PDV)
-- ============================================================
INSERT INTO pontos_de_venda (codigo, cnpj, nome, rua, numero, bairro, cep, cidade_endereco, estado_endereco, telefone, id_gerente) VALUES
    ('PDV-RJ-001', '12345678000101', 'VVV Centro Rio',     'Av. Rio Branco',   '100', 'Centro',        '20040002', 'Rio de Janeiro', 'RJ', '02133334444', 3),
    ('PDV-SP-001', '98765432000188', 'VVV Paulista SP',    'Av. Paulista',     '900', 'Bela Vista',    '01310100', 'São Paulo',       'SP', '01155556666', 3);


-- Vínculos funcionários ↔ pontos de venda (RN28: máx 2)
INSERT INTO funcionarios_pontos_de_venda (id_funcionario, id_ponto, data_inicio) VALUES
    (4, 1, '2024-01-10'),   -- João no PDV-RJ-001
    (4, 2, '2024-03-01'),   -- João também no PDV-SP-001 (2 vínculos — limite máximo)
    (5, 1, '2024-02-01');   -- Maria no PDV-RJ-001


-- ============================================================
-- ROTAS PRÉ-CADASTRADAS
-- RN09: origem != destino | RN10: diretas e com escala
-- ============================================================
INSERT INTO rotas (codigo, descricao, id_cidade_origem, id_cidade_destino, tipo) VALUES
    -- Domésticas diretas
    ('RT-RIO-SAO-01', 'Rio de Janeiro → São Paulo',      1,  2, 'DIRETA'),
    ('RT-SAO-BSB-01', 'São Paulo → Brasília',             2,  3, 'DIRETA'),
    ('RT-RIO-SSA-01', 'Rio de Janeiro → Salvador',        1,  4, 'DIRETA'),
    ('RT-SAO-POA-01', 'São Paulo → Porto Alegre',         2,  7, 'DIRETA'),
    ('RT-RIO-MAO-01', 'Rio de Janeiro → Manaus (escala São Paulo)', 1, 6, 'COM_ESCALA'),
    -- Internacionais
    ('RT-SAO-MIA-01', 'São Paulo → Miami',                2, 13, 'DIRETA'),
    ('RT-RIO-LIS-01', 'Rio de Janeiro → Lisboa',          1, 12, 'DIRETA'),
    ('RT-SAO-CDG-01', 'São Paulo → Paris (escala Lisboa)',2, 14, 'COM_ESCALA');


-- ============================================================
-- TRECHOS DAS ROTAS
-- RN14: hora partida, chegada e tempo estimado obrigatórios
-- RN12: aeroporto obrigatório em trechos aéreos
-- ============================================================

-- RT-RIO-SAO-01: voo direto (1 trecho)
INSERT INTO trechos_rota (id_rota, ordem, id_cidade_origem, id_cidade_destino, id_aeroporto_origem, id_aeroporto_destino, hora_partida, hora_chegada, tempo_estimado_min) VALUES
    (1, 1, 1, 2, 1, 3, '07:00:00', '08:10:00', 70);

-- RT-SAO-BSB-01: voo direto (1 trecho)
INSERT INTO trechos_rota (id_rota, ordem, id_cidade_origem, id_cidade_destino, id_aeroporto_origem, id_aeroporto_destino, hora_partida, hora_chegada, tempo_estimado_min) VALUES
    (2, 1, 2, 3, 3, 5, '10:00:00', '11:30:00', 90);

-- RT-RIO-SSA-01: voo direto (1 trecho)
INSERT INTO trechos_rota (id_rota, ordem, id_cidade_origem, id_cidade_destino, id_aeroporto_origem, id_aeroporto_destino, hora_partida, hora_chegada, tempo_estimado_min) VALUES
    (3, 1, 1, 4, 1, 6, '09:00:00', '11:00:00', 120);

-- RT-SAO-POA-01: voo direto (1 trecho)
INSERT INTO trechos_rota (id_rota, ordem, id_cidade_origem, id_cidade_destino, id_aeroporto_origem, id_aeroporto_destino, hora_partida, hora_chegada, tempo_estimado_min) VALUES
    (4, 1, 2, 7, 3, 9, '06:00:00', '07:40:00', 100);

-- RT-RIO-MAO-01: COM ESCALA em São Paulo (2 trechos)
INSERT INTO trechos_rota (id_rota, ordem, id_cidade_origem, id_cidade_destino, id_aeroporto_origem, id_aeroporto_destino, hora_partida, hora_chegada, tempo_estimado_min) VALUES
    (5, 1, 1, 2, 1, 3, '06:00:00', '07:10:00',  70),   -- Rio → SP
    (5, 2, 2, 6, 3, 8, '09:00:00', '12:30:00', 210);   -- SP → Manaus (aeroporto MAO id=8, não FOR id=7)

-- RT-SAO-MIA-01: internacional direto
INSERT INTO trechos_rota (id_rota, ordem, id_cidade_origem, id_cidade_destino, id_aeroporto_origem, id_aeroporto_destino, hora_partida, hora_chegada, tempo_estimado_min) VALUES
    (6, 1, 2, 13, 3, 15, '23:00:00', '07:30:00', 570);

-- RT-RIO-LIS-01: internacional direto
INSERT INTO trechos_rota (id_rota, ordem, id_cidade_origem, id_cidade_destino, id_aeroporto_origem, id_aeroporto_destino, hora_partida, hora_chegada, tempo_estimado_min) VALUES
    (7, 1, 1, 12, 1, 14, '22:00:00', '11:00:00', 660);

-- RT-SAO-CDG-01: COM ESCALA em Lisboa (2 trechos)
INSERT INTO trechos_rota (id_rota, ordem, id_cidade_origem, id_cidade_destino, id_aeroporto_origem, id_aeroporto_destino, hora_partida, hora_chegada, tempo_estimado_min) VALUES
    (8, 1, 2,  12, 3,  14, '22:00:00', '12:00:00', 660),  -- SP → Lisboa
    (8, 2, 12, 14, 14, 16, '14:00:00', '17:20:00', 140);  -- Lisboa → Paris


-- ============================================================
-- PROGRAMAÇÕES DE VIAGEM (EXEMPLOS)
-- Primeiras datas disponíveis para demonstração
-- ============================================================
INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status) VALUES
    -- Rio → São Paulo (LATAM A320, diária)
    (1, 1, '2026-04-01', 186, 350.00,  'ATIVO'),
    (1, 1, '2026-04-02', 186, 350.00,  'ATIVO'),
    (1, 4, '2026-04-01', 189, 320.00,  'ATIVO'),
    -- São Paulo → Brasília
    (2, 2, '2026-04-01', 396, 420.00,  'ATIVO'),
    (2, 3, '2026-04-02', 118, 390.00,  'ATIVO'),
    -- Rio → Salvador
    (3, 4, '2026-04-01', 189, 480.00,  'ATIVO'),
    -- São Paulo → Porto Alegre
    (4, 3, '2026-04-01', 118, 310.00,  'ATIVO'),
    -- Rio → Manaus (escala SP)
    (5, 2, '2026-04-03', 396, 750.00,  'ATIVO'),
    -- São Paulo → Miami (internacional)
    (6, 2, '2026-04-05', 396, 3200.00, 'ATIVO'),
    -- Rio → Lisboa
    (7, 2, '2026-04-10', 396, 4100.00, 'ATIVO'),
    -- São Paulo → Paris (escala Lisboa)
    (8, 2, '2026-04-12', 396, 4800.00, 'ATIVO');


SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Confirma carga inicial
-- ============================================================
SELECT 'Seed concluído com sucesso!' AS status;
SELECT COUNT(*) AS total_perfis         FROM perfis;
SELECT COUNT(*) AS total_cidades        FROM cidades;
SELECT COUNT(*) AS total_aeroportos     FROM aeroportos;
SELECT COUNT(*) AS total_transportadoras FROM transportadoras;
SELECT COUNT(*) AS total_modais         FROM modais;
SELECT COUNT(*) AS total_rotas          FROM rotas;
SELECT COUNT(*) AS total_trechos        FROM trechos_rota;
SELECT COUNT(*) AS total_programacoes   FROM programacoes_viagem;
