# Dog API Test Automation

Projeto de automacao de testes de API para a [Dog API](https://dog.ceo/dog-api/documentation), com foco em:
- confiabilidade dos testes
- rastreabilidade detalhada com Allure
- execucao padronizada em ambiente local e CI/CD

## Quick start (2 minutos)

### Windows (PowerShell)
```powershell
.\mvnw.cmd clean test
.\mvnw.cmd allure:report
start .\target\site\allure-maven-plugin\index.html
```

### Linux/macOS
```bash
./mvnw clean test
./mvnw allure:report
xdg-open ./target/site/allure-maven-plugin/index.html
```

Saidas esperadas:
- resultados de testes: `target/surefire-reports`
- resultados brutos Allure: `target/allure-results`
- relatorio HTML Allure: `target/site/allure-maven-plugin/index.html`

## Objetivo

Validar endpoints criticos da Dog API garantindo:
- conformidade de contrato JSON (schema validation)
- comportamento funcional (status code e payload)
- desempenho basico (tempo de resposta)
- evidencias de execucao e falha com rastreabilidade rica no Allure

## Escopo coberto

| Endpoint | Tipo de teste | Contrato | Payload | Tempo de resposta |
|---|---|---|---|---|
| `GET /breeds/list/all` | funcional | schema JSON | `status=success` | validado |
| `GET /breed/{breed}/images` | parametrizado (validas/invalidas) | schemas sucesso/erro | regras por cenario | validado |
| `GET /breeds/image/random` | funcional | schema JSON | URL de imagem valida | validado |

## Arquitetura do projeto

```text
src/test/java/
  utils/
    ConfigManager.java
    RequestSpecFactory.java
    AllureReportManager.java
  clients/
    DogApiClient.java
  tests/
    DogApiTests.java
src/test/resources/
  allure.properties
  schemas/
    breeds-list-all-schema.json
    breed-images-success-schema.json
    breed-images-error-schema.json
    random-image-schema.json
```

## Decisoes tecnicas

- **Padrao Client:** `DogApiClient` encapsula chamadas HTTP e reduz duplicacao.
- **Config centralizada:** `ConfigManager` permite override via `-D`.
- **Request spec unica:** `RequestSpecFactory` padroniza base URL, headers e filtros.
- **Contratos desacoplados:** schemas JSON separados em `src/test/resources/schemas`.
- **Observabilidade Allure:** steps, anexos de API, contexto de execucao, timeline de retry e contexto de assercao.

## Passo a passo de implementacao

1. Bootstrap Maven + Java 17 + wrapper + Docker.
2. Criacao da base de testes (`ConfigManager` e `RequestSpecFactory`).
3. Implementacao do cliente da API (`DogApiClient`).
4. Implementacao dos schemas JSON e testes por endpoint.
5. Enriquecimento de rastreabilidade Allure.
6. Pipeline CI/CD com artefatos e publicacao no GitHub Pages.

## Stack

- Java 17+
- Maven / Maven Wrapper
- JUnit 5
- RestAssured
- Allure
- Docker
- GitHub Actions + GitHub Pages

## Pre-requisitos

- JDK 17+ (`java -version`)
- Docker (opcional, para execucao containerizada)
- Internet para acessar `https://dog.ceo/api`

Observacao: Maven global e opcional, pois o projeto usa wrapper (`mvnw` / `mvnw.cmd`).

## Execucao local

### CLI (recomendado)

#### Testes

Windows:
```powershell
.\mvnw.cmd clean test
```

Linux/macOS:
```bash
./mvnw clean test
```

#### Relatorio Allure

Windows:
```powershell
.\mvnw.cmd allure:report
start .\target\site\allure-maven-plugin\index.html
```

Linux/macOS:
```bash
./mvnw allure:report
xdg-open ./target/site/allure-maven-plugin/index.html
```

### IDE

1. Importar como projeto Maven.
2. Executar `tests.DogApiTests`.
3. Rodar goal `allure:report`.

## Configuracoes por propriedade (`-D`)

| Propriedade | Default | Uso |
|---|---|---|
| `dog.api.baseUrl` | `https://dog.ceo/api` | muda endpoint base |
| `dog.api.maxResponseTimeMs` | `2000` | define limite de SLA |
| `dog.api.slaAttempts` | `10` | numero maximo de tentativas por chamada |
| `dog.api.slaBackoffMs` | `200` | intervalo entre tentativas |

Exemplo:
```bash
./mvnw clean test -Ddog.api.maxResponseTimeMs=2500 -Ddog.api.slaAttempts=12
```

## Allure: logs e rastreabilidade

O projeto gera:
- labels (`epic`, `feature`, `story`, `severity`, `owner`)
- steps de validacao (`Allure.step(...)`)
- anexos por chamada:
  - `Request Summary`
  - `Response Summary`
  - `Response Body`
- anexos de diagnostico:
  - `Execution Context`
  - `SLA Retry Timeline`
  - `Assertion Context` (quando houver falha)
- metadados automaticos:
  - `target/allure-results/environment.properties`
  - `target/allure-results/executor.json`
  - `target/allure-results/categories.json`

## Execucao via Docker

Build:
```bash
docker build -t dog-api-tests .
```

Run (Linux/macOS):
```bash
docker run --rm -v ${PWD}/target:/app/target dog-api-tests
```

Run (Windows PowerShell):
```powershell
docker run --rm -v ${PWD}\target:/app/target dog-api-tests
```

## CI/CD (GitHub Actions + GitHub Pages)

Workflow: `.github/workflows/ci.yml`

Fluxo:
1. Checkout
2. Setup Java 17 + cache Maven
3. Testes com parametros resilientes para API publica:
   - `./mvnw clean test -Ddog.api.maxResponseTimeMs=5000 -Ddog.api.slaAttempts=15`
4. Restore de historico Allure (se existir em `gh-pages/history`)
5. `./mvnw allure:report`
6. Upload de artefatos (`surefire`, `allure-results`, `allure HTML`)
7. Publicacao no GitHub Pages (branch `main`)

Hardening:
- job `test` com permissao minima: `contents: read`
- deploy com permissao separada: `pages: write`, `id-token: write`, `contents: read`

## Jenkins (referencia rapida)

Pipeline minima:
1. Checkout do repositório
2. `./mvnw clean test -Ddog.api.maxResponseTimeMs=5000 -Ddog.api.slaAttempts=15`
3. `./mvnw allure:report`
4. Publicar artefatos de:
   - `target/surefire-reports`
   - `target/allure-results`
   - `target/site/allure-maven-plugin`

## Troubleshooting

- **Timeout/rede na API externa**
  ```bash
  ./mvnw clean test -e
  ```

- **SLA falhando por variacao de latencia da API publica**
  ```bash
  ./mvnw clean test -Ddog.api.maxResponseTimeMs=5000 -Ddog.api.slaAttempts=15
  ```

- **Allure report nao gerado**
  ```bash
  ./mvnw clean test allure:report
  ```

- **GitHub Pages nao publica**
  - conferir `Settings > Pages > Source = GitHub Actions`
  - validar se o job `deploy-pages` executou com sucesso

## Checklist de validacao final

- [ ] `clean test` executa sem erro
- [ ] `allure:report` gera HTML local
- [ ] workflow no GitHub Actions em verde
- [ ] relatorio publicado no GitHub Pages
- [ ] artefatos anexados na execucao de CI
