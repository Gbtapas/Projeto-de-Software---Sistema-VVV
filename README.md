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

## CI/CD e qualidade

O workflow `.github/workflows/ci.yml` executa em pushes e Pull Requests:

- testes unitários;
- testes de integração;
- build da imagem Docker;
- análise SonarCloud, quando configurada.

Para ativar o SonarCloud, importe este repositório na organização `tipilegal` do SonarCloud e crie o secret de Actions `SONAR_TOKEN` com o token gerado pela plataforma. O pipeline detecta esse secret e executa a análise automaticamente; o token nunca é armazenado no repositório.

## Publicação no Railway

O arquivo `railway.toml` instrui o Railway a usar o `Dockerfile` da raiz. Para publicar:

1. Crie um projeto no Railway e adicione um serviço MySQL.
2. Adicione este repositório como serviço de aplicação e mantenha o diretório-raiz como contexto de build.
3. Configure as variáveis abaixo na aplicação, usando referências ao serviço MySQL criado:

| Variável da aplicação | Referência Railway |
| --- | --- |
| `DB_HOST` | `${{MySQL.MYSQLHOST}}` |
| `DB_PORT` | `${{MySQL.MYSQLPORT}}` |
| `DB_NAME` | `${{MySQL.MYSQLDATABASE}}` |
| `DB_USERNAME` | `${{MySQL.MYSQLUSER}}` |
| `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
| `SERVER_PORT` | `${{PORT}}` |
| `SHOW_SQL` | `false` |

4. Execute uma única vez os scripts de `database/init/` no MySQL, respeitando a ordem descrita em `database/README.md`.
5. Gere um domínio público no serviço de aplicação e confira a rota `/`.

O nome `MySQL` nas referências deve coincidir com o nome do serviço de banco no Railway.
