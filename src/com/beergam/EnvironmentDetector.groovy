package com.beergam

/**
 * Detecta automaticamente o ambiente baseado na branch
 */
class EnvironmentDetector {
    
    /**
     * Detecta o ambiente baseado na branch
     */
    static Map detectEnvironment(String branchName) {
        def config = [:]
        
        switch(branchName) {
            case "main":
                config = [
                    environment: "production",
                    docker_tag: "latest",
                    deploy_target: "vps-swarm",
                    build_strategy: "optimized",
                    test_level: "smoke",
                    security_scan: true,
                    performance_test: false,
                    auto_deploy: false,
                    requires_approval: true,
                    health_checks: true,
                    secrets_enabled: true,
                    monitoring: "full",
                    restart_policy: "unless-stopped",
                    volumes_enabled: false,
                    hot_reload: false
                ]
                break
                
            case "staging":
                config = [
                    environment: "staging",
                    docker_tag: "staging",
                    deploy_target: "staging-server",
                    build_strategy: "balanced",
                    test_level: "integration",
                    security_scan: true,
                    performance_test: true,
                    auto_deploy: true,
                    requires_approval: false,
                    health_checks: true,
                    secrets_enabled: false,
                    monitoring: "basic",
                    restart_policy: "on-failure",
                    volumes_enabled: false,
                    hot_reload: false
                ]
                break
                
            case "dev":
            case "development":
                config = [
                    environment: "development",
                    docker_tag: "dev",
                    deploy_target: "local",
                    build_strategy: "fast",
                    test_level: "unit",
                    security_scan: false,
                    performance_test: false,
                    auto_deploy: true,
                    requires_approval: false,
                    health_checks: false,
                    secrets_enabled: false,
                    monitoring: "minimal",
                    restart_policy: "always",
                    volumes_enabled: true,
                    hot_reload: true
                ]
                break
                
            default:
                // Para feature branches, hotfix, etc.
                if (branchName.startsWith("feature-") || branchName.startsWith("hotfix-") || branchName.startsWith("bugfix-")) {
                    config = [
                        environment: "development",
                        docker_tag: "dev",
                        deploy_target: "local",
                        build_strategy: "fast",
                        test_level: "unit",
                        security_scan: false,
                        performance_test: false,
                        auto_deploy: true,
                        requires_approval: false,
                        health_checks: false,
                        secrets_enabled: false,
                        monitoring: "minimal",
                        restart_policy: "always",
                        volumes_enabled: true,
                        hot_reload: true
                    ]
                } else {
                    config = [
                        environment: "development",
                        docker_tag: "dev",
                        deploy_target: "local",
                        build_strategy: "fast",
                        test_level: "unit",
                        security_scan: false,
                        performance_test: false,
                        auto_deploy: true,
                        requires_approval: false,
                        health_checks: false,
                        secrets_enabled: false,
                        monitoring: "minimal",
                        restart_policy: "always",
                        volumes_enabled: true,
                        hot_reload: true
                    ]
                }
                break
        }
        
        return config
    }
    
    /**
     * Valida se a configuração está completa
     */
    static Boolean validateConfig(Map config) {
        def requiredFields = [
            "environment", "docker_tag", "deploy_target", "build_strategy",
            "test_level", "security_scan", "performance_test", "auto_deploy",
            "requires_approval", "health_checks", "secrets_enabled",
            "monitoring", "restart_policy", "volumes_enabled", "hot_reload"
        ]
        
        return requiredFields.every { field -> config.containsKey(field) }
    }
}