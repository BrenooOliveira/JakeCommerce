package com.les.jakebooks.service;

import com.les.jakebooks.domain.Cartao;
import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.domain.enums.BandeiraCartao;
import com.les.jakebooks.dto.CartaoResumoDTO;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.repository.CartaoRepository;
import com.les.jakebooks.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gerenciar operações de Cartão.
 * RF0027: Cadastrar múltiplos cartões (um preferencial).
 * RN0024: Campos obrigatórios do cartão.
 * RN0025: Bandeira deve estar cadastrada.
 * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão).
 */
@Service
@Transactional
public class CartaoService {

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private LogService logService;

    /**
     * Lista cartões ativos do cliente.
     * Conforme especificação TASK-PAY-04.
     *
     * @param clienteId ID do cliente
     * @return Lista de DTOs dos cartões ativos
     */
    public List<CartaoResumoDTO> listarCartoesAtivos(Long clienteId) {
        return cartaoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::converterCartaoParaResumoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Registra um novo cartão para o cliente durante checkout.
     * RF0027: Cadastrar múltiplos cartões.
     * RN0024: Campos obrigatórios.
     * RN0025: Bandeira cadastrada.
     *
     * @param numero número do cartão (13-19 dígitos)
     * @param nomeImpresso nome impresso (5-50 caracteres)
     * @param bandeira bandeira do cartão (VISA, MASTERCARD, ELO, AMEX)
     * @param codigoSeguranca CVV (3-4 dígitos)
     * @param preferencial se é o cartão preferencial
     * @param clienteId ID do cliente proprietário
     * @return Cartão criado e persistido
     * @throws ValidacaoNegocioException se validação falhar
     * @throws RecursoNaoEncontradoException se cliente não existe
     */
    public Cartao registrarCartao(
            String numero,
            String nomeImpresso,
            BandeiraCartao bandeira,
            String codigoSeguranca,
            Boolean preferencial,
            Long clienteId) {

        // Buscar cliente
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente não encontrado: ID " + clienteId));

        // Validar campos obrigatórios e formatos (RN0024, RN0025)
        validarFormatoCartao(numero, codigoSeguranca, nomeImpresso, bandeira);

        // Validar duplicação de cartão (mesmo cliente não pode ter 2 igual)
        validarDuplicacao(numero, clienteId);

        // Se este cartão será preferencial, desmarcar outros
        if (Boolean.TRUE.equals(preferencial)) {
            desmarcarOutrosPreferenciais(clienteId);
        } else {
            preferencial = false;  // Garantir que nunca seja null
        }

        // Criar novo cartão
        Cartao cartao = new Cartao(
                numero,
                nomeImpresso,
                bandeira,
                codigoSeguranca,
                preferencial
        );
        cartao.setCliente(cliente);

        // Persistir
        cartao = cartaoRepository.save(cartao);

        // Registrar log
        logService.registrar(
                "REGISTRAR_CARTAO",
                "Cartao",
                null,
                "Cartão: ****" + numero.substring(numero.length() - 4) +
                ", Bandeira: " + bandeira +
                ", Preferencial: " + preferencial,
                "Novo cartão registrado para cliente: " + cliente.getCodigo()
        );

        return cartao;
    }

    /**
     * Valida formato do cartão antes de registrar.
     * RN0024: Campos obrigatórios.
     * RN0025: Bandeira cadastrada.
     *
     * @param numero número do cartão
     * @param cvv código de segurança
     * @param nomeImpresso nome impresso
     * @param bandeira bandeira
     * @throws ValidacaoNegocioException se algum campo inválido
     */
    public void validarFormatoCartao(String numero, String cvv, String nomeImpresso, BandeiraCartao bandeira) {
        // Validar número (13-19 dígitos)
        if (numero == null || numero.isBlank()) {
            throw new ValidacaoNegocioException("Número do cartão é obrigatório");
        }

        if (!numero.matches("\\d{13,19}")) {
            throw new ValidacaoNegocioException(
                    "Número do cartão inválido. Deve conter entre 13 e 19 dígitos");
        }

        // Validar CVV (3-4 dígitos)
        if (cvv == null || cvv.isBlank()) {
            throw new ValidacaoNegocioException("Código de segurança é obrigatório");
        }

        if (!cvv.matches("\\d{3,4}")) {
            throw new ValidacaoNegocioException(
                    "Código de segurança inválido. Deve conter 3 ou 4 dígitos");
        }

        // Validar nome impresso (5-50 caracteres)
        if (nomeImpresso == null || nomeImpresso.isBlank()) {
            throw new ValidacaoNegocioException("Nome impresso é obrigatório");
        }

        if (nomeImpresso.length() < 5 || nomeImpresso.length() > 50) {
            throw new ValidacaoNegocioException(
                    "Nome impresso deve conter entre 5 e 50 caracteres");
        }

        // Validar bandeira (RN0025)
        validarBandeira(bandeira);
    }

    /**
     * Valida se a bandeira está cadastrada no sistema.
     * RN0025: Bandeira deve estar cadastrada.
     *
     * @param bandeira bandeira a validar
     * @throws ValidacaoNegocioException se bandeira inválida
     */
    public void validarBandeira(BandeiraCartao bandeira) {
        if (bandeira == null) {
            throw new ValidacaoNegocioException("Bandeira é obrigatória");
        }

        // Verificar se é uma bandeira válida (enum garante isso)
        try {
            // Tentar converter para garantir que é válida
            BandeiraCartao.valueOf(bandeira.name());
        } catch (IllegalArgumentException e) {
            throw new ValidacaoNegocioException(
                    "Bandeira '" + bandeira.name() + "' não é permitida");
        }
    }

    /**
     * Valida se outro cartão com o mesmo número já existe para este cliente.
     * Previne duplicação no mesmo cliente.
     *
     * @param numero número do cartão
     * @param clienteId ID do cliente
     * @throws ValidacaoNegocioException se cartão já existe
     */
    public void validarDuplicacao(String numero, Long clienteId) {
        List<Cartao> cartoesExistentes = cartaoRepository.findByClienteId(clienteId);

        boolean jaExiste = cartoesExistentes.stream()
                .anyMatch(c -> c.getNumero().equals(numero));

        if (jaExiste) {
            throw new ValidacaoNegocioException(
                    "Este cartão já está registrado para o cliente");
        }
    }

    /**
     * Desmarcar outros cartões como preferencial ao registrar novo preferencial.
     * Garante que apenas um cartão é preferencial por cliente.
     *
     * @param clienteId ID do cliente
     */
    private void desmarcarOutrosPreferenciais(Long clienteId) {
        List<Cartao> cartoesPreferenciais = cartaoRepository.findByClienteId(clienteId)
                .stream()
                .filter(c -> Boolean.TRUE.equals(c.getPreferencial()))
                .collect(Collectors.toList());

        for (Cartao cartao : cartoesPreferenciais) {
            cartao.setPreferencial(false);
            cartaoRepository.save(cartao);
        }
    }

    /**
     * Converte entidade Cartao para DTO resumido.
     * Método reutilizado para consistência.
     *
     * @param cartao entidade a converter
     * @return DTO resumido do cartão
     */
    private CartaoResumoDTO converterCartaoParaResumoDTO(Cartao cartao) {
        // Mascarar número do cartão
        String numero = cartao.getNumero();
        String numeroMascarado = "**** **** **** " + numero.substring(numero.length() - 4);

        return new CartaoResumoDTO(
                cartao.getId(),
                numeroMascarado,
                cartao.getNomeImpresso(),
                cartao.getBandeira(),
                cartao.getPreferencial()
        );
    }
}
