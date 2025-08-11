package com.beergam

/**
 * Templates para geração de Docker Compose
 */
class ComposeTemplates {
    
    /**
     * Gera o docker-compose para um ambiente específico
     */
    static void generateDockerCompose(String environment, String dockerTag) {
        def script = new org.jenkinsci.plugins.workflow.cps.CpsScript()
        
        // Gerar docker-compose específico do ambiente
        def composeContent = getDockerCompose(environment, dockerTag)
        def composeFileName = getComposeFileName(environment)
        
        script.writeFile file: composeFileName, text: composeContent
        
        // Também criar o docker-compose.yml padrão para compatibilidade
        script.writeFile file: "docker-compose.yml", text: composeContent
    }
    
    /**
     * Retorna o docker-compose baseado no ambiente
     */
    static String getDockerCompose(String environment, String dockerTag) {
        switch(environment) {
            case "production":
                return getProductionCompose(dockerTag)
            case "staging":
                return getStagingCompose(dockerTag)
            default:
                return getDevelopmentCompose(dockerTag)
        }
    }
    
    /**
     * Gera o nome do arquivo docker-compose
     */
    static String getComposeFileName(String environment) {
        return "docker-compose.${environment}.yml"
    }
    
    // ============================================================================
    // TEMPLATES DE DOCKER COMPOSE PARA PRODUÇÃO
    // ============================================================================
    
    private static String getProductionCompose(String dockerTag) {
        return """

            services:
                beergam_core:
                    image: beergammaster/beergam_core:${dockerTag}
                    ports:
                    - "5000:5000"
                    environment:
                    - FLASK_ENV=production
                    networks:
                    - beergam_net
                    deploy:
                    replicas: 2
                    restart_policy:
                        condition: on-failure
                        delay: 5s
                        max_attempts: 3
                    secrets:
                    - mysql_password
                    - mysql_root_password

                socket_service:
                    image: beergammaster/socket_service:${dockerTag}
                    ports:
                    - "3000:3000"
                    networks:
                    - beergam_net
                    deploy:
                    replicas: 2
                    restart_policy:
                        condition: on-failure
                        delay: 5s
                        max_attempts: 3

                reports_service:
                    image: beergammaster/reports_service:${dockerTag}
                    ports:
                    - "5001:5000"
                    networks:
                    - beergam_net
                    deploy:
                    replicas: 1
                    restart_policy:
                        condition: on-failure
                        delay: 5s
                        max_attempts: 3

                catalog_service:
                    image: beergammaster/catalog_service:${dockerTag}
                    ports:
                    - "5002:5000"
                    networks:
                    - beergam_net
                    deploy:
                    replicas: 1
                    restart_policy:
                        condition: on-failure
                        delay: 5s
                        max_attempts: 3

                orders_service:
                    image: beergammaster/orders_service:${dockerTag}
                    ports:
                    - "5003:5000"
                    networks:
                    - beergam_net
                    deploy:
                    replicas: 1
                    restart_policy:
                        condition: on-failure
                        delay: 5s
                        max_attempts: 3

                listing_service:
                    image: beergammaster/listing_service:${dockerTag}
                    ports:
                    - "5004:5000"
                    networks:
                    - beergam_net
                    deploy:
                    replicas: 1
                    restart_policy:
                        condition: on-failure
                        delay: 5s
                        max_attempts: 3

                ml_service:
                    image: beergammaster/ml_service:${dockerTag}
                    ports:
                    - "5005:5000"
                    networks:
                    - beergam_net
                    deploy:
                    replicas: 1
                    restart_policy:
                        condition: on-failure
                        delay: 5s
                        max_attempts: 3

                mysql:
                    image: mysql:8.0.38
                    environment:
                    MYSQL_ROOT_PASSWORD_FILE: /run/secrets/mysql_root_password
                    MYSQL_DATABASE: beergam
                    MYSQL_USER: admin
                    MYSQL_PASSWORD_FILE: /run/secrets/mysql_password
                    networks:
                    - beergam_net
                    deploy:
                    replicas: 1
                    restart_policy:
                        condition: on-failure
                        delay: 5s
                        max_attempts: 3
                    secrets:
                    - mysql_password
                    - mysql_root_password

                redis:
                    image: redis:alpine
                    networks:
                    - beergam_net
                    deploy:
                    replicas: 1
                    restart_policy:
                        condition: on-failure
                        delay: 5s
                        max_attempts: 3

                rabbitmq:
                    image: rabbitmq:3-management
                    ports:
                    - "15672:15672"
                    networks:
                    - beergam_net
                    deploy:
                    replicas: 1
                    restart_policy:
                        condition: on-failure
                        delay: 5s
                        max_attempts: 3

                networks:
                beergam_net:
                    external: true

                secrets:
                mysql_password:
                    external: true
                mysql_root_password:
                    external: true
        """
    }
    
