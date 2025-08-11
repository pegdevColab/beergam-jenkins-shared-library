package com.beergam

/**
 * Utilitários para deploy em diferentes ambientes
 */
class DeployUtils {
    
    /**
     * Deploy para ambiente específico
     */
    static void deployToEnvironment(String environment, String dockerTag) {
        switch(environment) {
            case "production":
                deployToProduction()
                break
            case "staging":
                deployToStaging()
                break
            default:
                deployToDevelopment()
                break
        }
    }
    
    /**
     * Deploy para produção (MANTÉM LÓGICA ATUAL)
     */
    static void deployToProduction() {
        def script = new org.jenkinsci.plugins.workflow.cps.CpsScript()
        
        script.echo "Executando deploy para PRODUÇÃO..."
        
        script.sh """
            set -e
            set +x
            
            command -v sshpass >/dev/null || sudo apt-get install -y sshpass
            
            rm -f socket.env || true
            chmod -R u+rwX .
            
            tar czf projeto.tar.gz --exclude=projeto.tar.gz --exclude=.git .
            
            # Envio para o servidor
            sshpass -p ${VPS_PASSWORD} scp -o StrictHostKeyChecking=no projeto.tar.gz root@89.116.74.23:/home/Beergam_project_test/
            
            # Preparação remota
            sshpass -p ${VPS_PASSWORD} ssh -o StrictHostKeyChecking=no root@89.116.74.23 "
                set -e
                mkdir -p /home/Beergam_project_test
                if [ -d /home/Beergam_project_test/Beergam_project ]; then
                    rm -rf /home/Beergam_project_test/Beergam_project_backup
                    mv /home/Beergam_project_test/Beergam_project /home/Beergam_project_test/Beergam_project_backup
                fi
                mkdir -p /home/Beergam_project_test/Beergam_project
                tar xzf /home/Beergam_project_test/projeto.tar.gz -C /home/Beergam_project_test/Beergam_project
                rm -f /home/Beergam_project_test/projeto.tar.gz
            "
            
            # Rede + secrets
            sshpass -p ${VPS_PASSWORD} ssh -o StrictHostKeyChecking=no root@89.116.74.23 "
                docker network create beergam_net || true
                echo -n "${MYSQL_PASSWORD}" | docker secret create mysql_password - 2>/dev/null || true
                echo -n "${MYSQL_PASSWORD}" | docker secret create mysql_root_password - 2>/dev/null || true
            "
            
            # Pull de imagens
            sshpass -p ${VPS_PASSWORD} ssh -o StrictHostKeyChecking=no root@89.116.74.23 "
                docker pull beergammaster/beergam_core:latest &&
                docker pull beergammaster/socket_service:latest &&
                docker pull beergammaster/reports_service:latest &&
                docker pull beergammaster/catalog_service:latest &&
                docker pull beergammaster/orders_service:latest &&
                docker pull beergammaster/listing_service:latest &&
                docker pull beergammaster/ml_service:latest &&
                docker pull mysql:8.0.38 &&
                docker pull redis:alpine &&
                docker pull rabbitmq:3-management
            "
            
            # Deploy do stack
            sshpass -p ${VPS_PASSWORD} ssh -o StrictHostKeyChecking=no root@89.116.74.23 "
                docker stack deploy --with-registry-auth -c /home/Beergam_project_test/Beergam_project/docker-compose.yml Beergam-production
            "
            
            # Espera pelo MySQL no Swarm
            sshpass -p ${VPS_PASSWORD} ssh -o StrictHostKeyChecking=no root@89.116.74.23 "
                set -e
                echo "Aguardando MySQL (até 2 minutos)..."
                for i in $(seq 1 24); do
                  if docker run --rm --network beergam_net --entrypoint mysqladmin mysql:8.0.38 \\
                      ping -h mysql -uadmin -p$(cat /var/lib/docker/volumes/ | head -n1 2>/dev/null >/dev/null) --silent; then
                    echo "MySQL OK"
                    break
                  fi
                  if docker run --rm --network beergam_net --entrypoint mysqladmin mysql:8.0.38 \\
                      ping -h mysql -uadmin -p"$(cat /run/secrets/mysql_password 2>/dev/null || true)" --silent; then
                    echo "MySQL OK (via secret)"
                    break
                  fi
                  sleep 5
                  if [ $i -eq 24 ]; then echo "MySQL não respondeu a tempo"; exit 1; fi
                done
            "
            
            # Migrações usando container efêmero
            sshpass -p ${VPS_PASSWORD} ssh -o StrictHostKeyChecking=no root@89.116.74.23 "
                docker run --rm --network beergam_net \\
                  --env-file /home/Beergam_project_test/Beergam_project/.env \\
                  beergammaster/beergam_core:latest \\
                  flask db upgrade --directory app/migrations
            "
            
            echo "Deploy em produção finalizado com sucesso!"
            set -x
        """
    }
    
