package com.les.jakebooks.services;

import com.les.jakebooks.domain.Carrinho;
import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.domain.Estoque;
import com.les.jakebooks.domain.ItemCarrinho;
import com.les.jakebooks.domain.Livro;
import com.les.jakebooks.dto.CarrinhoComExpiracaoDTO;
import com.les.jakebooks.dto.CarrinhoDTO;
import com.les.jakebooks.dto.ItemCarrinhoDTO;
import com.les.jakebooks.exception.CarrinhoBloqueadoPagamentoException;
import com.les.jakebooks.exception.CarrinhoNaoEncontradoException;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.model.enums.StatusCarrinho;
import com.les.jakebooks.repository.CarrinhoRepository;
import com.les.jakebooks.repository.ClienteRepository;
import com.les.jakebooks.repository.EstoqueRepository;
import com.les.jakebooks.repository.ItemCarrinhoRepository;
import com.les.jakebooks.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service para gerenciar operações de Carrinho de Compras.
 * RF0031: Gerenciar carrinho
 * RF0032: Definir quantidade no carrinho
 * RN0031: Validar estoque no carrinho
 * RN0032: Validar estoque antes da finalização
 * RN0044: Carrinho é bloqueado com aviso 5 minutos antes da expiração
 * RN0063: Máximo 10 unidades do mesmo livro por pedido
 * RNF venda: Exibir itens removidos do carrinho por expiração
 */
@Service
@Transactional
public class CarrinhoService {

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private ItemCarrinhoRepository itemCarrinhoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    // Constantes
    private static final int MAX_QUANTIDADE_LIVRO = 10;  // RN0063
    private static final int MAX_TENTATIVAS_REPROVADAS = 3;  // RN0065

