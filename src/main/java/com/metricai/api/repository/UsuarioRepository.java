package com.metricai.api.repository;

import com.metricai.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// REPOSITORY - Interface para acessar a tabela "usuarios" no banco de dados
// JpaRepository é uma interface do Spring Data JPA que implementa os métodos automaticamente

// @Repository marca essa interface como um componente de acesso a dados
@Repository

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
     // "Usuario": Entidade que este repository gerencia
     // "Long": Tipo do ID dessa entidade

    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}