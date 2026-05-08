package com.metricai.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CLASSE PRINCIPAL DA APLICAÇÃO METRIC.AI
 *
 * Esta é a porta de entrada do sistema!
 * Quando você roda a aplicação, o Spring Boot começa por aqui.
 *
 * @SpringBootApplication é uma anotação "3 em 1":
 * 1. @Configuration: Diz que esta classe pode definir beans (componentes)
 * 2. @EnableAutoConfiguration: Spring configura tudo automaticamente
 * 3. @ComponentScan: Spring procura por controllers, services, repositories
 *
 * Resultado: Com APENAS esta anotação, o Spring Boot:
 * - Configura o servidor web (Tomcat na porta 8080)
 * - Conecta no banco de dados (PostgreSQL)
 * - Escaneia e registra todos os @Controller, @Service, @Repository
 * - Configura o Hibernate/JPA
 * - E muito mais!
 */
@SpringBootApplication
public class MetricAiApiApplication {

    /**
     * MÉTODO MAIN - Ponto de entrada da aplicação Java
     *
     * Quando você clica no botão Play do IntelliJ, este método é executado.
     * Ele inicia todo o Spring Boot.
     *
     * @param args - Argumentos da linha de comando (raramente usado)
     */
    public static void main(String[] args) {
        // Inicia a aplicação Spring Boot
        // Este método faz TODA a mágica acontecer!
        SpringApplication.run(MetricAiApiApplication.class, args);
    }

    /**
     * REMOVEMOS O @Bean CommandLineRunner
     *
     * Aquele código era só um TESTE para verificar se o Docker estava funcionando.
     * Agora vamos focar no sistema de USUÁRIOS, então não precisamos mais dele!
     */
}