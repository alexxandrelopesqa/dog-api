# dog-api

Testes de API contra a [Dog API](https://dog.ceo/dog-api/documentation) (`https://dog.ceo/api`).

![CI](https://github.com/alexxandrelopesqa/dog-api/actions/workflows/api-tests.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-blue)

## O que cobre

- `GET /breeds/list/all`
- `GET /breed/{breed}/images`
- `GET /breeds/image/random`

Contratos em JSON Schema (`src/test/resources/schemas/`), asserts em código e retry em 429, 5xx ou falha de rede.

## Stack

Java 17, Maven Wrapper, JUnit 5, Rest Assured, Allure. CI no GitHub Actions; relatório no Pages via branch `gh-pages`. Tem também `Jenkinsfile` e um `Dockerfile` opcional.

## Layout

```text
src/test/java/
  core/     — config, retry, asserts, Allure
  client/   — chamadas HTTP
  models/   — respostas
  tests/    — suíte

src/test/resources/
  schemas/, allure/, testdata/
```

## Rodar

Suite padrão:

```bash
./mvnw clean test
```

Windows (PowerShell), quando precisar passar `-D`:

```powershell
.\mvnw.cmd --% clean test -Pregression -Ddog.api.maxResponseTimeMs=5000
```

Perfis: `-Psmoke`, `-Pregression`. Um método:

```bash
./mvnw -Dtest=DogApiPositiveTests#shouldReturnValidRandomImage test
```

## Allure

```bash
./mvnw allure:report
```

Abre `target/site/allure-maven-plugin/index.html`. No site publicado, trends usam a pasta `history/` que o CI copia da última versão em `gh-pages`. A aba Retries do Allure vem do Surefire (`rerunFailingTestsCount`); no `pom` o default local é 0, no CI é 1 (`-Dsurefire.rerunFailingTestsCount=1`).

## GitHub Actions

Arquivo: `.github/workflows/api-tests.yml`. Matriz ubuntu/windows/macos, upload de relatórios, job que junta resultados do Ubuntu, gera HTML e faz push para `gh-pages`. No Windows o job usa `mvnw.cmd --%` para o PowerShell não tratar `-P`/`-D` como parâmetros dele.

Relatório: `https://alexxandrelopesqa.github.io/dog-api/`  
Pages: **Settings → Pages → Deploy from a branch → `gh-pages`**, pasta root.

## Jenkins

O `Jenkinsfile` faz checkout, `clean test -Pregression` com timeout de resposta alargado, `allure:report` e arquivo de `target/`. Agente precisa de JDK 17 e rede.

## Se der erro

- API lenta: `-Ddog.api.maxResponseTimeMs=5000`
- Pages sem atualizar: confirma fonte `gh-pages` e se o job de agregação/publicação correu no Actions
- Primeira vez sem branch `gh-pages`: o passo que restaura `history` pode não fazer nada ainda; normal

A API é pública; latência e falhas intermitentes acontecem.
