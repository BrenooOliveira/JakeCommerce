package com.les.jakebooks.dto;

import java.util.List;

/**
 * DTO para representar dados de um gráfico.
 * Utilizado em análise de vendas por período.
 * RF0055: Analisar histórico por período comparando produtos ou categorias.
 * RNF0055: Exibição em gráfico de linhas.
 */
public record DadosGraficoDTO(
        String label,
        List<PontoDTO> pontos
) {
}
