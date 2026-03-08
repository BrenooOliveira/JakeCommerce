package com.les.jakebooks.dto;

import java.math.BigDecimal;

/**
 * DTO para representar um ponto em um gráfico.
 * Utilizado em análise de vendas por período.
 * RF0055: Analisar histórico por período comparando produtos ou categorias.
 */
public record PontoDTO(
        String periodo,
        BigDecimal valor
) {
}
