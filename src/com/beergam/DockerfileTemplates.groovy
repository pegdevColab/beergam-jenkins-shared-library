package com.beergam

/**
 * Templates para geração de Dockerfiles
 */
class DockerfileTemplates {
    
    /**
     * Cria todos os Dockerfiles para um ambiente
     */
    static void createAllDockerfiles(String environment) {
        def script = new org.jenkinsci.plugins.workflow.cps.CpsScript()
        
        // Dockerfile do core
        def coreDockerfile = getCoreDockerfile(environment)
        script.writeFile file: "Dockerfile.core.${environment}", text: coreDockerfile
        
        // Dockerfile do socket service
        def socketDockerfile = getSocketServiceDockerfile(environment)
        script.writeFile file: "Dockerfile.socket.${environment}", text: socketDockerfile
        
        // Dockerfiles dos microserviços
        def services = getAvailableServices()
        services.each { service ->
            def microserviceDockerfile = getMicroserviceDockerfile(environment, service)
            def fileName = getDockerfileName(service, environment)
            script.writeFile file: fileName, text: microserviceDockerfile
        }
    }
    
    /**
     * Retorna o Dockerfile para o core baseado no ambiente
     */
    static String getCoreDockerfile(String environment) {
        switch(environment) {
            case 'production':
                return getProductionCoreDockerfile()
            case 'staging':
                return getStagingCoreDockerfile()
            default:
                return getDevelopmentCoreDockerfile()
        }
    }
    
    /**
     * Retorna o Dockerfile para microserviços baseado no ambiente
     */
    static String getMicroserviceDockerfile(String environment, String serviceName) {
        switch(environment) {
            case 'production':
                return getProductionMicroserviceDockerfile(serviceName)
            case 'staging':
                return getStagingMicroserviceDockerfile(serviceName)
            default:
                return getDevelopmentMicroserviceDockerfile(serviceName)
        }
    }
    
    /**
     * Retorna o Dockerfile para o socket service baseado no ambiente
     */
    static String getSocketServiceDockerfile(String environment) {
        switch(environment) {
            case 'production':
                return getProductionSocketServiceDockerfile()
            case 'staging':
                return getStagingSocketServiceDockerfile()
            default:
                return getDevelopmentSocketServiceDockerfile()
        }
    }
    
    /**
     * Lista todos os serviços disponíveis
     */
    static List getAvailableServices() {
        return ['reports_service', 'catalog_service', 'orders_service', 'listing_service', 'ml_service']
    }
    
    /**
     * Gera o nome do arquivo Dockerfile
     */
    static String getDockerfileName(String serviceName, String environment) {
        return "Dockerfile.${serviceName}.${environment}"
    }
    
    // ============================================================================
    // TEMPLATES DE DOCKERFILE PARA PRODUÇÃO
    // ============================================================================
    
    private static String getProductionCoreDockerfile() {
        return '''
            # Dockerfile para produção - Core
            FROM python:3.11-slim as base

            # Instalar dependências do sistema
            RUN apt-get update && apt-get install -y \\
                gcc \\
                g++ \\
                libffi-dev \\
                libssl-dev \\
                && rm -rf /var/lib/apt/lists/*

            # Configurar diretório de trabalho
            WORKDIR /app

            # Copiar requirements
            COPY requirements.txt .

            # Instalar dependências Python
            RUN pip install --no-cache-dir -r requirements.txt

            # Copiar código da aplicação
            COPY . .

            # Expor porta
            EXPOSE 5000

            # Comando de inicialização
            CMD ["python", "-m", "flask", "run", "--host=0.0.0.0", "--port=5000"]
        '''
    }
    
    private static String getProductionMicroserviceDockerfile(String serviceName) {
        if (serviceName == 'reports_service') {
            return '''
                # Dockerfile para reports_service em produção
                FROM python:3.11-slim

                WORKDIR /app

                COPY requirements.txt .
                RUN pip install --no-cache-dir -r requirements.txt

                COPY . .

                EXPOSE 5000
                CMD ["python", "app.py"]
            '''
        } else {
            return '''
                # Dockerfile para ${serviceName} em produção
                FROM python:3.11-slim

                WORKDIR /app

                COPY requirements.txt .
                RUN pip install --no-cache-dir -r requirements.txt

                COPY . .

                EXPOSE 5000
                CMD ["python", "app.py"]
            '''
        }
    }
    
