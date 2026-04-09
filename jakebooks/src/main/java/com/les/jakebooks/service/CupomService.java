package com.les.jakebooks.service;

import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.domain.Cupom;
import com.les.jakebooks.dto.CupomAplicadoDTO;
import com.les.jakebooks.dto.CupomDTO;
import com.les.jakebooks.exception.CupomInvalidoException;
import com.les.jakebooks.exception.CupomJaUtilizadoException;
import com.les.jakebooks.exception.CupomNaoEncontradoException;
import com.les.jakebooks.exception.CupomPromocionalDuplicadoException;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.domain.enums.TipoCupom;
import com.les.jakebooks.repository.ClienteRepository;
import com.les.jakebooks.repository.CupomRepository;
import com.les.jakebooks.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service para gerenciar operações de Cupom.
 * RF0036: Selecionar pagamento (cupom promocional, cupom de troca).
 * RF0044: Gerar cupom de troca.
 * RN0033: Apenas um cupom promocional por compra.
 * RN0035: Consumir cupons antes do cartão.
 * RN0036: Gerar cupom para excedente.
 */
@Service
@Transactional
public class CupomService {

    @Autowired
    private CupomRepository cupomRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private LogService logService;

    /**
     * Lista cupons de troca ativos do cliente.
     * RN0035: Listar cupons de troca disponíveis para uso no pagamento.
     *
     * @param clienteId ID do cliente
     * @return lista de DTOs dos cupons de troca ativos
     */
    public List<CupomDTO> listarCuponsTrocaAtivos(Long clienteId) {
        List<Cupom> cupons = cupomRepository.findCuponsTrocaAtivosDoCliente(clienteId);
        return cupons.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * Calcula o saldo total de cupons de troca do cliente.
     *
     * @param clienteId ID do cliente
     * @return soma dos valores dos cupons de troca ativos
     */
    public BigDecimal calcularSaldoCuponsTroca(Long clienteId) {
        List<Cupom> cupons = cupomRepository.findCuponsTrocaAtivosDoCliente(clienteId);
        return cupons.stream()
                .map(Cupom::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Valida e retorna um cupom promocional pelo código.
     * RN0033: Apenas um cupom promocional por compra.
     *
     * @param codigo código do cupom
     * @return DTO do cupom se válido
     * @throws CupomInvalidoException se cupom não existe, está inativo ou expirado
     */
    public CupomDTO validarCupomPromocional(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new CupomInvalidoException("Código do cupom é obrigatório", null, "CODIGO_VAZIO");
        }

        Cupom cupom = cupomRepository.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new CupomInvalidoException(
                        "Cupom não encontrado: " + codigo, codigo, "NAO_ENCONTRADO"));

        // Validar se é promocional
        if (!TipoCupom.PROMOCIONAL.equals(cupom.getTipo())) {
            throw new CupomInvalidoException(
                    "Este não é um cupom promocional", codigo, "TIPO_INVALIDO");
        }

        // Validar se está ativo
        if (!Boolean.TRUE.equals(cupom.getAtivo())) {
            throw new CupomInvalidoException(
                    "Cupom inativo: " + codigo, codigo, "INATIVO");
        }

        // Validar validade
        if (cupom.getDataValidade() != null && LocalDate.now().isAfter(cupom.getDataValidade())) {
            throw new CupomInvalidoException(
                    "Cupom expirado: " + codigo, codigo, "EXPIRADO");
        }

        return converterParaDTO(cupom);
    }

    /**
     * Valida se cupons de troca pertencem ao cliente e estão ativos.
     *
     * @param clienteId ID do cliente
     * @param cuponsIds IDs dos cupons a validar
     * @return lista de cupons válidos
     * @throws CupomInvalidoException se algum cupom for inválido
     */
    public List<Cupom> validarCuponsTroca(Long clienteId, List<Long> cuponsIds) {
        if (cuponsIds == null || cuponsIds.isEmpty()) {
            return List.of();
        }

        return cuponsIds.stream()
                .map(cupomId -> {
                    Cupom cupom = cupomRepository.findById(cupomId)
                            .orElseThrow(() -> new CupomInvalidoException(
                                    "Cupom não encontrado: ID " + cupomId, String.valueOf(cupomId)));

                    // Validar tipo
                    if (!TipoCupom.TROCA.equals(cupom.getTipo())) {
                        throw new CupomInvalidoException(
                                "Cupom ID " + cupomId + " não é um cupom de troca",
                                String.valueOf(cupomId), "TIPO_INVALIDO");
                    }

                    // Validar se pertence ao cliente
                    if (cupom.getCliente() == null || !cupom.getCliente().getId().equals(clienteId)) {
                        throw new CupomInvalidoException(
                                "Cupom ID " + cupomId + " não pertence ao cliente",
                                String.valueOf(cupomId), "NAO_PERTENCE");
                    }

                    // Validar se está ativo e válido
                    if (!cupom.isValido()) {
                        throw new CupomInvalidoException(
                                "Cupom ID " + cupomId + " está inativo ou expirado",
                                String.valueOf(cupomId), "INVALIDO");
                    }

                    return cupom;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcula o valor total de uma lista de cupons.
     *
     * @param cupons lista de cupons
     * @return soma dos valores
     */
    public BigDecimal calcularValorTotalCupons(List<Cupom> cupons) {
        if (cupons == null || cupons.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return cupons.stream()
                .map(Cupom::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Gera um novo cupom de troca para o cliente.
     * RN0036: Gerar cupom para excedente.
     * RF0044: Gerar cupom de troca.
     *
     * @param cliente cliente que receberá o cupom
     * @param valor valor do cupom
     * @param motivo motivo da geração (ex: "EXCEDENTE_PAGAMENTO", "TROCA_PRODUTO")
     * @return DTO do cupom gerado
     */
    public CupomDTO gerarCupomTroca(Cliente cliente, BigDecimal valor, String motivo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CupomInvalidoException("Valor do cupom deve ser maior que zero");
        }

        String codigo = "TROCA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Cupom cupom = new Cupom();
        cupom.setCodigo(codigo);
        cupom.setValor(valor);
        cupom.setTipo(TipoCupom.TROCA);
        cupom.setAtivo(true);
        cupom.setCliente(cliente);
        cupom.setDataCriacao(LocalDate.now());
        // Cupons de troca não expiram por padrão

        cupom = cupomRepository.save(cupom);

        // Registrar log
        logService.registrar(
                "GERAR_CUPOM_TROCA",
                "Cupom",
                null,
                "Cupom: " + codigo + ", Valor: R$ " + valor + ", Cliente: " + cliente.getCodigo(),
                "Cupom de troca gerado - Motivo: " + motivo
        );

        return converterParaDTO(cupom);
    }

    /**
     * Marca cupons como consumidos (inativos).
     *
     * @param cupons lista de cupons a consumir
     */
    public void consumirCupons(List<Cupom> cupons) {
        if (cupons == null || cupons.isEmpty()) {
            return;
        }

        for (Cupom cupom : cupons) {
            cupom.setAtivo(false);
            cupomRepository.save(cupom);

            logService.registrar(
                    "CONSUMIR_CUPOM",
                    "Cupom",
                    "Ativo: true",
                    "Ativo: false",
                    "Cupom " + cupom.getCodigo() + " consumido em pagamento"
            );
        }
    }

    /**
     * Aplica cupons ao pagamento.
     * RN0033: Apenas um cupom promocional por compra.
     * RN0035: Consumir cupons antes do cartao.
     *
     * @param cuponsTrocaIds IDs dos cupons de troca selecionados
     * @param codigoPromocional codigo do cupom promocional (pode ser null)
     * @param clienteId ID do cliente
     * @return lista de cupons aplicados com valores
     * @throws CupomPromocionalDuplicadoException se mais de 1 promocional
     * @throws CupomNaoEncontradoException se cupom nao encontrado
     * @throws CupomJaUtilizadoException se cupom ja foi utilizado
     * @throws CupomInvalidoException se cupom invalido
     */
    public List<CupomAplicadoDTO> aplicarCupons(
            List<Long> cuponsTrocaIds,
            String codigoPromocional,
            Long clienteId) {

        List<CupomAplicadoDTO> cuponsAplicados = new java.util.ArrayList<>();

        // 1. Validar e aplicar cupons de troca
        if (cuponsTrocaIds != null && !cuponsTrocaIds.isEmpty()) {
            for (Long cupomId : cuponsTrocaIds) {
                Cupom cupom = cupomRepository.findById(cupomId)
                        .orElseThrow(() -> new CupomNaoEncontradoException(cupomId));

                // Validar que pertence ao cliente
                if (cupom.getCliente() == null || !cupom.getCliente().getId().equals(clienteId)) {
                    throw new CupomInvalidoException(
                            "Cupom nao pertence ao cliente", String.valueOf(cupomId), "NAO_PERTENCE");
                }

                // Validar que e do tipo TROCA
                if (!TipoCupom.TROCA.equals(cupom.getTipo())) {
                    throw new CupomInvalidoException(
                            "Cupom " + cupom.getCodigo() + " nao e do tipo troca",
                            cupom.getCodigo(), "TIPO_INVALIDO");
                }

                // Validar que esta ativo
                if (!Boolean.TRUE.equals(cupom.getAtivo())) {
                    throw new CupomJaUtilizadoException(
                            "Cupom " + cupom.getCodigo() + " ja foi utilizado", cupom.getCodigo());
                }

                // Validar validade
                if (cupom.getDataValidade() != null && LocalDate.now().isAfter(cupom.getDataValidade())) {
                    throw new CupomInvalidoException(
                            "Cupom " + cupom.getCodigo() + " esta expirado",
                            cupom.getCodigo(), "EXPIRADO");
                }

                cuponsAplicados.add(new CupomAplicadoDTO(
                        cupom.getId(),
                        cupom.getCodigo(),
                        cupom.getValor(),
                        TipoCupom.TROCA
                ));
            }
        }

        // 2. Validar e aplicar cupom promocional (se informado)
        if (codigoPromocional != null && !codigoPromocional.isBlank()) {
            CupomDTO promocional = validarCupomPromocional(codigoPromocional);

            cuponsAplicados.add(new CupomAplicadoDTO(
                    promocional.id(),
                    promocional.codigo(),
                    promocional.valor(),
                    TipoCupom.PROMOCIONAL
            ));
        }

        // 3. Validar limite de 1 cupom promocional (RN0033)
        long qtdPromocionais = cuponsAplicados.stream()
                .filter(c -> c.tipo() == TipoCupom.PROMOCIONAL)
                .count();

        if (qtdPromocionais > 1) {
            throw new CupomPromocionalDuplicadoException();
        }

        return cuponsAplicados;
    }

    /**
     * Busca cupom por ID.
     *
     * @param cupomId ID do cupom
     * @return cupom encontrado
     * @throws RecursoNaoEncontradoException se não encontrar
     */
    public Cupom buscarPorId(Long cupomId) {
        return cupomRepository.findById(cupomId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cupom não encontrado: ID " + cupomId));
    }

    /**
     * Busca cupom por código.
     *
     * @param codigo código do cupom
     * @return cupom encontrado
     * @throws RecursoNaoEncontradoException se não encontrar
     */
    public Cupom buscarPorCodigo(String codigo) {
        return cupomRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cupom não encontrado: " + codigo));
    }

    /**
     * Converte entidade Cupom para DTO.
     *
     * @param cupom entidade a converter
     * @return DTO do cupom
     */
    private CupomDTO converterParaDTO(Cupom cupom) {
        return new CupomDTO(
                cupom.getId(),
                cupom.getCodigo(),
                cupom.getValor(),
                cupom.getTipo(),
                cupom.getDataValidade(),
                Boolean.TRUE.equals(cupom.getAtivo())
        );
    }
}
