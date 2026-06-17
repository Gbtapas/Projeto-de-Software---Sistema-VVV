-- UC18: Bloqueio temporário após 3 tentativas de login falhas
-- Execute este script UMA VEZ no banco de dados vvv antes de iniciar a aplicação.

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS tentativas_falhas INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS bloqueado_ate     DATETIME NULL;
