-- UC18: bloqueio de conta após 3 tentativas de login falhas
-- Executar uma única vez no banco vvv_db

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS tentativas_falhas INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS bloqueado_ate     DATETIME NULL;