    /**
     * Deploy para staging (NOVO AMBIENTE)
     */
    static void deployToStaging() {
        def script = new org.jenkinsci.plugins.workflow.cps.CpsScript()
        
        script.echo "Executando deploy para STAGING..."
        
        // TODO: Implementar lógica específica para staging
        // Por enquanto, apenas exibe o docker-compose gerado
        script.sh """
            echo "========================================"
            echo "Docker Compose para STAGING:"
            echo "========================================"
            cat docker-compose.staging.yml
            echo "========================================"
            echo "Fim do docker-compose.staging.yml"
            echo "========================================"
            echo "Deploy para staging será implementado em breve!"
        """
    }
    
    /**
     * Deploy para desenvolvimento (MANTÉM LÓGICA ATUAL)
     */
    static void deployToDevelopment() {
        def script = new org.jenkinsci.plugins.workflow.cps.CpsScript()
        
        script.echo "Executando deploy para DESENVOLVIMENTO..."
        
        script.sh """
            echo "========================================"
            echo "Docker Compose para ambiente de desenvolvimento:"
            echo "========================================"
            cat docker-compose.dev.yml
            echo "========================================"
            echo "Fim do docker-compose.dev.yml"
            echo "========================================"
            echo "Para executar: docker-compose -f docker-compose.dev.yml up -d"
        """
    }
    
    /**
     * Função de limpeza
     */
    static void cleanup() {
        def script = new org.jenkinsci.plugins.workflow.cps.CpsScript()
        script.sh "docker logout"
        script.sh "docker image prune -f"
    }
    
    /**
     * Notificação de sucesso
     */
    static void notifySuccess(String environment, String branchName) {
        def script = new org.jenkinsci.plugins.workflow.cps.CpsScript()
        script.echo "Pipeline executado com sucesso!"
        script.echo "Ambiente: ${environment}"
        script.echo "Branch: ${branchName}"
    }
    
    /**
     * Notificação de falha
     */
    static void notifyFailure(String environment, String branchName) {
        def script = new org.jenkinsci.plugins.workflow.cps.CpsScript()
        script.echo "Pipeline falhou!"
        script.echo "Ambiente: ${environment}"
        script.echo "Branch: ${branchName}"
    }
    
    /**
     * Rollback de produção
     */
    static void rollbackProduction() {
        def script = new org.jenkinsci.plugins.workflow.cps.CpsScript()
        
        script.echo "Executando rollback de produção..."
        
        script.sh """
            set -e
            set +x
            sshpass -p ${VPS_PASSWORD} ssh -o StrictHostKeyChecking=no root@89.116.74.23 "
                if [ -d /home/Beergam_project_test/Beergam_project_backup ]; then
                    rm -rf /home/Beergam_project_test/Beergam_project
                    mv /home/Beergam_project_test/Beergam_project_backup /home/Beergam_project_test/Beergam_project
                    echo "Backup restaurado com sucesso!"
                    docker stack deploy --with-registry-auth -c /home/Beergam_project_test/Beergam_project/docker-compose.yml Beergam-production
                else
                    echo "Nenhum backup encontrado para restaurar."
                fi
            "
            set -x
        """
    }
}