    // ============================================================================
    // TEMPLATES DE DOCKER COMPOSE PARA STAGING
    // ============================================================================
    
    private static String getStagingCompose(String dockerTag) {
        return """

            services:
                beergam_core:
                    image: beergammaster/beergam_core:${dockerTag}
                    ports:
                    - "5000:5000"
                    environment:
                    - FLASK_ENV=staging
                    restart: unless-stopped

                socket_service:
                    image: beergammaster/socket_service:${dockerTag}
                    ports:
                    - "3000:3000"
                    restart: unless-stopped

                reports_service:
                    image: beergammaster/reports_service:${dockerTag}
                    ports:
                    - "5001:5000"
                    restart: unless-stopped

                catalog_service:
                    image: beergammaster/catalog_service:${dockerTag}
                    ports:
                    - "5002:5000"
                    restart: unless-stopped

                orders_service:
                    image: beergammaster/orders_service:${dockerTag}
                    ports:
                    - "5003:5000"
                    restart: unless-stopped

                listing_service:
                    image: beergammaster/listing_service:${dockerTag}
                    ports:
                    - "5004:5000"
                    restart: unless-stopped

                ml_service:
                    image: beergammaster/ml_service:${dockerTag}
                    ports:
                    - "5005:5000"
                    restart: unless-stopped

                mysql:
                    image: mysql:8.0.38
                    environment:
                    MYSQL_ROOT_PASSWORD: staging_root_password
                    MYSQL_DATABASE: beergam_staging
                    MYSQL_USER: admin
                    MYSQL_PASSWORD: staging_password
                    restart: unless-stopped

                redis:
                    image: redis:alpine
                    restart: unless-stopped

                rabbitmq:
                    image: rabbitmq:3-management
                    ports:
                    - "15672:15672"
                    restart: unless-stopped
        """
    }
    
    // ============================================================================
    // TEMPLATES DE DOCKER COMPOSE PARA DESENVOLVIMENTO
    // ============================================================================
    
    private static String getDevelopmentCompose(String dockerTag) {
        return """
            services:
                beergam_core:
                    image: beergammaster/beergam_core:${dockerTag}
                    ports:
                    - "5000:5000"
                    environment:
                    - FLASK_ENV=development
                    - FLASK_DEBUG=1
                    volumes:
                    - .:/app
                    restart: always

                socket_service:
                    image: beergammaster/socket_service:${dockerTag}
                    ports:
                    - "3000:3000"
                    volumes:
                    - ./socket-service:/app
                    restart: always

                reports_service:
                    image: beergammaster/reports_service:${dockerTag}
                    ports:
                    - "5001:5000"
                    volumes:
                    - ./reports_service:/app
                    restart: always

                catalog_service:
                    image: beergammaster/catalog_service:${dockerTag}
                    ports:
                    - "5002:5000"
                    volumes:
                    - ./catalog_service:/app
                    restart: always

                orders_service:
                    image: beergammaster/orders_service:${dockerTag}
                    ports:
                    - "5003:5000"
                    volumes:
                    - ./orders_service:/app
                    restart: always

                listing_service:
                    image: beergammaster/listing_service:${dockerTag}
                    ports:
                    - "5004:5000"
                    volumes:
                    - ./listing_service:/app
                    restart: always

                ml_service:
                    image: beergammaster/ml_service:${dockerTag}
                    ports:
                    - "5005:5000"
                    volumes:
                    - ./ml_service:/app
                    restart: always

                mysql:
                    image: mysql:8.0.38
                    environment:
                    MYSQL_ROOT_PASSWORD: dev_root_password
                    MYSQL_DATABASE: beergam_dev
                    MYSQL_USER: admin
                    MYSQL_PASSWORD: dev_password
                    restart: always

                redis:
                    image: redis:alpine
                    restart: always

                rabbitmq:
                    image: rabbitmq:3-management
                    ports:
                    - "15672:15672"
                    restart: always
        """
    }
}