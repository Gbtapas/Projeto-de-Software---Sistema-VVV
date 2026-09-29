# Vai & Volta Viagens

Aplicação web de reservas de viagens construída com Java 21, Spring Boot, Maven e MySQL 8.

## Estrutura

- `app/`: aplicação Spring Boot e testes.
- `database/init/`: scripts ordenados de inicialização do banco.
- `database/legacy/`: scripts históricos que não integram o bootstrap.
- `.github/workflows/`: automação de integração contínua.

## Configuração local

Copie `.env.example` para `.env` e ajuste os valores de banco de dados para seu ambiente. A aplicação usa as variáveis `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` e `DB_PASSWORD`; valores de desenvolvimento local são usados quando uma variável não é definida.

## Docker

Com Docker Desktop em execução, inicie a aplicação e o MySQL com:

```powershell
docker compose --env-file .env.example up --build
```

A aplicação estará em `http://localhost:8080`. Para interromper os serviços, use `docker compose down`; para remover também os dados locais, use `docker compose down -v`.

## Testes

Na pasta `app`, execute:

```powershell
.\mvnw.cmd test
```

Os testes unitários não exigem banco de dados. O teste de integração sobe o contexto completo da aplicação com um banco H2 isolado:

```powershell
.\mvnw.cmd verify
```

## Fluxo Git

O repositório usa Git Flow: `main` representa versões estáveis, `develop` integra o desenvolvimento e as alterações são criadas em branches `feature/`, `bugfix/` e `release/` por meio de Pull Requests.
