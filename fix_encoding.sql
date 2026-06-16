-- ============================================================
--  SISTEMA VAI & VOLTA VIAGENS (VVV)
--  fix_encoding.sql — Correção de encoding dos dados existentes
--
--  COMO USAR:
--    mysql -u root -p vvv < fix_encoding.sql
--    OU pelo MySQL Workbench/DBeaver executando este arquivo.
--
--  POR QUÊ: O seed foi importado sem SET NAMES utf8mb4,
--  fazendo o cliente MySQL interpretar os bytes UTF-8 do arquivo
--  como Latin-1. Os UPDATE abaixo sobrescrevem os valores
--  corrompidos com o texto correto.
-- ============================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vvv;

-- ============================================================
-- CIDADES
-- ============================================================
UPDATE cidades SET nome = 'São Paulo',  estado = 'São Paulo'       WHERE identificador = 'SAO';
UPDATE cidades SET nome = 'Brasília',   estado = 'Distrito Federal' WHERE identificador = 'BSB';
UPDATE cidades SET nome = 'Fortaleza',  estado = 'Ceará'            WHERE identificador = 'FOR';
UPDATE cidades SET nome = 'Belém',      estado = 'Pará'             WHERE identificador = 'BEL';
UPDATE cidades SET nome = 'Curitiba',   estado = 'Paraná'           WHERE identificador = 'CWB';
UPDATE cidades SET nome = 'Paris',      estado = 'Île-de-France',
                           pais  = 'França'                         WHERE identificador = 'CDG';

-- ============================================================
-- AEROPORTOS
-- ============================================================
UPDATE aeroportos
   SET nome = 'Aeroporto Internacional Dep. Luís Eduardo Magalhães'
 WHERE codigo_iata = 'SSA';

-- ============================================================
-- TRANSPORTADORAS
-- ============================================================
UPDATE transportadoras SET nome = 'Azul Linhas Aéreas' WHERE cnpj = '09296295000160';
UPDATE transportadoras SET nome = 'Gol Linhas Aéreas'  WHERE cnpj = '07575651000159';

-- ============================================================
-- ROTAS
-- ============================================================
UPDATE rotas SET descricao = 'Rio de Janeiro → São Paulo'             WHERE codigo = 'RT-RIO-SAO-01';
UPDATE rotas SET descricao = 'São Paulo → Brasília'                   WHERE codigo = 'RT-SAO-BSB-01';
UPDATE rotas SET descricao = 'Rio de Janeiro → Salvador'              WHERE codigo = 'RT-RIO-SSA-01';
UPDATE rotas SET descricao = 'São Paulo → Porto Alegre'               WHERE codigo = 'RT-SAO-POA-01';
UPDATE rotas SET descricao = 'Rio de Janeiro → Manaus (escala São Paulo)' WHERE codigo = 'RT-RIO-MAO-01';
UPDATE rotas SET descricao = 'São Paulo → Miami'                      WHERE codigo = 'RT-SAO-MIA-01';
UPDATE rotas SET descricao = 'Rio de Janeiro → Lisboa'                WHERE codigo = 'RT-RIO-LIS-01';
UPDATE rotas SET descricao = 'São Paulo → Paris (escala Lisboa)'      WHERE codigo = 'RT-SAO-CDG-01';

-- ============================================================
-- PERFIS (descrições em português)
-- ============================================================
UPDATE perfis SET descricao = 'Administrador do sistema com acesso total'          WHERE nome = 'ADMIN';
UPDATE perfis SET descricao = 'Atendente de ponto de venda físico'                 WHERE nome = 'FUNCIONARIO';
UPDATE perfis SET descricao = 'Gerente responsável por ponto de venda físico'      WHERE nome = 'GERENTE_PDV';
UPDATE perfis SET descricao = 'Gerente de negócios virtuais — supervisiona vendas online' WHERE nome = 'GERENTE_VIRTUAL';
UPDATE perfis SET descricao = 'Cliente final que realiza reservas online'           WHERE nome = 'CLIENTE';

SELECT 'Encoding corrigido com sucesso!' AS resultado;
