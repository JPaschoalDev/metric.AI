package com.metricai.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//     [==== Metric.AI =====]
// CLASSE PRINCIPAL DA APLICAÇÃO
// Esta classe é a porta de entrada do sistema, quando rodar a aplicação o Sping Boot se inicia aqui

// @SpringBootApplication é um conjunto de 3 requisições:
// 1. @Configuration: Diz que esta classe pode definir beans (componentes)
// 2. @EnableAutoConfiguration: Spring configura tudo automaticamente
// 3. @ComponentScan: Spring procura por controllers, services, repositories
@SpringBootApplication
public class MetricAiApiApplication {

    // METODO MAIN
    public static void main(String[] args) {
        // Inicia a aplicação Spring Boot

        // Inicia a aplicação SpringBoot
        SpringApplication.run(MetricAiApiApplication.class, args);
    }
}