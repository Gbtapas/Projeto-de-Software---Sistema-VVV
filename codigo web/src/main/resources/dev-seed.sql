-- ============================================================
--  Sistema Vai&Volta Viagens (VVV)
--  dev-seed.sql — APENAS DADOS para a demonstração do MVP web
--
--  Por quê: o 05_seed.sql cadastra programações em abril/2026 (passado), e a
--  view vw_programacoes_disponiveis só mostra viagens com data_viagem >= hoje.
--  Sem isto, a tela de busca (UC02) viria vazia na demo.
--
--  NÃO altera a estrutura do banco — só insere linhas. Rodar depois do 05_seed.sql:
--    mysql -u root vvv < src/main/resources/dev-seed.sql
--
--  Usa rotas (1..8) e modais (1..6) já existentes do seed oficial.
-- ============================================================

USE vvv;

INSERT INTO programacoes_viagem (id_rota, id_modal, data_viagem, vagas_disponiveis, valor_base, status) VALUES
    -- Rio → São Paulo
    (1, 1, DATE_ADD(CURDATE(), INTERVAL 7  DAY), 186, 350.00, 'ATIVO'),
    (1, 4, DATE_ADD(CURDATE(), INTERVAL 8  DAY), 189, 320.00, 'ATIVO'),
    -- São Paulo → Brasília
    (2, 2, DATE_ADD(CURDATE(), INTERVAL 10 DAY), 396, 420.00, 'ATIVO'),
    (2, 3, DATE_ADD(CURDATE(), INTERVAL 12 DAY), 118, 390.00, 'ATIVO'),
    -- Rio → Salvador
    (3, 4, DATE_ADD(CURDATE(), INTERVAL 9  DAY), 189, 480.00, 'ATIVO'),
    -- São Paulo → Porto Alegre
    (4, 3, DATE_ADD(CURDATE(), INTERVAL 11 DAY), 118, 310.00, 'ATIVO'),
    -- Rio → Manaus (com escala)
    (5, 2, DATE_ADD(CURDATE(), INTERVAL 14 DAY), 396, 750.00, 'ATIVO'),
    -- Internacionais
    (6, 2, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 396, 3200.00, 'ATIVO'),
    (7, 2, DATE_ADD(CURDATE(), INTERVAL 25 DAY), 396, 4100.00, 'ATIVO');
