# Vai & Volta Viagens

Aplicação web de reservas de viagens construída com Java 21, Spring Boot, Maven e MySQL 8.

## Estrutura

- `app/`: aplicação Spring Boot e testes.
- `database/init/`: scripts ordenados de inicialização do banco.
- `database/legacy/`: scripts históricos que não integram o bootstrap.
- `.github/workflows/`: automação de integração contínua.

## Configuração local

Copie `.env.example` para `.env` e ajuste os valores de banco de dados para seu ambiente. A aplicação usa as variáveis `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD`; valores de desenvolvimento local são usados quando uma variável não é definida.

## Testes

Na pasta `app`, execute:

```powershell
.\mvnw.cmd test
```

Os testes unitários não exigem banco de dados. Os testes de integração e a execução completa serão disponibilizados com Docker e Maven na próxima etapa da refatoração.

## Fluxo Git

O repositório usa Git Flow: `main` representa versões estáveis, `develop` integra o desenvolvimento e as alterações são criadas em branches `feature/`, `bugfix/` e `release/` por meio de Pull Requests.
