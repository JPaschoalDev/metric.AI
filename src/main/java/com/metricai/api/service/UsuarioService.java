package com.metricai.api.service;

// Importa as ferramentass necessárias para implementação da lógica
import com.metricai.api.dto.RegisterRequest;
import com.metricai.api.model.Usuario;
import com.metricai.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// SERVICE
// O cérebro da operação, fica entre o Controller (que recebe requisições HTTP) e o Repository (que acessa o banco)

// RESPONSABILIDADES
// - Validar regras de négocio (email duplicado, senha forte, etc)
// - Criptografar senhas
// - Converter DTO´s em Entidades e vice-versa
// - Chamar o Repository para salvar/buscar dados

// FLUXO
// Frontend -> Controller -> Service -> Repository -> Banco de Dados

// @service = Marca a classe como um componente de serviço do SpringBoot
// O spring cria uma instância dela e injeta onde for necessário
@Service

public class UsuarioService {
    // INJEÇÃO DE DEPENDÊNCIAS
    // @Autowired: o Spring automaticamente injeta esse objetos
    // Sem necessidade de usar: "new UsuarioRepository()" ou "new PasswordEncoder()"
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // METODO "cadastrar" = CADASTRAR NOVO USUÁRIO
    // Esse metodo recebe os dados de cadastro frontend, valida, criptografa a senha e salva no banco.

    // FLUXO
    // 1. Verifica se o email já existe (evita duplicidade)
    // 2. Cria uma nova entidade "Usuario"
    // 3. Criptografar a senha usando ByCrypt
    // 4. Salvar no banco de dados
    // 5. Retorna o usuário salvo (com iD gerado)

    // @param request - Dados do formulário de cadastro (nome, email, senha)
    // @return Usuario - Usuário criado com ID do banco
    // @throws RuntimeException - Se o email já estiver cadastrado
    public Usuario cadastrar(RegisterRequest request) {

        // 1: VERIFICAR SE O EMAIL JÁ EXISTE
        // existsByEmail retorna true se encontrar, false se não encontrar
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            // Lança uma exceção (erro) que vai ser capturada pelo Controller
            throw new RuntimeException("Email já cadastrado!");
        }

        // 2: CRIAR A NOVA ENTIDADE "Usuario"
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.getNome());
        novoUsuario.setEmail(request.getEmail());

        // 3: CRIPTOGRAFAR A SENHA
        String senhaCriptografada = passwordEncoder.encode(request.getSenha());
        novoUsuario.setSenha(senhaCriptografada);

        // 4: SALVAR NO BANCO DE DADOS
        // O metodo "save()" do JpaRepository:
        // Caso o iD for null: INSERT (Cria novo registro)
        // Caso o iD existir: UPDATE (Atualiza regitro)
        // Retorna a entidade salva (com o iD preenchido pelo banco)
        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        // 5: Retornar o usuário criado
        // O Controller vai usar esse objeto para gerar o token JWT
        return usuarioSalvo;
    }

    // METODO "autenticar" = FAZER LOGIN (AUTENTICAR USUÁRIO)
    // Verifica se o email existe e se a senha está correta

    // FLUXO:
    // 1. Busca usuário pelo email
    // 2. Se não encontrar: erro "Email não encontrado"
    // 3. Se encontrar: comparar a senha digitada com a senha criptografada do banco
    // 4. Se a senha bater: retornar o usuário (Login bem-sucedido)
    // 5. Se a senha não bater: retorna erro (Senha incorreta)

    // @param email - Email digitado no login
    // @param senha - Senha digitada no login (texto puro)
    // @return Usuario - Usuário autenticado
    // @throws RuntimeException - Se email não existir ou senha estiver errada
    public Usuario autenticar(String email, String senha) {

        // 1: BUSCAR USUÁRIO PELO EMAIL
        // "findByEmail" retorna Optional<Usuario>
        // "Optional" é um container que pode estar vazio ou conter um usuário
        Usuario usuario = usuarioRepository.findByEmail(email)
                // Se o "Optional" estiver vazio (email não encontrado), lança erro
                .orElseThrow(() -> new RuntimeException("Email não encontrado!"));

        // 2: VERIFICAR A SENHA
        // passwordEncoder.matches() compara:
        // 1º parâmetro: senha digitada (texto puro)
        // 2º parâmetro: senha criptografada do banco
        boolean senhaCorreta = passwordEncoder.matches(senha, usuario.getSenha());

        if (!senhaCorreta) {
            // Senha incorreta: lança erro
            throw new RuntimeException("Senha incorreta!");
        }

        // 3: SENHA CORRETA -> RETORNA USUÁRIO AUTENTICADO
        // O Controller vai usar esse usuário para gerar o token JWT
        return usuario;
    }

    // METODO "bucarPorEmail" = BUSCA USUÁRIO PELO EMAIL CADASTRADO
    // Útil para auxiliar na buscar de usuários e validações

    // @param email - Email a ser buscado
    // @return Usuario ou null se não encontrar
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    // METODO "buscarPorId" = BUSCA USUÁRIO PELO ID CADASTRADO
    // Útil para auxiliar na busca onde há necessidade do usuário completo

    // @param id - ID do usuário
    // @return Usuario ou null se não encontrar
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
}