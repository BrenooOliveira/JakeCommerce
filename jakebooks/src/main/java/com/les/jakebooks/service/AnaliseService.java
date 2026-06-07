package com.les.jakebooks.service;

import com.les.jakebooks.dto.DadosGraficoDTO;
import com.les.jakebooks.dto.PontoDTO;
import com.les.jakebooks.domain.Categoria;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.repository.CategoriaRepository;
import com.les.jakebooks.repository.ItemPedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Service para análise de vendas.
 * RF0055: Analisar histórico por período por categoria selecionada.
 * RNF0055: Exibição em gráfico de linhas.
 */
@Service
@Transactional(readOnly = true)
public class AnaliseService {

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Analisa o volume vendido por categoria em um período.
     * RF0055: Analisar histórico por período.
     * RNF0055: Exibição em gráfico de linhas.
     *
     * @param dataInicio   data de início do período
     * @param dataFim      data de fim do período
     * @param categoriaIds categorias selecionadas para análise
     * @return lista de DTOs com dados para gráfico
     * @throws ValidacaoNegocioException se categorias ou datas forem inválidas
     */
    public List<DadosGraficoDTO> analisarVendasPorPeriodo(LocalDate dataInicio, LocalDate dataFim, List<Long> categoriaIds) {
        // Validar categorias
        List<Long> categoriaIdsSelecionadas = categoriaIds == null ? List.of() : categoriaIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (categoriaIdsSelecionadas.isEmpty()) {
            throw new ValidacaoNegocioException("Selecione ao menos uma categoria");
        }

        // Validar período
        if (dataInicio == null || dataFim == null) {
            throw new ValidacaoNegocioException("Data de início e fim são obrigatórias");
        }

        if (dataInicio.isAfter(dataFim)) {
            throw new ValidacaoNegocioException("Data de início não pode ser posterior à data de fim");
        }

        // Buscar categorias para manter todas as linhas solicitadas, mesmo sem venda no período
        List<Categoria> categoriasSelecionadas = categoriaRepository.findAllById(categoriaIdsSelecionadas);
        if (categoriasSelecionadas.isEmpty()) {
            throw new ValidacaoNegocioException("Nenhuma categoria válida foi encontrada");
        }

        Map<Long, String> nomeCategoriaPorId = categoriasSelecionadas.stream()
            .collect(Collectors.toMap(Categoria::getId, Categoria::getNome, (primeiro, segundo) -> primeiro, LinkedHashMap::new));

        // Buscar dados apenas para as categorias selecionadas
        List<Object> resultados = itemPedidoRepository.buscarVendasPorCategoria(dataInicio, dataFim, categoriaIdsSelecionadas);

        // Processar resultados em mapa
        Map<String, Map<String, BigDecimal>> dadosAgrupados = processarResultados(resultados);

        List<String> labelsPeriodo = gerarLabelsPeriodo(dataInicio, dataFim);

        // Converter para DTOs
        return nomeCategoriaPorId.values().stream()
            .map(nomeCategoria -> {
                Map<String, BigDecimal> pontosCategoria = dadosAgrupados.getOrDefault(nomeCategoria, new HashMap<>());

                List<PontoDTO> pontos = labelsPeriodo.stream()
                    .map(periodo -> new PontoDTO(
                        periodo,
                        pontosCategoria.getOrDefault(periodo, BigDecimal.ZERO)
                    ))
                    .collect(Collectors.toList());

                return new DadosGraficoDTO(nomeCategoria, pontos);
            })
            .collect(Collectors.toList());
    }

    /**
     * Processa os resultados das queries.
    * Agrupa dados por categoria e depois por data.
     *
     * @param resultados resultados brutos das queries
     * @return mapa estruturado com label -> (data -> valor)
     */
    private Map<String, Map<String, BigDecimal>> processarResultados(List<Object> resultados) {
        Map<String, Map<String, BigDecimal>> dadosAgrupados = new LinkedHashMap<>();

        for (Object resultado : resultados) {
            if (resultado instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapa = (Map<String, Object>) resultado;

                // Extrair label da categoria
                String label = (String) mapa.get("categoria");

                // Extrair data e converter para string
                Object dataObj = mapa.get("data");
                String periodoStr = formatarData(dataObj);

                // Extrair quantidade vendida
                Number quantidadeNum = (Number) mapa.get("quantidade");
                BigDecimal quantidade = BigDecimal.valueOf(quantidadeNum.doubleValue());

                // Adicionar ao mapa agrupado com comparador de datas cronológicas
                dadosAgrupados.computeIfAbsent(label, k -> new TreeMap<>(this::compararDatas))
                        .put(periodoStr, quantidade);
            }
        }

        return dadosAgrupados;
    }

    /**
     * Gera todos os dias entre a data inicial e final, inclusive.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return lista ordenada de datas formatadas
     */
    private List<String> gerarLabelsPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        List<String> labels = new ArrayList<>();

        for (LocalDate data = dataInicio; !data.isAfter(dataFim); data = data.plusDays(1)) {
            labels.add(data.format(FORMATTER));
        }

        return labels;
    }

    /**
     * Comparador para ordenar datas em formato dd/MM/yyyy cronologicamente.
     *
     * @param data1 primeira data em formato dd/MM/yyyy
     * @param data2 segunda data em formato dd/MM/yyyy
     * @return valor negativo, zero ou positivo conforme ordenação
     */
    private int compararDatas(String data1, String data2) {
        try {
            LocalDate d1 = LocalDate.parse(data1, FORMATTER);
            LocalDate d2 = LocalDate.parse(data2, FORMATTER);
            return d1.compareTo(d2);
        } catch (Exception e) {
            // Fallback para comparação alfanumérica se houver erro
            return data1.compareTo(data2);
        }
    }

    /**
     * Formata a data para string.
     *
     * @param dataObj objeto de data do resultado
     * @return string formatada da data
     */
    private String formatarData(Object dataObj) {
        if (dataObj instanceof LocalDate) {
            return ((LocalDate) dataObj).format(FORMATTER);
        } else if (dataObj instanceof java.sql.Date) {
            return ((java.sql.Date) dataObj).toLocalDate().format(FORMATTER);
        } else if (dataObj instanceof java.util.Date) {
            return new java.text.SimpleDateFormat("dd/MM/yyyy").format(dataObj);
        }
        return dataObj != null ? dataObj.toString() : "Data desconhecida";
    }
}
