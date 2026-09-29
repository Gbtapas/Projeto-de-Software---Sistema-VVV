# Banco de dados

Os arquivos em `init/` formam a inicialização ordenada do banco MySQL do sistema.
Eles são executados nessa sequência:

1. Criação do banco (`01_database.sql`)
2. Tabelas (`02_tables.sql`)
3. Índices e views (`03_indexes_views.sql`)
4. Triggers (`04_triggers.sql`)
5. Dados iniciais (`05_seed.sql`)
6. Ajuste do modelo (`06_align_model.sql`)
7. Regra de bloqueio de login (`07_uc18_login_lockout.sql`)

O Docker Compose monta essa pasta em `docker-entrypoint-initdb.d`; em uma instalação manual, execute os arquivos na mesma ordem.

Os scripts em `legacy/` são históricos ou corretivos e não são executados automaticamente.
