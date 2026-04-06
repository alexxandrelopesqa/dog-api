# Dog API - QA Automation Engineering Challenge

Repositório completo para avaliação técnica de QA API com foco em engenharia de qualidade, rastreabilidade e CI/CD.

API alvo: [Dog API](https://dog.ceo/dog-api/documentation)  
Base URL: `https://dog.ceo/api`

![CI](https://github.com/alexxandrelopesqa/dog-api/actions/workflows/api-tests.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-blue)
![Build](https://img.shields.io/badge/Maven-Wrapper-informational)

## 1) Visão geral do desafio e objetivo

Esta suíte valida 3 endpoints críticos da Dog API cobrindo:
- cenários positivos (funcional + contrato)
- cenários negativos/robustez (raça inválida e validação defensiva de payload)
- estabilidade com retries controlados apenas para falhas transitórias
- rastreabilidade operacional via Allure (steps, labels, anexos e metadados de execução)

## 2) Stack utilizada e justificativas

- `Java 17`: LTS, estabilidade e ampla adoção corporativa.
- `Maven + Maven Wrapper`: build reproduzível em qualquer sistema sem dependência de Maven global.
- `JUnit 5`: organização moderna de testes e execução robusta.
- `Rest Assured`: padrão de mercado para testes de API em Java.
- `Allure Report`: excelente visibilidade de evidências para troubleshooting.
- `GitHub Actions + Pages`: pipeline CI/CD com publicação contínua do relatório.
- `Docker` (opcional): execução padronizada em ambiente isolado.

## 3) Pré-requisitos

- Java 17+ (`java -version`)
- Git
- Acesso à internet para consumir `https://dog.ceo/api`
- Docker (opcional)

## 4) Setup local (Windows / Linux / macOS)

```bash
git clone <url-do-repo>
cd dog-api
```

O projeto já inclui `mvnw` e `mvnw.cmd`.

## 5) Como executar os testes

Suite completa:

- Windows (PowerShell):
```powershell
.\mvnw.cmd clean test
```

- Linux/macOS:
```bash
./mvnw clean test
```

Perfis de execução:

- `smoke` (cenários críticos rápidos):
```bash
./mvnw clean test -Psmoke
```

- `regression` (suíte completa funcional/negativa):
```bash
./mvnw clean test -Pregression
```

Teste específico:

- Windows:
```powershell
.\mvnw.cmd -Dtest=DogApiPositiveTests#shouldReturnValidRandomImage test
```

- Linux/macOS:
```bash
./mvnw -Dtest=DogApiPositiveTests#shouldReturnValidRandomImage test
```

## 6) Como gerar e abrir o Allure

- Windows:
```powershell
.\mvnw.cmd allure:report
start .\target\site\allure-maven-plugin\index.html
```

- Linux/macOS:
```bash
./mvnw allure:report
xdg-open ./target/site/allure-maven-plugin/index.html
```

Arquivos relevantes:
- resultados brutos: `target/allure-results`
- relatório HTML: `target/site/allure-maven-plugin`
- metadados automáticos:
  - `environment.properties`
  - `executor.json`
  - `categories.json`

## 7) Interpretação dos principais cenários cobertos

Cobertura obrigatória implementada:

1. `GET /breeds/list/all`
   - HTTP 200
   - `status=success`
   - `message` como mapa de raças/sub-raças
   - schema validation

2. `GET /breed/{breed}/images` (raça existente)
   - HTTP 200
   - lista de URLs válidas
   - schema validation

3. `GET /breeds/image/random`
   - HTTP 200
   - URL válida de imagem
   - schema validation

4. Negativo: raça inexistente (`invalidbreed`)
   - HTTP 404
   - `status=error`
   - mensagem coerente
   - schema de erro

5. Validação defensiva de payload
   - chaves obrigatórias (`status`, `message`)
   - tipos esperados
   - asserts explícitos para evitar falso-positivo

## 8) Troubleshooting comum

- Timeout/latência da API pública:
```bash
./mvnw clean test -Ddog.api.maxResponseTimeMs=5000
```

- Ajustar retries transitórios:
```bash
./mvnw clean test -Ddog.api.retryAttempts=5 -Ddog.api.retryBackoffMs=400
```

- Falha na geração Allure:
```bash
./mvnw clean test allure:report -e
```

- Pipeline sem publicar Pages:
  - habilite GitHub Pages em `Settings > Pages` com source `GitHub Actions`
  - execute novamente o workflow na branch `main`

## 9) Como funciona o pipeline GitHub Actions

Workflow: `.github/workflows/api-tests.yml`

Fluxo:
- **Job `test`**: matriz multi-OS (`ubuntu-latest`, `windows-latest`, `macos-latest`), setup Java 17, cache Maven, execução dos testes e upload de artifacts por sistema operacional.
- **Job `allure-aggregate`**: baixa `allure-results`, restaura `history` do `gh-pages` (quando existir), gera relatório consolidado e monta estrutura de publicação:
  - latest na raiz
  - snapshot por execução em `runs/<run_number>`
- **Job `deploy-pages`**: publica no GitHub Pages com permissões mínimas.

Hardening aplicado:
- actions pinadas por SHA
- princípio de least privilege por job
- sem uso de segredos hardcoded
- Dependabot para Maven e GitHub Actions
- `maven-enforcer-plugin` para garantir Java 17+ e Maven 3.9+

## 10) Link esperado do GitHub Pages

Após o primeiro deploy em `main`, o relatório ficará disponível em:

- latest: `https://alexxandrelopesqa.github.io/dog-api/`
- snapshot da execução: `https://alexxandrelopesqa.github.io/dog-api/runs/<run_number>/`

## 11) Limitações conhecidas e próximos passos

Limitações:
- API pública pode apresentar variação de latência e intermitência.
- Como o serviço é externo, não há controle sobre disponibilidade/SLAs reais.

Próximos passos:
- adicionar testes de contract-first com versionamento formal de schemas
- incluir testes de carga leve (k6/Gatling) para diagnóstico complementar
- integrar quality gates (ex.: SonarQube + cobertura mínima de assertions)
- publicar badges de pipeline e qualidade no README

## Estrutura do projeto

```text
src/test/java/
  core/
    AllureReportManager.java
    ApiAssertions.java
    BaseApiTest.java
    ConfigManager.java
    RequestSpecFactory.java
    RetryExecutor.java
    TestDataLoader.java
  client/
    DogApiClient.java
  models/
    ApiBaseResponse.java
    BreedImagesResponse.java
    BreedListResponse.java
    RandomImageResponse.java
  tests/
    DogApiNegativeTests.java
    DogApiPositiveTests.java
src/test/resources/
  allure.properties
  allure/
    categories.json
  schemas/
    breed-images-error-schema.json
    breed-images-success-schema.json
    breeds-list-all-schema.json
    random-image-schema.json
  testdata/
    breeds.json
.github/
  workflows/
    api-tests.yml
  dependabot.yml
```
