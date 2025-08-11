# Beergam Shared Library

Biblioteca compartilhada para pipelines Jenkins do projeto Beergam.

## Estrutura

- `src/com/beergam/EnvironmentDetector.groovy` - Detecção automática de ambiente
- `src/com/beergam/DockerfileTemplates.groovy` - Templates de Dockerfile
- `src/com/beergam/ComposeTemplates.groovy` - Templates de Docker Compose
- `src/com/beergam/DeployUtils.groovy` - Utilitários de deploy

## Uso

```groovy
@Library('beergam-shared-library') _

// Detectar ambiente
def config = EnvironmentDetector.detectEnvironment('main')

// Criar Dockerfiles
DockerfileTemplates.createAllDockerfiles('production')

// Gerar docker-compose
ComposeTemplates.generateDockerCompose('production', 'latest')

// Deploy
DeployUtils.deployToEnvironment('production', 'latest')
```

## Configuração no Jenkins

1. Vá em **Manage Jenkins** → **Global Pipeline Libraries**
2. Clique em **Add**
3. Configure:
   - **Name:** `beergam-shared-library`
   - **Source Code Management:** Git
   - **Project Repository:** `https://github.com/Pegdev-master/beergam-jenkins-shared-library.git`