// Define que esta classe pertence ao pacote "model"
package com.metricai.api.model;

import jakarta.persistence.*;     // Importa as anotações do JPA para o banco de dados
import lombok.Data;               // Importa a biblioteca que gera código repetitivo
import java.math.BigDecimal;      // Importa o tipo idela para lidar com dinheiro
import java.time.LocalDateTime;   // Importa o tipo para data e hora completa

// Cria uma tabela no banco baseada nesta classe "Venda"
@Entity
// Diz ao banco que o nome da tabela deve ser "vendas"
@Table(name = "vendas")
// Gera automaticamente os Getters, Setters, toString e Equals
@Data

// Classe que vai gerar os atributos (tabelas) do banco de dados
public class Venda {

    // Esse campo será a chave primária
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto incremental
    private Long id; // Indentificador único e interno

    @Column(nullable = false) // Garante que o banco não aceite uma venda sem esse iD externo
    private String externalId; // iD da venda no MELI

    private String produtoNome; // Nome do procuto

    private BigDecimal valor; // Valor da venda

    private LocalDateTime dataVenda; // Data e hora exata que a venda ocorreu

    private String status; // Status atual da venda

    @PrePersist // Antes de salvar no banco, executa esse metodo
    protected void onCreate() {
        if (this.dataVenda == null) { // Se o sistema não recebeu uma data do Meli
            this.dataVenda = LocalDateTime.now(); // Ele utiliza a hora exata por padrão
        }
    }
}
