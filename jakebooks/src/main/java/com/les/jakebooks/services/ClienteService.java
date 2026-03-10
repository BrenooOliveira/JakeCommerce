package com.les.jakebooks.services;

import com.les.jakebooks.domain.Cartao;
import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.domain.Endereco;
import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.dto.*;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.model.enums.BandeiraCartao;
import com.les.jakebooks.model.enums.StatusCliente;
import com.les.jakebooks.model.enums.TipoEndereco;
import com.les.jakebooks.repository.CartaoRepository;
import com.les.jakebooks.repository.ClienteRepository;
import com.les.jakebooks.repository.EnderecoRepository;
import com.les.jakebooks.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service para gerenciar operações de Cliente.
 * RN0021: Pelo menos um endereço de cobrança é obrigatório.
 * RN0022: Pelo menos um endereço de entrega é obrigatório.
 * RN0024: Campos obrigatórios do cartão.
 * RN0025: Bandeira deve estar cadastrada.
 * RN0026: Dados obrigatórios do cliente.
 * RN0027: Cliente possui ranking numérico.
 * RNF0012: Senha criptografada e forte (mínimo 8 caracteres, maiúsculas, minúsculas e especiais).
 */
@Service
@Transactional
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Padrão de validação de senha forte
    private static final String SENHA_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final Pattern PATTERN_SENHA = Pattern.compile(SENHA_PATTERN);

    /**
     * Cadastra um novo cliente.
     * RF0021: Cadastrar cliente
     * RN0021: Pelo menos um endereço de cobrança é obrigatório
     * RN0022: Pelo menos um endereço de entrega é obrigatório
     * RN0026: Dados obrigatórios do cliente
     * RNF0012: Senha criptografada e forte
     *
     * @param dto DTO com dados do cliente
     * @return DTO do cliente criado
     * @throws ValidacaoNegocioException se validações falharem
     */
    public ClienteDetalheDTO cadastrar(ClienteCadastroDTO dto) {
        // Validar se já existe cliente com CPF
        Optional<Cliente> clienteExistente = clienteRepository.findByCpf(dto.cpf());
        if (clienteExistente.isPresent()) {
            throw new ValidacaoNegocioException("Já existe um cliente cadastrado com este CPF: " + dto.cpf());
        }

        // Validar se já existe cliente com email
        clienteExistente = clienteRepository.findByEmail(dto.email());
        if (clienteExistente.isPresent()) {
            throw new ValidacaoNegocioException("Já existe um cliente cadastrado com este email: " + dto.email());
        }

        // Validar senha forte
        validarSenhaForte(dto.senha());

        // Validar confirmação de senha
        if (!dto.senha().equals(dto.confirmacaoSenha())) {
            throw new ValidacaoNegocioException("A senha e confirmação de senha não conferem");
        }

        // Criar cliente
        Cliente cliente = new Cliente();
        cliente.setCodigo(gerarCodigoUnico());
        cliente.setNome(dto.nome());
        cliente.setGenero(dto.genero());
        cliente.setDataNascimento(dto.dataNascimento());
        cliente.setCpf(dto.cpf());
        cliente.setTelefone(dto.telefone());
        cliente.setEmail(dto.email());

        // Criptografar senha com BCrypt
        String senhaCriptografada = passwordEncoder.encode(dto.senha());
        cliente.setSenhaCriptografada(senhaCriptografada);

        // Inicializar ranking
        cliente.setRanking(0.0);

        // Status inicial: ATIVO
        cliente.setStatus(StatusCliente.ATIVO);

        Cliente clienteSalvo = clienteRepository.save(cliente);

        return converterParaDetalheDTO(clienteSalvo);
    }

    /**
     * Altera dados de um cliente existente.
     * RF0022: Alterar cliente
     *
     * @param codigo código único do cliente
     * @param dto novos dados do cliente
     * @return DTO do cliente alterado
     * @throws RecursoNaoEncontradoException se cliente não existe
     */
    public ClienteDetalheDTO alterar(String codigo, ClienteAlteracaoDTO dto) {
        Cliente cliente = buscarClientePorCodigo(codigo);

        // Validar se email já está em uso por outro cliente
        Optional<Cliente> clienteComEmail = clienteRepository.findByEmail(dto.email());
        if (clienteComEmail.isPresent() && !clienteComEmail.get().getId().equals(cliente.getId())) {
            throw new ValidacaoNegocioException("Email já está em uso por outro cliente: " + dto.email());
        }

        // Atualizar campos
        cliente.setNome(dto.nome());
        cliente.setGenero(dto.genero());
        cliente.setDataNascimento(dto.dataNascimento());
        cliente.setTelefone(dto.telefone());
        cliente.setEmail(dto.email());
        cliente.setStatus(dto.status());

        Cliente clienteAtualizado = clienteRepository.save(cliente);

        return converterParaDetalheDTO(clienteAtualizado);
    }

    /**
     * Inativa um cliente.
     * RF0023: Inativar cliente
     *
     * @param codigo código único do cliente
     * @return DTO do cliente inativado
     * @throws RecursoNaoEncontradoException se cliente não existe
     */
    public ClienteDetalheDTO inativar(String codigo) {
        Cliente cliente = buscarClientePorCodigo(codigo);

        if (cliente.getStatus() == StatusCliente.INATIVO) {
            throw new ValidacaoNegocioException("Cliente já está inativo");
        }

        cliente.setStatus(StatusCliente.INATIVO);
        Cliente clienteAtualizado = clienteRepository.save(cliente);

        return converterParaDetalheDTO(clienteAtualizado);
    }

    /**
     * Altera a senha do cliente.
     * RF0028: Alterar apenas senha
     * RNF0012: Senha criptografada e forte
     *
     * @param codigo código único do cliente
     * @param dto dados de alteração de senha
     * @return DTO do cliente (sem alterações nos dados, só na senha)
     * @throws RecursoNaoEncontradoException se cliente não existe
     * @throws ValidacaoNegocioException se senhas não conferem ou senha atual está incorreta
     */
    public ClienteDetalheDTO alterarSenha(String codigo, AlteraSenhaDTO dto) {
        Cliente cliente = buscarClientePorCodigo(codigo);

        // Validar senha atual
        if (!passwordEncoder.matches(dto.senhaAtual(), cliente.getSenhaCriptografada())) {
            throw new ValidacaoNegocioException("Senha atual está incorreta");
        }

        // Validar senha nova forte
        validarSenhaForte(dto.novaSenha());

        // Validar confirmação de senha
        if (!dto.novaSenha().equals(dto.confirmacaoNovaSenha())) {
            throw new ValidacaoNegocioException("A nova senha e confirmação não conferem");
        }

        // Validar se nova senha é diferente da atual
        if (passwordEncoder.matches(dto.novaSenha(), cliente.getSenhaCriptografada())) {
            throw new ValidacaoNegocioException("A nova senha deve ser diferente da senha atual");
        }

        // Criptografar e atualizar
        String novaSenhaCriptografada = passwordEncoder.encode(dto.novaSenha());
        cliente.setSenhaCriptografada(novaSenhaCriptografada);

        Cliente clienteAtualizado = clienteRepository.save(cliente);

        return converterParaDetalheDTO(clienteAtualizado);
    }

    /**
     * Adiciona um novo endereço ao cliente.
     * RF0026: Cadastrar múltiplos endereços
     * RN0023: Campos obrigatórios do endereço
     * RN0021: Pelo menos um endereço de cobrança é obrigatório
     * RN0022: Pelo menos um endereço de entrega é obrigatório
     *
     * @param codigo código único do cliente
     * @param dto dados do endereço
     * @return DTO do cliente atualizado
     * @throws RecursoNaoEncontradoException se cliente não existe
     */
    public ClienteDetalheDTO adicionarEndereco(String codigo, EnderecoDTO dto) {
        Cliente cliente = buscarClientePorCodigo(codigo);

        // Validar se já existe endereço com mesmo nome identificador
        Endereco enderecoExistente = enderecoRepository.findByClienteIdAndNomeIdentificadorAndTipoEndereco(
                cliente.getId(), dto.nomeIdentificador(), dto.tipoEndereco());
        if (enderecoExistente != null) {
            throw new ValidacaoNegocioException("Já existe um endereço com este identificador: " + dto.nomeIdentificador());
        }

        // Criar endereco
        Endereco endereco = new Endereco();
        endereco.setNomeIdentificador(dto.nomeIdentificador());
        endereco.setTipoResidencia(dto.tipoResidencia());
        endereco.setLogradouro(dto.logradouro());
        endereco.setNumero(dto.numero());
        endereco.setBairro(dto.bairro());
        endereco.setCep(dto.cep());
        endereco.setCidade(dto.cidade());
        endereco.setEstado(dto.estado());
        endereco.setPais(dto.pais());
        endereco.setTipoEndereco(dto.tipoEndereco());
        endereco.setCliente(cliente);

        enderecoRepository.save(endereco);
        cliente.getEnderecos().add(endereco);

        return converterParaDetalheDTO(cliente);
    }

    /**
     * Adiciona um novo cartão ao cliente.
     * RF0027: Cadastrar múltiplos cartões (um preferencial)
     * RN0024: Campos obrigatórios do cartão
     * RN0025: Bandeira deve estar cadastrada
     * RN0034: Múltiplos cartões permitidos (mínimo R$ 10 por transação)
     *
     * @param codigo código único do cliente
     * @param dto dados do cartão
     * @return DTO do cliente atualizado
     * @throws RecursoNaoEncontradoException se cliente não existe
     * @throws ValidacaoNegocioException se bandeira não existir ou número duplicado
     */
    public ClienteDetalheDTO adicionarCartao(String codigo, CartaoDTO dto) {
        Cliente cliente = buscarClientePorCodigo(codigo);

        // Validar se bandeira está cadastrada (RN0025)
        try {
            BandeiraCartao.valueOf(dto.bandeira().name());
        } catch (IllegalArgumentException e) {
            throw new ValidacaoNegocioException("Bandeira de cartão não cadastrada: " + dto.bandeira());
        }

        // Validar se já existe cartão com mesmo número neste cliente
        Optional<Cartao> cartaoExistente = cartaoRepository.findByClienteIdAndNumero(cliente.getId(), dto.numero());
        if (cartaoExistente.isPresent()) {
            throw new ValidacaoNegocioException("Você já possui um cartão com este número cadastrado");
        }

        // Se novo cartão é preferencial, remover preferencial de outros
        if (dto.preferencial()) {
            List<Cartao> cartoesAtuais = cartaoRepository.findByClienteId(cliente.getId());
            for (Cartao cartao : cartoesAtuais) {
                if (cartao.getPreferencial()) {
                    cartao.setPreferencial(false);
                    cartaoRepository.save(cartao);
                }
            }
        }

        // Criar cartão
        Cartao cartao = new Cartao();
        cartao.setNumero(dto.numero());
        cartao.setNomeImpresso(dto.nomeImpresso());
        cartao.setBandeira(dto.bandeira());
        cartao.setCodigoSeguranca(dto.codigoSeguranca());
        cartao.setPreferencial(dto.preferencial());
        cartao.setCliente(cliente);

        cartaoRepository.save(cartao);
        cliente.getCartoes().add(cartao);

        return converterParaDetalheDTO(cliente);
    }

    /**
     * Busca um cliente pelo código e retorna dados completos.
     * RF0024: Consultar cliente
     *
     * @param codigo código único do cliente
     * @return DTO com detalhes completos do cliente
     * @throws RecursoNaoEncontradoException se cliente não existe
     */
    public ClienteDetalheDTO buscarPorCodigo(String codigo) {
        Cliente cliente = buscarClientePorCodigo(codigo);
        return converterParaDetalheDTO(cliente);
    }

    /**
     * Recupera as transações (pedidos) do cliente.
     * RF0025: Consultar transações do cliente
     *
     * @param codigo código único do cliente
     * @return lista de pedidos resumidos do cliente
     * @throws RecursoNaoEncontradoException se cliente não existe
     */
    public List<PedidoResumoDTO> buscarTransacoes(String codigo) {
        Cliente cliente = buscarClientePorCodigo(codigo);

        List<Pedido> pedidos = pedidoRepository.findByClienteCodigoOrderByDataCriacaoDesc(codigo);

        return pedidos.stream()
                .map(this::converterParaResumoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Valida se uma senha é forte.
     * RNF0012: Mínimo 8 caracteres, maiúsculas, minúsculas, números e especiais
     *
     * @param senha senha a validar
     * @throws ValidacaoNegocioException se senha não é forte
     */
    private void validarSenhaForte(String senha) {
        if (senha == null || !PATTERN_SENHA.matcher(senha).matches()) {
            throw new ValidacaoNegocioException(
                    "Senha deve conter: mínimo 8 caracteres, letra maiúscula, letra minúscula, número e caractere especial (@$!%*?&)"
            );
        }
    }

    /**
     * Gera um código único para cliente.
     * Formato: CLT + timestamp + UUID reduzido
     *
     * @return código único
     */
    private String gerarCodigoUnico() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "CLT" + System.currentTimeMillis() + uuid;
    }

    /**
     * Busca cliente pelo código (helper).
     *
     * @param codigo código do cliente
     * @return cliente encontrado
     * @throws RecursoNaoEncontradoException se cliente não existe
     */
    private Cliente buscarClientePorCodigo(String codigo) {
        return clienteRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com código " + codigo + " não encontrado"));
    }

    /**
     * Converter entidade Cliente para DTO de detalhe.
     *
     * @param cliente entidade a converter
     * @return DTO de detalhe completo
     */
    private ClienteDetalheDTO converterParaDetalheDTO(Cliente cliente) {
        List<EnderecoDTO> enderecosDTO = cliente.getEnderecos().stream()
                .map(this::converterEnderecoParaDTO)
                .collect(Collectors.toList());

        List<CartaoDTO> cartoesDTO = cliente.getCartoes().stream()
                .map(this::converterCartaoParaDTO)
                .collect(Collectors.toList());

        return new ClienteDetalheDTO(
                cliente.getId(),
                cliente.getCodigo(),
                cliente.getNome(),
                cliente.getGenero(),
                cliente.getDataNascimento(),
                mascararCPF(cliente.getCpf()),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getRanking(),
                cliente.getStatus(),
                enderecosDTO,
                cartoesDTO
        );
    }

    /**
     * Converter entidade Endereco para DTO.
     *
     * @param endereco entidade a converter
     * @return DTO do endereço
     */
    private EnderecoDTO converterEnderecoParaDTO(Endereco endereco) {
        return new EnderecoDTO(
                endereco.getId(),
                endereco.getNomeIdentificador(),
                endereco.getTipoResidencia(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getBairro(),
                endereco.getCep(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getPais(),
                endereco.getTipoEndereco()
        );
    }

    /**
     * Converter entidade Cartao para DTO.
     * Mascara o número do cartão para segurança.
     *
     * @param cartao entidade a converter
     * @return DTO do cartão
     */
    private CartaoDTO converterCartaoParaDTO(Cartao cartao) {
        return new CartaoDTO(
                cartao.getId(),
                mascararNumeroCartao(cartao.getNumero()),
                cartao.getNomeImpresso(),
                cartao.getBandeira(),
                maskCVV(cartao.getCodigoSeguranca()),
                cartao.getPreferencial()
        );
    }

    /**
     * Converter entidade Pedido para DTO de resumo.
     *
     * @param pedido entidade a converter
     * @return DTO resumido do pedido
     */
    private PedidoResumoDTO converterParaResumoDTO(Pedido pedido) {
        int quantidadeItens = pedido.getItens() != null ? pedido.getItens().size() : 0;

        return new PedidoResumoDTO(
                pedido.getId(),
                pedido.getDataCriacao(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getValorFrete(),
                quantidadeItens
        );
    }

    /**
     * Mascara CPF para exibição segura.
     * Formato: 000.000.***-00
     *
     * @param cpf CPF completo
     * @return CPF mascarado
     */
    private String mascararCPF(String cpf) {
        if (cpf == null || cpf.length() < 11) {
            return cpf;
        }
        return cpf.substring(0, 7) + "***" + cpf.substring(10);
    }

    /**
     * Mascara número do cartão para exibição segura.
     * Formato: **** **** **** 1234
     *
     * @param numero número do cartão
     * @return número mascarado
     */
    private String mascararNumeroCartao(String numero) {
        if (numero == null || numero.length() < 4) {
            return numero;
        }
        return "**** **** **** " + numero.substring(numero.length() - 4);
    }

    /**
     * Mascara CVV para exibição segura.
     *
     * @param cvv código de segurança
     * @return CVV mascarado
     */
    private String maskCVV(String cvv) {
        return "***";
    }

    /**
     * Recalcula o ranking do cliente baseado no número de pedidos entregues/trocados.
     * RN0027: Cliente possui ranking numérico.
     * 
     * Regra de Ranking:
     * - 1-3 pedidos: ranking 1.0
     * - 4-7 pedidos: ranking 2.0
     * - 8+ pedidos: ranking 3.0
     * 
     * Chamado automaticamente após confirmarEntrega() no PedidoService.
     * 
     * @param codigoCliente código do cliente
     * @throws RecursoNaoEncontradoException se cliente não existe
     */
    @Transactional
    public void recalcularRanking(String codigoCliente) {
        // Buscar cliente
        Cliente cliente = buscarClientePorCodigo(codigoCliente);

        // Contar pedidos com status ENTREGUE ou TROCADO
        List<Pedido> pedidosEntregues = pedidoRepository.findByClienteIdAndStatus(
                cliente.getId(), 
                com.les.jakebooks.model.enums.StatusPedido.ENTREGUE
        );

        List<Pedido> pedidosTrocados = pedidoRepository.findByClienteIdAndStatus(
                cliente.getId(),
                com.les.jakebooks.model.enums.StatusPedido.TROCADO
        );

        // Total de pedidos completados
        int totalPedidos = pedidosEntregues.size() + pedidosTrocados.size();

        // Calcular novo ranking
        Double novoRanking;
        if (totalPedidos >= 8) {
            novoRanking = 3.0;
        } else if (totalPedidos >= 4) {
            novoRanking = 2.0;
        } else if (totalPedidos >= 1) {
            novoRanking = 1.0;
        } else {
            novoRanking = 0.0; // Sem pedidos completados, ranking 0
        }

        // Atualizar ranking do cliente
        cliente.setRanking(novoRanking);
        clienteRepository.save(cliente);
    }
}
