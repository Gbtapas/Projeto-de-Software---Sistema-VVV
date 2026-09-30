-- UC18: bloqueio de conta após 3 tentativas de login falhas.
-- Compatível com MySQL que não suporta ADD COLUMN IF NOT EXISTS.

SET @tentativas_existe = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'usuarios'
      AND column_name = 'tentativas_falhas'
);
SET @comando = IF(
    @tentativas_existe = 0,
    'ALTER TABLE usuarios ADD COLUMN tentativas_falhas INT NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE comando FROM @comando;
EXECUTE comando;
DEALLOCATE PREPARE comando;

SET @bloqueio_existe = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'usuarios'
      AND column_name = 'bloqueado_ate'
);
SET @comando = IF(
    @bloqueio_existe = 0,
    'ALTER TABLE usuarios ADD COLUMN bloqueado_ate DATETIME NULL',
    'SELECT 1'
);
PREPARE comando FROM @comando;
EXECUTE comando;
DEALLOCATE PREPARE comando;