    /**
     * Obtém o carrinho aberto do cliente ou cria um novo.
     * RF0031: Gerenciar carrinho
     *
     * @param codigoCliente código único do cliente
     * @return DTO do carrinho
     * @throws RecursoNaoEncontradoException se cliente não existe
     */
    public CarrinhoDTO obterOuCriar(String codigoCliente) {
        // Validar se cliente existe
        Cliente cliente = clienteRepository.findByCodigo(codigoCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com código " + codigoCliente + " não encontrado"));

        // Buscar carrinho aberto
        Optional<Carrinho> carrinhoAberto = carrinhoRepository.findByClienteIdAndStatusEquals(
                cliente.getId(), StatusCarrinho.ABERTO);

        Carrinho carrinho;
        if (carrinhoAberto.isPresent()) {
            carrinho = carrinhoAberto.get();
        } else {
            // Criar novo carrinho
            carrinho = new Carrinho();
            carrinho.setCliente(cliente);
            carrinho.setStatus(StatusCarrinho.ABERTO);
            carrinho.setDataCriacao(LocalDate.now());
            carrinho.setDataExpiracao(LocalDate.now().plusDays(1));  // Expira em 1 dia (aprox 30 minutos em abstração)

            carrinho = carrinhoRepository.save(carrinho);
        }

        return converterParaDTO(carrinho);
    }

    /**
     * Adiciona um item ao carrinho.
     * RF0032: Definir quantidade no carrinho
     * RN0031: Validar estoque no carrinho
     * RN0063: Máximo 10 unidades do mesmo livro
     *
     * @param codigoCliente código do cliente
     * @param codigoLivro código do livro
     * @param quantidade quantidade a adicionar
     * @return DTO do carrinho atualizado
     * @throws RecursoNaoEncontradoException se cliente ou livro não existe
     * @throws ValidacaoNegocioException se quantidade excede limite ou estoque insuficiente
     */
    public CarrinhoDTO adicionarItem(String codigoCliente, String codigoLivro, int quantidade) {
        // Validar quantidade
        if (quantidade <= 0) {
            throw new ValidacaoNegocioException("Quantidade deve ser maior que zero");
        }

        // Validar limite máximo por livro (RN0063)
        if (quantidade > MAX_QUANTIDADE_LIVRO) {
            throw new ValidacaoNegocioException("Máximo de " + MAX_QUANTIDADE_LIVRO + " unidades do mesmo livro por pedido");
        }

        // Buscar cliente
        Cliente cliente = clienteRepository.findByCodigo(codigoCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com código " + codigoCliente + " não encontrado"));

        // Buscar livro pelo código (ISBN)
        Livro livro = livroRepository.findByIsbn(codigoLivro);
        if (livro == null) {
            throw new RecursoNaoEncontradoException("Livro com código " + codigoLivro + " não encontrado");
        }

        // Validar estoque (RN0031)
        Estoque estoque = estoqueRepository.findByLivroId(livro.getId());
        if (estoque == null || estoque.getQuantidade() < quantidade) {
            throw new ValidacaoNegocioException("Estoque insuficiente para o livro: " + livro.getTitulo() + 
                    ". Disponível: " + (estoque != null ? estoque.getQuantidade() : 0));
        }

        // Obter ou criar carrinho
        Optional<Carrinho> carrinhoOpt = carrinhoRepository.findByClienteIdAndStatusEquals(
                cliente.getId(), StatusCarrinho.ABERTO);
        
        Carrinho carrinho;
        if (carrinhoOpt.isEmpty()) {
            carrinho = new Carrinho();
            carrinho.setCliente(cliente);
            carrinho.setStatus(StatusCarrinho.ABERTO);
            carrinho.setDataCriacao(LocalDate.now());
            carrinho.setDataExpiracao(LocalDate.now().plusDays(1));  // Expira em 1 dia
            carrinho = carrinhoRepository.save(carrinho);
        } else {
            carrinho = carrinhoOpt.get();
        }

        // Verificar se livro já está no carrinho
        Optional<ItemCarrinho> itemExistente = itemCarrinhoRepository.findByCarrinhoIdAndLivroId(
                carrinho.getId(), livro.getId());

        ItemCarrinho item;
        if (itemExistente.isPresent()) {
            item = itemExistente.get();
            int novaQuantidade = item.getQuantidade() + quantidade;

            // Validar se nova quantidade não ultrapassa limite (RN0063)
            if (novaQuantidade > MAX_QUANTIDADE_LIVRO) {
                throw new ValidacaoNegocioException("Quantidade total do livro no carrinho não pode exceder " + MAX_QUANTIDADE_LIVRO + " unidades");
            }

            // Validar se há estoque para adicionar mais
            if (estoque.getQuantidade() < novaQuantidade) {
                throw new ValidacaoNegocioException("Estoque insuficiente. Disponível: " + estoque.getQuantidade() + 
                        ". Solicitado: " + novaQuantidade);
            }

            item.setQuantidade(novaQuantidade);
        } else {
            // Criar novo item
            item = new ItemCarrinho();
            item.setCarrinho(carrinho);
            item.setLivro(livro);
            item.setQuantidade(quantidade);
            item.setValorUnitario(livro.getValorVenda());  // Registra valor atual do livro
        }

        itemCarrinhoRepository.save(item);
        carrinho.getItens().add(item);

        return converterParaDTO(carrinho);
    }

    /**
     * Remove um item do carrinho.
     *
     * @param codigoCliente código do cliente
     * @param codigoLivro código do livro
     * @return DTO do carrinho atualizado
     * @throws RecursoNaoEncontradoException se cliente, carrinho ou livro não existe
     */
    public CarrinhoDTO removerItem(String codigoCliente, String codigoLivro) {
        // Buscar cliente
        Cliente cliente = clienteRepository.findByCodigo(codigoCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com código " + codigoCliente + " não encontrado"));

        // Buscar carrinho aberto
        Carrinho carrinho = carrinhoRepository.findByClienteIdAndStatusEquals(cliente.getId(), StatusCarrinho.ABERTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho aberto não encontrado para cliente " + codigoCliente));

        // Buscar livro
        Livro livro = livroRepository.findByIsbn(codigoLivro);
        if (livro == null) {
            throw new RecursoNaoEncontradoException("Livro com código " + codigoLivro + " não encontrado");
        }

        // Buscar e remover item
        Optional<ItemCarrinho> item = itemCarrinhoRepository.findByCarrinhoIdAndLivroId(carrinho.getId(), livro.getId());
        if (item.isEmpty()) {
            throw new RecursoNaoEncontradoException("Item não encontrado no carrinho");
        }

        itemCarrinhoRepository.delete(item.get());
        carrinho.getItens().remove(item.get());

        return converterParaDTO(carrinho);
    }

    /**
     * Altera a quantidade de um item no carrinho.
     * RF0032: Definir quantidade no carrinho
     * RN0063: Máximo 10 unidades do mesmo livro
     *
     * @param codigoCliente código do cliente
     * @param codigoLivro código do livro
     * @param novaQuantidade nova quantidade
     * @return DTO do carrinho atualizado
     * @throws ValidacaoNegocioException se quantidade é inválida
     */
    public CarrinhoDTO alterarQuantidade(String codigoCliente, String codigoLivro, int novaQuantidade) {
        // Validar quantidade
        if (novaQuantidade <= 0) {
            throw new ValidacaoNegocioException("Quantidade deve ser maior que zero");
        }

        // Validar limite máximo (RN0063)
        if (novaQuantidade > MAX_QUANTIDADE_LIVRO) {
            throw new ValidacaoNegocioException("Máximo de " + MAX_QUANTIDADE_LIVRO + " unidades do mesmo livro");
        }

        // Buscar cliente
        Cliente cliente = clienteRepository.findByCodigo(codigoCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com código " + codigoCliente + " não encontrado"));

        // Buscar carrinho aberto
        Carrinho carrinho = carrinhoRepository.findByClienteIdAndStatusEquals(cliente.getId(), StatusCarrinho.ABERTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho aberto não encontrado"));

        // Buscar livro
        Livro livro = livroRepository.findByIsbn(codigoLivro);
        if (livro == null) {
            throw new RecursoNaoEncontradoException("Livro com código " + codigoLivro + " não encontrado");
        }

        // Validar estoque
        Estoque estoque = estoqueRepository.findByLivroId(livro.getId());
        if (estoque == null || estoque.getQuantidade() < novaQuantidade) {
            throw new ValidacaoNegocioException("Estoque insuficiente. Disponível: " + 
                    (estoque != null ? estoque.getQuantidade() : 0));
        }

        // Buscar e atualizar item
        ItemCarrinho item = itemCarrinhoRepository.findByCarrinhoIdAndLivroId(carrinho.getId(), livro.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado no carrinho"));

        item.setQuantidade(novaQuantidade);
        itemCarrinhoRepository.save(item);

        return converterParaDTO(carrinho);
    }

    /**
     * Verifica expiração do carrinho.
     * RN0044: Carrinho é bloqueado com aviso 5 minutos antes da expiração
     * RNF venda: Exibir itens removidos do carrinho por expiração
     *
     * @param codigoCliente código do cliente
     * @return DTO com informações de expiração e itens removidos se expirou
     * @throws RecursoNaoEncontradoException se cliente ou carrinho não existe
     */
    public CarrinhoComExpiracaoDTO verificarExpiracao(String codigoCliente) {
        // Buscar cliente
        Cliente cliente = clienteRepository.findByCodigo(codigoCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com código " + codigoCliente + " não encontrado"));

        // Buscar carrinho aberto
        Carrinho carrinho = carrinhoRepository.findByClienteIdAndStatusEquals(cliente.getId(), StatusCarrinho.ABERTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho aberto não encontrado"));

        LocalDate agora = LocalDate.now();
        LocalDate dataExpiracao = carrinho.getDataExpiracao();
        List<ItemCarrinhoDTO> itensRemovidos = new ArrayList<>();
        String mensagemAviso = null;
        Boolean proximoDeExpirar = false;
        Integer minutosFaltando = 0;

        // Calcular dias faltando (conversão para minutos)
        if (agora.isBefore(dataExpiracao)) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(agora, dataExpiracao);
            minutosFaltando = (int) (dias * 24 * 60);  // Converter para minutos

            // Verificar se está próximo de expirar (menos de 1 dia)
            if (dias == 0) {
                proximoDeExpirar = true;
                mensagemAviso = "Seu carrinho expirará em menos de 1 dia. Complete sua compra!";
            }
        } else {
            // Carrinho expirou
            carrinho.setStatus(StatusCarrinho.EXPIRADO);
            carrinhoRepository.save(carrinho);

            // Copiar itens removidos antes de limpar
            itensRemovidos = carrinho.getItens().stream()
                    .map(this::converterItemParaDTO)
                    .collect(Collectors.toList());

            // Limpar itens
            carrinho.getItens().clear();
            carrinhoRepository.save(carrinho);

            mensagemAviso = "Seu carrinho expirou e foi limpo. Os itens foram removidos.";
            proximoDeExpirar = false;
            minutosFaltando = 0;
        }

        return new CarrinhoComExpiracaoDTO(
                carrinho.getId(),
                codigoCliente,
                carrinho.getStatus(),
                carrinho.getDataCriacao(),
                carrinho.getDataExpiracao(),
                proximoDeExpirar,
                minutosFaltando,
                converterItensParaDTO(carrinho.getItens()),
                itensRemovidos,
                calcularValorTotal(carrinho.getItens()),
                mensagemAviso
            );
    }

    /**
     * Limpa todos os itens do carrinho.
     *
     * @param codigoCliente código do cliente
     * @return DTO do carrinho vazio
     * @throws RecursoNaoEncontradoException se cliente ou carrinho não existe
     */
    public CarrinhoDTO limpar(String codigoCliente) {
        // Buscar cliente
        Cliente cliente = clienteRepository.findByCodigo(codigoCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente com código " + codigoCliente + " não encontrado"));

        // Buscar carrinho aberto
        Carrinho carrinho = carrinhoRepository.findByClienteIdAndStatusEquals(cliente.getId(), StatusCarrinho.ABERTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho aberto não encontrado"));

        // Remover todos os itens
        itemCarrinhoRepository.deleteAll(carrinho.getItens());
        carrinho.getItens().clear();
        carrinhoRepository.save(carrinho);

        return converterParaDTO(carrinho);
    }

    /**
     * Converter entidade Carrinho para DTO.
     *
     * @param carrinho entidade a converter
     * @return DTO do carrinho
     */
    private CarrinhoDTO converterParaDTO(Carrinho carrinho) {
        return new CarrinhoDTO(
                carrinho.getId(),
                carrinho.getCliente().getCodigo(),
                carrinho.getStatus(),
                carrinho.getDataCriacao(),
                carrinho.getDataExpiracao(),
                converterItensParaDTO(carrinho.getItens()),
                calcularValorTotal(carrinho.getItens())
        );
    }

    /**
     * Converter lista de items para DTOs.
     *
     * @param itens lista de items
     * @return lista de DTOs
     */
    private List<ItemCarrinhoDTO> converterItensParaDTO(List<ItemCarrinho> itens) {
        return itens.stream()
                .map(this::converterItemParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converter entidade ItemCarrinho para DTO.
     *
     * @param item entidade a converter
     * @return DTO do item
     */
    private ItemCarrinhoDTO converterItemParaDTO(ItemCarrinho item) {
        BigDecimal subtotal = item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));

        return new ItemCarrinhoDTO(
                item.getId(),
                item.getLivro().getId(),
                item.getLivro().getCodigo(),
                item.getLivro().getTitulo(),
                item.getQuantidade(),
                item.getValorUnitario(),
                subtotal
        );
    }

    /**
     * Calcula o valor total do carrinho.
     *
     * @param itens itens do carrinho
     * @return valor total
     */
    private BigDecimal calcularValorTotal(List<ItemCarrinho> itens) {
        return itens.stream()
                .map(item -> item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Incrementa contador de tentativas reprovadas.
     * RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho.
     * TASK-PAY-06: Controlar tentativas reprovadas.
     *
     * @param carrinhoId ID do carrinho
     * @throws CarrinhoBloqueadoPagamentoException se atingir limite
     * @throws CarrinhoNaoEncontradoException se carrinho não existe
     */
    @Transactional
    public void registrarTentativaReprovada(Long carrinhoId) {
        Carrinho carrinho = carrinhoRepository.findById(carrinhoId)
                .orElseThrow(() -> new CarrinhoNaoEncontradoException(carrinhoId));

        if (carrinho.getStatus() == StatusCarrinho.BLOQUEADO) {
            throw new CarrinhoBloqueadoPagamentoException(carrinho.getCliente().getId());
        }

        int tentativas = carrinho.getTentativasReprovadas() + 1;
        carrinho.setTentativasReprovadas(tentativas);

        if (tentativas >= MAX_TENTATIVAS_REPROVADAS) {
            carrinho.setStatus(StatusCarrinho.BLOQUEADO);
            carrinho.setDataBloqueio(LocalDateTime.now());
            carrinhoRepository.save(carrinho);

            // Lançar exceção informando bloqueio
            throw new CarrinhoBloqueadoPagamentoException(
                    carrinho.getCliente().getId(), tentativas);
        }

        carrinhoRepository.save(carrinho);
    }

    /**
     * Reseta contador apos pagamento aprovado.
     * RN0065: Contador reseta após aprovação.
     * TASK-PAY-06: Controlar tentativas reprovadas.
     *
     * @param carrinhoId ID do carrinho
     * @throws CarrinhoNaoEncontradoException se carrinho não existe
     */
    @Transactional
    public void resetarTentativasReprovadas(Long carrinhoId) {
        Carrinho carrinho = carrinhoRepository.findById(carrinhoId)
                .orElseThrow(() -> new CarrinhoNaoEncontradoException(carrinhoId));

        carrinho.setTentativasReprovadas(0);
        carrinhoRepository.save(carrinho);
    }

    /**
     * Verifica se carrinho esta bloqueado.
     * TASK-PAY-06: Controlar tentativas reprovadas.
     *
     * @param carrinhoId ID do carrinho
     * @throws CarrinhoBloqueadoPagamentoException se carrinho está bloqueado
     * @throws CarrinhoNaoEncontradoException se carrinho não existe
     */
    public void verificarCarrinhoBloqueado(Long carrinhoId) {
        Carrinho carrinho = carrinhoRepository.findById(carrinhoId)
                .orElseThrow(() -> new CarrinhoNaoEncontradoException(carrinhoId));

        if (carrinho.getStatus() == StatusCarrinho.BLOQUEADO) {
            throw new CarrinhoBloqueadoPagamentoException(carrinho.getCliente().getId());
        }
    }

    /**
     * Retorna tentativas restantes.
     * TASK-PAY-06: Controlar tentativas reprovadas.
     *
     * @param carrinhoId ID do carrinho
     * @return número de tentativas restantes
     * @throws CarrinhoNaoEncontradoException se carrinho não existe
     */
    public int getTentativasRestantes(Long carrinhoId) {
        Carrinho carrinho = carrinhoRepository.findById(carrinhoId)
                .orElseThrow(() -> new CarrinhoNaoEncontradoException(carrinhoId));

        return MAX_TENTATIVAS_REPROVADAS - carrinho.getTentativasReprovadas();
    }

    /**
     * Finaliza o carrinho alterando status para FINALIZADO.
     * TASK-CHK-03: Converter Carrinho em Pedido
     * RF0037: Finalizar compra
     *
     * Chamado após conversão bem-sucedida de carrinho em pedido.
     * Impede que o carrinho seja reutilizado.
     *
     * @param carrinhoId ID do carrinho a finalizar
     * @throws CarrinhoNaoEncontradoException se carrinho não existe
     */
    @Transactional
    public void finalizarCarrinho(Long carrinhoId) {
        Carrinho carrinho = carrinhoRepository.findById(carrinhoId)
                .orElseThrow(() -> new CarrinhoNaoEncontradoException(carrinhoId));

        carrinho.setStatus(StatusCarrinho.FINALIZADO);
        carrinhoRepository.save(carrinho);
    }

    /**
     * Busca carrinho por ID.
     * TASK-CHK-03: Usado para conversão de carrinho em pedido.
     *
     * @param carrinhoId ID do carrinho
     * @return entidade Carrinho
     * @throws CarrinhoNaoEncontradoException se carrinho não existe
     */
    public Carrinho buscarPorId(Long carrinhoId) {
        return carrinhoRepository.findById(carrinhoId)
                .orElseThrow(() -> new CarrinhoNaoEncontradoException(carrinhoId));
    }
}
