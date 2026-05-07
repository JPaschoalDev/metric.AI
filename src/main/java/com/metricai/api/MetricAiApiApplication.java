package com.metricai.api;

import com.metricai.api.model.Venda;
import com.metricai.api.repository.VendaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.math.BigDecimal;

@SpringBootApplication
public class MetricAiApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricAiApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(VendaRepository repository) {
        return (args) -> {
            Venda v = new Venda();
            v.setExternalId("TESTE-123");
            v.setProdutoNome("Produto Teste Docker");
            v.setValor(new BigDecimal("100.00"));
            v.setStatus("PAGO");

            repository.save(v);
            System.out.println(">>> SUCESSO AO SALVAR NO DOCKER! <<<");
        };
    }
}