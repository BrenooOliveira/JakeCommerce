package com.les.jakebooks.service;

import com.les.jakebooks.dto.DadosGraficoDTO;
import com.les.jakebooks.dto.PontoDTO;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.repository.ItemPedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Service para análise de vendas.
 * RF0055: Analisar histórico por período comparando produtos ou categorias.
 * RNF0055: Exibição em gráfico de linhas.
 */
@Service
@Transactional(readOnly = true)
public class AnaliseService {

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Analisa as vendas de um período com agrupamento por produto ou categoria.
     * RF0055: Analisar histórico por período.
     * RNF0055: Exibição em gráfico de linhas.
     *
     * @param dataInicio   data de início do período
     * @param dataFim      data de fim do período
     * @param agrupamento  tipo de agrupamento: "PRODUTO" ou "CATEGORIA"
     * @return lista de DTOs com dados para gráfico
     * @throws ValidacaoNegocioException se agrupamento inválido ou datas inválidas
     */
    public List<DadosGraficoDTO> analisarVendasPorPeriodo(LocalDate dataInicio, LocalDate dataFim, String agrupamento) {
        // Validar agrupamento
        if (agrupamento == null || agrupamento.trim().isEmpty()) {
            throw new ValidacaoNegocioException("Agrupamento é obrigatório");
        }

        String agrupamentoNormalizado = agrupamento.toUpperCase().trim();
        if (!agrupamentoNormalizado.equals("PRODUTO") && !agrupamentoNormalizado.equals("CATEGORIA")) {
            throw new ValidacaoNegocioException("Agrupamento deve ser PRODUTO ou CATEGORIA");
        }

        // Validar período
        if (dataInicio == null || dataFim == null) {
            throw new ValidacaoNegocioException("Data de início e fim são obrigatórias");
        }

        if (dataInicio.isAfter(dataFim)) {
            throw new ValidacaoNegocioException("Data de início não pode ser posterior à data de fim");
        }

        // Buscar dados segundo agrupamento
        List<Object> resultados;
        if (agrupamentoNormalizado.equals("PRODUTO")) {
            resultados = itemPedidoRepository.buscarVendasPorProduto(dataInicio, dataFim);
        } else {
            resultados = itemPedidoRepository.buscarVendasPorCategoria(dataInicio, dataFim);
        }

        // Processar resultados em mapa
        Map<String, Map<String, BigDecimal>> dadosAgrupados = processarResultados(resultados);

        // Converter para DTOs
        return dadosAgrupados.entrySet().stream()
                .map(entry -> new DadosGraficoDTO(
                        entry.getKey(),
                        entry.getValue().entrySet().stream()
                                .map(pontoEntry -> new PontoDTO(pontoEntry.getKey(), pontoEntry.getValue()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Processa os resultados das queries.
     * Agrupa dados por label (produto ou categoria) e depois por data.
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

                // Extrair label (titulo ou categoria)
                String label = mapa.containsKey("titulo") ? 
                    (String) mapa.get("titulo") : 
                    (String) mapa.get("categoria");

                // Extrair data e converter para string
                Object dataObj = mapa.get("data");
                String periodoStr = formatarData(dataObj);

                // Extrair valor
                Number valorNum = (Number) mapa.get("valor");
                BigDecimal valor = BigDecimal.valueOf(valorNum.doubleValue());

                // Adicionar ao mapa agrupado com comparador de datas cronológicas
                dadosAgrupados.computeIfAbsent(label, k -> new TreeMap<>(this::compararDatas))
                        .put(periodoStr, valor);
            }
        }

        return dadosAgrupados;
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
