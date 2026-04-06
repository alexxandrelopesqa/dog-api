# Dog API - QA API Automation

Projeto de testes automatizados para o desafio da [Dog API](https://dog.ceo/dog-api/documentation), com foco em validação de contrato, comportamento funcional e rastreabilidade de execução.

Base URL usada nos testes: `https://dog.ceo/api`

![CI](https://github.com/alexxandrelopesqa/dog-api/actions/workflows/api-tests.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-blue)

## O que foi coberto

Endpoints obrigatórios:

- `GET /breeds/list/all`
- `GET /breed/{breed}/images`
- `GET /breeds/image/random`

Validações implementadas:

- status HTTP esperado
- content type JSON
- chaves obrigatórias do payload (`status`, `message`)
- schema validation
- cenários positivos e negativos
- retry apenas para falhas transitórias (`429`, `5xx` e exceções de rede)

## Stack

- Java 17
- Maven + Maven Wrapper
- JUnit 5
- Rest Assured
- Allure Report
- GitHub Actions + GitHub Pages
- Docker (opcional)

## Pré-requisitos

- Java 17+
- Git
- acesso à internet para a API pública

## Como rodar localmente

### Suite completa

Windows:

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

### Teste isolado

```bash
./mvnw -Dtest=DogApiPositiveTests#shouldReturnValidRandomImage test
```

## Allure

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

Arquivos úteis:

- `target/allure-results`
- `target/site/allure-maven-plugin`
- `environment.properties`
- `executor.json`
- `categories.json`

## Como o pipeline funciona

Arquivo: `.github/workflows/api-tests.yml`

- job de testes em matriz (`ubuntu`, `windows`, `macos`)
- upload de artefatos (surefire + allure-results)
- job agregador para gerar Allure consolidado
- publicação no GitHub Pages
- histórico preservado para tendência
- snapshot por execução em `runs/<run_number>`

URL esperada do Pages:

- `https://alexxandrelopesqa.github.io/dog-api/`

## Jenkins

O repositório também possui `Jenkinsfile` para execução em pipeline declarativa.

Fluxo no Jenkins:

- checkout do repositório
- execução da suite `regression` com Java 17
- geração de relatório Allure em HTML (`allure:report`)
- publicação de:
  - `target/surefire-reports`
  - `target/allure-results`
  - `target/site/allure-maven-plugin`

Requisitos do agente Jenkins:

- JDK 17+
- acesso à internet para baixar dependências Maven e consumir a Dog API
- permissões de escrita no workspace (para `target/`)

## Estrutura

```text
src/test/java/
  core/
  client/
  models/
  tests/
src/test/resources/
  schemas/
  allure/
  testdata/
.github/workflows/
```

## Limitações

- Por ser API pública, pode ocorrer intermitência ocasional de rede/latência.
- Os retries foram limitados para não mascarar falha real de contrato/funcional.

## Próximos passos que eu faria

- separar smoke para PR e regression completo no merge para `main`
- incluir um teste de contrato versionado por endpoint
- adicionar badge do Allure latest na documentação
