#!/usr/bin/env groovy

/**
 * Função global para pipeline simplificado
 * Uso: beergamPipeline(environment: 'auto-detect')
 */
def call(Map config = [:]) {
    def environment = config.environment ?: 'auto-detect'
    def dockerTag = config.dockerTag ?: 'auto'
    def deployStrategy = config.deployStrategy ?: 'auto'
    
    echo "Iniciando pipeline Beergam com configuração: ${config}"
    
    // Aqui você pode chamar o pipeline principal
    // Por enquanto, apenas exibe as configurações
    echo "Ambiente: ${environment}"
    echo "Docker Tag: ${dockerTag}"
    echo "Deploy Strategy: ${deployStrategy}"
}