# dog-api

Testes automatizados contra a [Dog API](https://dog.ceo/dog-api/documentation) (`https://dog.ceo/api`).

![CI](https://github.com/alexxandrelopesqa/dog-api/actions/workflows/api-tests.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-blue)

## Requisitos

JDK 17+, Maven 3.9+ (o `pom` força isso com o Enforcer), rede. Opcional: Docker ou Jenkins se quiseres correr igual ao servidor.

## Ferramentas

Java 17, Maven Wrapper, JUnit 5 com tags `smoke` e `regression`, Rest Assured e validação com JSON Schema, Allure para relatório. CI no GitHub Actions (ubuntu/windows/macos); Dependabot para dependências. Versões concretas no `pom.xml`.

## Endpoints exercitados

- `GET /breeds/list/all`
- `GET /breeds/image/random` e `/breeds/image/random/{n}` (pedir 51 devolve no máximo 50 URLs)
- `GET /breed/{breed}/images`, random e random com N
- `GET /breed/{breed}/list` (lista de sub-raças; pode vir vazia)
- `GET /breed/{breed}/{sub}/images` e variantes com random
- Respostas 404 para raça ou sub-raça inválida

Schemas em `src/test/resources/schemas/`. Retry em 429, 5xx ou erro de rede — ver `core/RetryExecutor`.

Há três classes: `DogApiPositiveTests`, `DogApiNegativeTests`, `DogApiRegressionTests`. Com `./mvnw test -Pregression` são 20 testes; com `-Psmoke` só os marcados com `@Tag("smoke")`.

## Pastas

```
src/test/java/   core, client, models, tests
src/test/resources/   schemas, allure, testdata
```

## Correr

Regressão (o que costumo usar):

```bash
./mvnw clean test -Pregression -Ddog.api.maxResponseTimeMs=5000
```

PowerShell precisa de `--%` antes dos argumentos do Maven, senão o `-P` e os `-D` partem-se:

```powershell
.\mvnw.cmd --% clean test -Pregression -Ddog.api.maxResponseTimeMs=5000
```

Smoke: `./mvnw clean test -Psmoke`

Um teste só: `./mvnw -Dtest=DogApiPositiveTests#shouldReturnValidRandomImage test`

Propriedades úteis: `dog.api.baseUrl` (default `https://dog.ceo/api`), `dog.api.maxResponseTimeMs` (3000 no código, 5000 no CI), `dog.api.retryAttempts`, `dog.api.retryBackoffMs`. No CI/Jenkins também vai `-Dsurefire.rerunFailingTestsCount=1` para a aba Retries no Allure.

Relatório local: `./mvnw allure:report` e abre `target/site/allure-maven-plugin/index.html`.

## GitHub Actions

Ficheiro `.github/workflows/api-tests.yml` — testes em três SOs com artefactos de Surefire e Allure por SO.

O job que gera o HTML e publica no Pages **só descarrega `allure-results-ubuntu-latest`**. Assim o relatório online não triplica os mesmos 20 testes (Windows e macOS continuam nos artefactos da matriz para comparar se precisares).

Em PRs não há push para `gh-pages`.

Relatório online: `https://alexxandrelopesqa.github.io/dog-api/`

Em **Settings → Pages** a fonte tem de ser **branch `gh-pages`**, pasta **root**. Se estiver em `main`, vês o repositório em vez do relatório.

## Jenkins e Docker

`Jenkinsfile`: checkout, testes com timeout 5000 ms no assert, `allure:report`, arquivo de `target/`.

`Dockerfile`: imagem Maven + Temurin 17; `docker build -t dog-api-tests .` e `docker run --rm dog-api-tests` corre testes e `allure:report`.

## Se algo falhar

Latência da API: subir `dog.api.maxResponseTimeMs`. Pages a mostrar código: rever branch `gh-pages`. Primeira vez sem histórico no Allure: normal. No Windows do Actions se faltar artefacto, o `mvnw.cmd` já vai com `--%`.

API pública — às vezes falha ou demora.
