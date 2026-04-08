# dog-api

Automação de testes de API contra a [Dog API](https://dog.ceo/dog-api/documentation) — base `https://dog.ceo/api`.

![CI](https://github.com/alexxandrelopesqa/dog-api/actions/workflows/api-tests.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-blue)

## Requisitos

- **JDK** 17+ (o Enforcer exige `[17,)`)
- **Maven** 3.9+ (Enforcer; o wrapper `mvnw` já traz uma versão compatível)
- Rede para a API pública e para o Maven
- Opcional: Docker (imagem no `Dockerfile`), Jenkins (pipeline no `Jenkinsfile`)

## Stack

| Componente | Uso |
|------------|-----|
| Java 17 | Linguagem |
| Maven Wrapper | Build e `allure:report` |
| JUnit 5 | `@Tag` smoke / regression |
| Rest Assured + JSON Schema | Chamadas e contratos |
| Allure | Relatório e anexos |
| GitHub Actions | CI multi-OS + agregação + deploy `gh-pages` |
| Dependabot | Atualizações Maven e Actions (semanal) |

Versões principais estão no `pom.xml` (JUnit 5.10.x, Rest Assured 5.4.x, Allure 2.25.x, etc.).

## O que os testes cobrem

- `GET /breeds/list/all`
- `GET /breeds/image/random` e `/breeds/image/random/{n}` (ex.: pedir 51 URLs → no máximo 50)
- `GET /breed/{breed}/images`, `.../images/random`, `.../images/random/{n}`
- `GET /breed/{breed}/list` (sub-raças; pode ser `[]`)
- `GET /breed/{breed}/{sub}/images` e variantes `.../random` e `.../random/{n}`
- Respostas **404** com raça ou sub-raça inválida

Contratos em `src/test/resources/schemas/`. Retry só para **429**, **5xx** ou falha de rede (`core/RetryExecutor`).

### Classes de teste

| Ficheiro | Conteúdo |
|----------|------------|
| `DogApiPositiveTests` | Caminhos felizes (lista, imagens, random) |
| `DogApiNegativeTests` | 404 e validações de erro / shape JSON |
| `DogApiRegressionTests` | Random N, listas, sub-raças, teto de 50 URLs |

Com `-Pregression` são **20** casos. Com `-Psmoke` corre um subconjunto marcado com `@Tag("smoke")`.

## Estrutura do projeto

```text
src/test/java/
  core/       ConfigManager, RetryExecutor, ApiAssertions, AllureReportManager, …
  client/     DogApiClient
  models/     POJOs das respostas
  tests/      Classes *Tests.java

src/test/resources/
  schemas/     JSON Schema
  allure/      categories.json
  testdata/    breeds.json (raças de exemplo)
```

## Executar localmente

Suite completa com perfil de regressão (recomendado):

```bash
./mvnw clean test -Pregression -Ddog.api.maxResponseTimeMs=5000
```

Windows (PowerShell) — o `--%` evita que o PowerShell interprete `-P` e `-D`:

```powershell
.\mvnw.cmd --% clean test -Pregression -Ddog.api.maxResponseTimeMs=5000
```

Só smoke:

```bash
./mvnw clean test -Psmoke
```

Um método:

```bash
./mvnw -Dtest=DogApiPositiveTests#shouldReturnValidRandomImage test
```

### Propriedades do sistema (`-D`)

| Propriedade | Default | Descrição |
|-------------|---------|-----------|
| `dog.api.baseUrl` | `https://dog.ceo/api` | Base da API |
| `dog.api.maxResponseTimeMs` | `3000` | SLA de tempo de resposta nos asserts |
| `dog.api.retryAttempts` | `3` | Tentativas em falhas transitórias |
| `dog.api.retryBackoffMs` | `250` | Espera entre tentativas (ms) |

No CI e no Jenkins usa-se `maxResponseTimeMs=5000` e `surefire.rerunFailingTestsCount=1` (aba Retries no Allure).

## Relatório Allure (local)

```bash
./mvnw allure:report
```

Abrir `target/site/allure-maven-plugin/index.html`.

## GitHub Actions

Ficheiro: `.github/workflows/api-tests.yml`.

1. **Job `test`:** matriz `ubuntu-latest`, `windows-latest`, `macos-latest`; `clean test -Pregression` com os mesmos `-D` que acima; upload de `surefire-reports` e `allure-results` por SO.
2. **Job `allure-aggregate`:** descarrega só `allure-results-ubuntu-latest`, opcionalmente restaura `history` a partir da branch `gh-pages`, corre `mvn allure:report`, monta `public/` (com `.nojekyll` e validação de `index.html`) e faz push com `peaceiris/actions-gh-pages` para **`gh-pages`**.

Em **pull requests** não há push para Pages — só geração de relatório e artefactos.

### Site público do relatório

URL: `https://alexxandrelopesqa.github.io/dog-api/`

**Settings → Pages → Build and deployment:** tem de ser **Deploy from a branch**, branch **`gh-pages`**, pasta **`/` (root)**. Se escolheres **`main`**, o Pages mostra o código do repositório, **não** o Allure. A fonte **GitHub Actions** (deploy por artefacto) **não** corresponde a este fluxo.

Tendências no Allure usam a pasta `history/` copiada do deploy anterior em `gh-pages`.

## Jenkins

O `Jenkinsfile` faz checkout, `clean test -Pregression` com timeout de resposta 5000 ms, `allure:report` e arquivo de `target/` (Surefire, resultados Allure, HTML). Agente com JDK 17 e acesso à rede.

## Docker

`docker build -t dog-api-tests .` e `docker run --rm dog-api-tests` executam testes + `allure:report` dentro do contentor (Maven 3.9 + Temurin 17 na imagem base).

## Problemas frequentes

| Situação | O que fazer |
|----------|-------------|
| Falhas por latência da API | Aumentar `-Ddog.api.maxResponseTimeMs=5000` |
| Página do relatório errada no GitHub | Confirmar Pages em **`gh-pages`**, não `main` |
| Sem histórico no Allure | Normal na primeira publicação; nas seguintes o `history` é restaurado |
| Windows no CI sem artefactos | O workflow já usa `mvnw.cmd --%` para passar `-P`/`-D` ao Maven |

A API é externa: indisponibilidade ou flakiness são esperáveis.
