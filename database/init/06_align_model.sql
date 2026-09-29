-- ============================================================
--  Sistema Vai&Volta Viagens (VVV)
--  06_align_model.sql — Alinhamento do modelo às correções do professor
--
--  Objetivo: separar CLIENTE (quem compra) de PASSAGEIRO (quem viaja), com
--  Cliente (1) — (n) Passageiro, e Reserva associada ao Cliente comprador.
--
--  - NÃO edita 01-05. Idempotente (checa information_schema antes de alterar).
--  - Colunas novas são NULLABLE → não quebram as triggers/constraints existentes.
--  - Rodar uma vez:  mysql -u root -p vvv < 06_align_model.sql
-- ============================================================

USE vvv;

-- ---- Tabela clientes ---------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    codigo      VARCHAR(20)     NOT NULL,
    nome        VARCHAR(150)    NOT NULL,
    cpf         CHAR(11)        NOT NULL COMMENT 'Apenas dígitos',
    email       VARCHAR(150)    NULL,
    telefone    VARCHAR(20)     NULL,
    ativo       TINYINT(1)      NOT NULL DEFAULT 1,
    criado_em   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- vínculo opcional com a credencial de acesso (generalização Usuário→Cliente)
    id_usuario  BIGINT UNSIGNED NULL,

    PRIMARY KEY (id_cliente),
    UNIQUE KEY  uq_clientes_codigo (codigo),
    UNIQUE KEY  uq_clientes_cpf    (cpf),
    UNIQUE KEY  uq_clientes_usuario (id_usuario),
    CONSTRAINT  fk_clientes_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario) ON DELETE SET NULL
) ENGINE = InnoDB
  COMMENT = 'Clientes (quem compra) — separado de passageiros (quem viaja)';


-- ---- reservas.id_cliente (comprador) -----------------------
SET @existe := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = 'vvv' AND TABLE_NAME = 'reservas'
                  AND COLUMN_NAME = 'id_cliente');
SET @sql := IF(@existe = 0,
    'ALTER TABLE reservas
        ADD COLUMN id_cliente BIGINT UNSIGNED NULL COMMENT ''Cliente que efetuou a compra'',
        ADD CONSTRAINT fk_res_cliente FOREIGN KEY (id_cliente)
            REFERENCES clientes (id_cliente) ON DELETE SET NULL',
    'SELECT ''reservas.id_cliente ja existe'' AS info');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;


-- ---- passageiros.id_cliente (a quem o passageiro pertence) --
SET @existe := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = 'vvv' AND TABLE_NAME = 'passageiros'
                  AND COLUMN_NAME = 'id_cliente');
SET @sql := IF(@existe = 0,
    'ALTER TABLE passageiros
        ADD COLUMN id_cliente BIGINT UNSIGNED NULL COMMENT ''Cliente dono do cadastro do passageiro'',
        ADD CONSTRAINT fk_pass_cliente FOREIGN KEY (id_cliente)
            REFERENCES clientes (id_cliente) ON DELETE SET NULL',
    'SELECT ''passageiros.id_cliente ja existe'' AS info');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;


-- ---- Dados de exemplo (idempotente) ------------------------
INSERT INTO clientes (codigo, nome, cpf, email, telefone)
SELECT 'CLI000001', 'Cliente Demonstração', '52998224725', 'cliente.demo@vvv.com.br', '21999990000'
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE cpf = '52998224725');
