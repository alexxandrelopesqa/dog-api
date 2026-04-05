# Dog API Test Automation

Projeto de automacao de testes de API para a [Dog API](https://dog.ceo/dog-api/documentation), com foco em qualidade de producao, rastreabilidade de falhas e pipeline CI/CD com publicacao de relatorios.

## Objetivo

Validar endpoints criticos da Dog API garantindo:
- conformidade de contrato JSON (schema validation),
- comportamento funcional (status code e payload),
- desempenho basico (tempo de resposta abaixo de 2000 ms),
- evidencias de execucao via Allure local e CI.

## Arquitetura e decisoes tecnicas

Estrutura:

```text
src/test/java/
  utils/
    ConfigManager.java
    RequestSpecFactory.java
  clients/
    DogApiClient.java
  tests/
    DogApiTests.java
src/test/resources/schemas/
  breeds-list-all-schema.json
  breed-images-success-schema.json
  breed-images-error-schema.json
  random-image-schema.json
```

Decisoes:
- **Padrao Client** com `DogApiClient` para encapsular chamadas HTTP e evitar duplicacao.
- **Factory de RequestSpecification** para centralizar base URL, headers e filtros.
- **ConfigManager** para permitir override de configuracoes via `-D` sem alterar codigo.
- **Schemas em resources** para validacao contratual desacoplada dos testes.
- **@ParameterizedTest** para cobrir racas validas e invalidas no endpoint `/breed/{breed}/images`.
- **Observabilidade em falha** via `RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()` + anexos no Allure.
- **Recursos avancados do Allure**: `@Epic`, `@Feature`, `@Story`, `@Severity`, `@Owner`, `Allure.step(...)`, anexos de request/response, `environment.properties`, `executor.json` e `categories.json`.

## Passo a passo da implementacao

1. **Bootstrap do projeto**
   - Criacao do `pom.xml` com Java 17+, JUnit 5, RestAssured e Allure.
   - Organizacao inicial da estrutura de pastas e configuracao de ignore.
2. **Camada de base para testes**
   - Implementacao de `ConfigManager` para centralizar parametros.
   - Implementacao de `RequestSpecFactory` para padronizar configuracao HTTP.
3. **Cliente da API**
   - Implementacao de `DogApiClient` com metodos dedicados para cada endpoint coberto.
4. **Contratos e cenarios de teste**
   - Criacao dos schemas JSON dos endpoints.
   - Implementacao de testes com validacoes de status code, schema, payload e tempo.
   - Parametrizacao de racas validas e invalidas para `/breed/{breed}/images`.
5. **Observabilidade e relatorios**
   - Integracao com Allure (steps, anexos de request/response, metadados de ambiente e executor).
   - Geracao de relatorio HTML local.
6. **CI/CD e publicacao**
   - Workflow GitHub Actions com cache Maven, artefatos e deploy no GitHub Pages.
   - Reaproveitamento do historico Allure para acompanhar tendencia de execucoes.

## Stack

- Java 17+
- Maven
- JUnit 5
- RestAssured
- Allure
- GitHub Actions
- Docker

## Pre-requisitos

- JDK 17+ instalado (`java -version`)
- Maven 3.9+ instalado (`mvn -version`)
- Docker (opcional, para execucao containerizada)
- Acesso a internet para atingir `https://dog.ceo/api`

## Execucao local

### Via CLI

Executar testes:

Linux/macOS:
```bash
./mvnw clean test
```

Windows (PowerShell):
```powershell
.\mvnw.cmd clean test
```

Gerar relatorio Allure em HTML:

Linux/macOS:
```bash
./mvnw allure:report
```

Windows (PowerShell):
```powershell
.\mvnw.cmd allure:report
```

Saida esperada:
- resultados brutos: `target/allure-results`
- HTML: `target/site/allure-maven-plugin/index.html`

### Via IDE

1. Importar projeto Maven.
2. Executar classe `tests.DogApiTests`.
3. Rodar goal Maven `allure:report` para gerar o HTML.

## Configuracoes opcionais

Sem alterar codigo, voce pode sobrescrever:
- `dog.api.baseUrl` (default: `https://dog.ceo/api`)
- `dog.api.maxResponseTimeMs` (default: `2000`)
- `dog.api.slaAttempts` (default: `10`)
- `dog.api.slaBackoffMs` (default: `200`)

Exemplo:

```bash
./mvnw clean test -Ddog.api.maxResponseTimeMs=2500
```

## Execucao via Docker

Build da imagem:

```bash
docker build -t dog-api-tests .
```

Executar testes + geracao de relatorio:

```bash
docker run --rm -v ${PWD}/target:/app/target dog-api-tests
```

No PowerShell:

```powershell
docker run --rm -v ${PWD}\target:/app/target dog-api-tests
```

## Estrutura de CI/CD

Workflow: `.github/workflows/ci.yml`

Fluxo:
1. Checkout
2. Setup Java 17 com cache Maven
3. `./mvnw clean test`
4. Restauracao de historico Allure (quando houver `gh-pages/history`)
5. `./mvnw allure:report`
6. Upload de artefatos:
   - `target/surefire-reports`
   - `target/allure-results`
   - `target/site/allure-maven-plugin`
7. Publicacao no GitHub Pages (apenas branch `main`)

Hardening aplicado:
- `permissions` minimas no job `test` (`contents: read`)
- permissoes de escrita isoladas no job `deploy-pages` (`pages: write`, `id-token: write`, `contents: read`)
- sem privilegios desnecessarios em jobs de teste
- tendencia Allure preservada entre execucoes via pasta `history` do `gh-pages`

## Recursos Allure habilitados

- Labels de rastreabilidade (epic/feature/story/owner/severity)
- Steps detalhados por validacao
- Anexos tecnicos por chamada de API (request summary, response summary, response body)
- Metadados automaticos:
  - `target/allure-results/environment.properties`
  - `target/allure-results/executor.json`
  - `target/allure-results/categories.json`

## Troubleshooting

- **Erro de timeout/rede na API externa**  
  Verifique conectividade e proxy corporativo. Rode novamente:
  ```bash
  ./mvnw clean test -e
  ```

- **Falha por tempo de resposta > 2000ms**  
  Pode ser variacao de rede/transiente da API publica. Reexecute e valide estabilidade:
  ```bash
  ./mvnw clean test
  ```
  Se necessario, ajuste temporariamente:
  ```bash
  ./mvnw clean test -Ddog.api.maxResponseTimeMs=2500
  ```

- **Allure report nao gerado**  
  Garanta que testes executaram antes e que existe `target/allure-results`:
  ```bash
  ./mvnw clean test allure:report
  ```

- **Docker sem relatorio no host**  
  Valide o bind mount para `target` no comando `docker run`.
