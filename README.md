# dog-api

Testes de API contra a [Dog API](https://dog.ceo/dog-api/documentation) (`https://dog.ceo/api`).

![CI](https://github.com/alexxandrelopesqa/dog-api/actions/workflows/api-tests.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-blue)

## O que cobre

- `GET /breeds/list/all`
- `GET /breeds/image/random` e `/breeds/image/random/{n}` (pedir 51 volta no máximo 50)
- `GET /breed/{breed}/images`, `.../random`, `.../random/{n}`
- `GET /breed/{breed}/list` (sub-racas; pode ser `[]`)
- `GET /breed/{breed}/{sub}/images` e `.../random`, `.../random/{n}`
- 404 com raca ou sub invalida

Schemas em `src/test/resources/schemas/`, retry em 429/5xx/rede.

## Stack

Java 17, Maven Wrapper, JUnit 5, Rest Assured, Allure. GitHub Actions + Pages (`gh-pages`). `Jenkinsfile` e `Dockerfile` opcionais.

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

```bash
./mvnw clean test
```

Windows (PowerShell), com `-D`:

```powershell
.\mvnw.cmd --% clean test -Pregression -Ddog.api.maxResponseTimeMs=5000
```

Perfis: `-Psmoke`, `-Pregression`. Exemplo de um método:

```bash
./mvnw -Dtest=DogApiPositiveTests#shouldReturnValidRandomImage test
```

## Allure

```bash
./mvnw allure:report
```

Abre `target/site/allure-maven-plugin/index.html`. Trends no site publicado usam `history/` vinda do último deploy em `gh-pages`. Aba Retries = Surefire (`rerunFailingTestsCount`: 0 no `pom` local, 1 no CI).

## GitHub Actions

`.github/workflows/api-tests.yml`: matriz de OS, upload de artefatos, agregação no Ubuntu, push `gh-pages`. Windows: `mvnw.cmd --%` para não partir os `-P`/`-D` no PowerShell.

Relatório: `https://alexxandrelopesqa.github.io/dog-api/` (tem de incluir `/dog-api/` no path).

**Pages:** em **Settings → Pages → Build and deployment**, a fonte tem de ser **Deploy from a branch**, branch **`gh-pages`**, pasta **`/` (root)**. Se estiver **GitHub Actions** como fonte, o site não usa o `index.html` que este repo envia para `gh-pages` — aí aparece página errada ou vazia. Depois de mudar, espera 1–2 minutos e atualiza com cache limpo (Ctrl+F5).

## Jenkins

`Jenkinsfile`: checkout, testes com timeout de resposta alto, `allure:report`, arquivo de `target/`. JDK 17 e rede.

## Se der erro

- API lenta: `-Ddog.api.maxResponseTimeMs=5000`
- Pages velho: confere fonte `gh-pages` e o último run no Actions
- Sem `gh-pages` ainda: sem `history` na primeira vez

API pública: pode falhar ou demorar.