    private static String getProductionSocketServiceDockerfile() {
        return '''
            # Dockerfile para socket service em produção
            FROM node:18-alpine

            WORKDIR /app

            COPY package*.json ./
            RUN npm ci --only=production

            COPY . .

            EXPOSE 3000
            CMD ["npm", "start"]
        '''
    }
    
    // ============================================================================
    // TEMPLATES DE DOCKERFILE PARA STAGING
    // ============================================================================
    
    private static String getStagingCoreDockerfile() {
        return '''
            # Dockerfile para staging - Core
            FROM python:3.11-slim

            WORKDIR /app

            COPY requirements.txt .
            RUN pip install --no-cache-dir -r requirements.txt

            COPY . .

            EXPOSE 5000
            CMD ["python", "-m", "flask", "run", "--host=0.0.0.0", "--port=5000"]
        '''
    }
    
    private static String getStagingMicroserviceDockerfile(String serviceName) {
        if (serviceName == 'reports_service') {
            return '''
                # Dockerfile para reports_service em staging
                FROM python:3.11-slim

                WORKDIR /app

                COPY requirements.txt .
                RUN pip install --no-cache-dir -r requirements.txt

                COPY . .

                EXPOSE 5000
                CMD ["python", "app.py"]
            '''
        } else {
            return '''
                # Dockerfile para ${serviceName} em staging
                FROM python:3.11-slim

                WORKDIR /app

                COPY requirements.txt .
                RUN pip install --no-cache-dir -r requirements.txt

                COPY . .

                EXPOSE 5000
                CMD ["python", "app.py"]
            '''
        }
    }
    
    private static String getStagingSocketServiceDockerfile() {
        return '''
            # Dockerfile para socket service em staging
            FROM node:18-alpine

            WORKDIR /app

            COPY package*.json ./
            RUN npm ci

            COPY . .

            EXPOSE 3000
            CMD ["npm", "start"]
        '''
    }
    
    // ============================================================================
    // TEMPLATES DE DOCKERFILE PARA DESENVOLVIMENTO
    // ============================================================================
    
    private static String getDevelopmentCoreDockerfile() {
        return '''
            # Dockerfile para desenvolvimento - Core
            FROM python:3.11-slim

            WORKDIR /app

            COPY requirements.txt .
            RUN pip install --no-cache-dir -r requirements.txt

            COPY . .

            EXPOSE 5000
            CMD ["python", "-m", "flask", "run", "--host=0.0.0.0", "--port=5000", "--debug"]
        '''
    }
    
    private static String getDevelopmentMicroserviceDockerfile(String serviceName) {
        if (serviceName == 'reports_service') {
            return '''
                # Dockerfile para reports_service em desenvolvimento
                FROM python:3.11-slim

                WORKDIR /app

                COPY requirements.txt .
                RUN pip install --no-cache-dir -r requirements.txt

                COPY . .

                EXPOSE 5000
                CMD ["python", "app.py"]
            '''
        } else {
            return '''
                # Dockerfile para ${serviceName} em desenvolvimento
                FROM python:3.11-slim

                WORKDIR /app

                COPY requirements.txt .
                RUN pip install --no-cache-dir -r requirements.txt

                COPY . .

                EXPOSE 5000
                CMD ["python", "app.py"]
            '''
        }
    }
    
    private static String getDevelopmentSocketServiceDockerfile() {
        return '''
            # Dockerfile para socket service em desenvolvimento
            FROM node:18-alpine

            WORKDIR /app

            COPY package*.json ./
            RUN npm install

            COPY . .

            EXPOSE 3000
            CMD ["npm", "run", "dev"]
        '''
    }
}