# Dog API - Teste Técnico QA API

Este repositório contém a automação de testes de API para a [Dog API](https://dog.ceo/dog-api/documentation), com foco em qualidade funcional, validação de contrato e execução reproduzível em diferentes ambientes.

Base URL: `https://dog.ceo/api`

![CI](https://github.com/alexxandrelopesqa/dog-api/actions/workflows/api-tests.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-blue)

## Objetivo

Validar endpoints críticos da Dog API, garantindo:

- respostas corretas (HTTP e payload)
- estrutura esperada dos dados
- comportamento consistente em cenários positivos e negativos
- rastreabilidade para análise de falhas

## Endpoints cobertos

- `GET /breeds/list/all`
- `GET /breed/{breed}/images`
- `GET /breeds/image/random`

## Stack utilizada

- Java 17
- Maven + Maven Wrapper (`mvnw`, `mvnw.cmd`)
- JUnit 5
- Rest Assured
- Allure Report
- GitHub Actions + GitHub Pages
- Jenkins (pipeline declarativa via `Jenkinsfile`)
- Docker (opcional)

## Arquitetura do projeto

```text
src/test/java/
  core/      -> configuração base, retry, assertions, hooks e metadados Allure
  client/    -> cliente HTTP da Dog API
  models/    -> POJOs para desserialização
  tests/     -> classes de teste (positivos e negativos)

src/test/resources/
  schemas/   -> contratos JSON Schema
  allure/    -> categories.json
  testdata/  -> massa de testes
```

## Cenários de teste implementados

### Positivos

1. `GET /breeds/list/all`
   - HTTP `200`
   - `status=success`
   - payload com mapa de raças/sub-raças
   - schema validation

2. `GET /breed/{breed}/images` para raça válida
   - HTTP `200`
   - lista de URLs de imagens
   - schema validation

3. `GET /breeds/image/random`
   - HTTP `200`
   - URL válida de imagem
   - schema validation

### Negativos e robustez

1. `GET /breed/{breed}/images` com raça inválida
   - HTTP `404`
   - `status=error`
   - mensagem coerente de erro
   - schema de erro

2. Validação defensiva de payload
   - checagem de campos obrigatórios
   - checagem de tipos esperados

### Estabilidade

- Retry aplicado apenas para falhas transitórias (`429`, `5xx` e exceções de rede), sem mascarar falhas reais de contrato/funcional.

## Pré-requisitos

- JDK 17+
- Git
- acesso à internet para consumir a Dog API pública
- Docker (opcional)

## Como executar localmente

### Suite completa

Windows (PowerShell):

```powershell
.\mvnw.cmd clean test
```

Linux/macOS:

```bash
./mvnw clean test
```

### Perfis

Smoke:

```bash
./mvnw clean test -Psmoke
```

Regression:

```bash
./mvnw clean test -Pregression
```

### Teste específico

```bash
./mvnw -Dtest=DogApiPositiveTests#shouldReturnValidRandomImage test
```

### Execução recomendada para reduzir flakiness de API pública

Windows (PowerShell):

```powershell
.\mvnw.cmd --% clean test -Pregression -Ddog.api.maxResponseTimeMs=5000
```

Linux/macOS:

```bash
./mvnw clean test -Pregression -Ddog.api.maxResponseTimeMs=5000
```

## Relatório Allure

Gerar relatório:

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

Arquivos gerados:

- `target/allure-results`
- `target/site/allure-maven-plugin`
- `environment.properties`
- `executor.json`
- `categories.json`

## CI/CD com GitHub Actions

Workflow: `.github/workflows/api-tests.yml`

Fluxo:

1. Job de testes em matriz (`ubuntu`, `windows`, `macos`)
2. Upload de `surefire-reports` e `allure-results`
3. Job agregador para relatório Allure consolidado
4. Publicação no GitHub Pages
5. Preservação de histórico e snapshots por execução (`runs/<run_number>`)

Relatório publicado em:

- `https://alexxandrelopesqa.github.io/dog-api/`

## Jenkins

O repositório possui `Jenkinsfile` para pipeline declarativa com:

- checkout do código
- `clean test -Pregression -Ddog.api.maxResponseTimeMs=5000`
- `allure:report`
- publicação de artefatos:
  - `target/surefire-reports`
  - `target/allure-results`
  - `target/site/allure-maven-plugin`

Requisitos do agente Jenkins:

- JDK 17+
- acesso à internet (dependências Maven e Dog API)
- permissão de escrita no workspace (`target/`)

## Troubleshooting

- **Falha por latência da API pública**  
  Rode com: `-Ddog.api.maxResponseTimeMs=5000`

- **Falha na publicação do GitHub Pages**  
  Verifique `Settings > Pages > Source = GitHub Actions`

- **Aviso no passo de checkout do history (`gh-pages`)**  
  Pode ocorrer na primeira publicação; não invalida os testes da execução atual.

## Limitações conhecidas

- A Dog API é externa e pode oscilar em disponibilidade/latência.
- O tempo de resposta pode variar conforme rede/região do executor.

## Próximos passos sugeridos

- separar execução `smoke` para PR e `regression` para merge em `main`
- evoluir versionamento de contrato por endpoint
- adicionar badge/link direto para o último Allure report publicado
