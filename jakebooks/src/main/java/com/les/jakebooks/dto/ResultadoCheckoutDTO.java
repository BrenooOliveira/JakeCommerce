package com.les.jakebooks.dto;

/**
 * DTO para resultado do processo de checkout.
 * TASK-CHK-05: Gerenciar Estado da Transacao
 *
 * Retorna o status da transacao e informacoes relevantes
 * para o cliente.
 */
public class ResultadoCheckoutDTO {

    /**
     * ID unico da transacao para rastreamento
     */
    private String transacaoId;

    /**
     * Status do resultado
     */
    private StatusResultadoCheckout status;

    /**
     * ID do pedido criado (null se checkout nao finalizado)
     */
    private Long pedidoId;

    /**
     * Mensagem descritiva do resultado
     */
    private String mensagem;

    /**
     * Tentativas restantes (relevante para PAGAMENTO_REPROVADO)
     */
    private Integer tentativasRestantes;

    // Construtores
    public ResultadoCheckoutDTO() {
    }

    public ResultadoCheckoutDTO(String transacaoId, StatusResultadoCheckout status, Long pedidoId,
                                String mensagem, Integer tentativasRestantes) {
        this.transacaoId = transacaoId;
        this.status = status;
        this.pedidoId = pedidoId;
        this.mensagem = mensagem;
        this.tentativasRestantes = tentativasRestantes;
    }

    // Getters e Setters
    public String getTransacaoId() {
        return transacaoId;
    }

    public void setTransacaoId(String transacaoId) {
        this.transacaoId = transacaoId;
    }

    public StatusResultadoCheckout getStatus() {
        return status;
    }

    public void setStatus(StatusResultadoCheckout status) {
        this.status = status;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Integer getTentativasRestantes() {
        return tentativasRestantes;
    }

    public void setTentativasRestantes(Integer tentativasRestantes) {
        this.tentativasRestantes = tentativasRestantes;
    }

    // Builder pattern
    public static ResultadoCheckoutDTOBuilder builder() {
        return new ResultadoCheckoutDTOBuilder();
    }

    public static class ResultadoCheckoutDTOBuilder {
        private String transacaoId;
        private StatusResultadoCheckout status;
        private Long pedidoId;
        private String mensagem;
        private Integer tentativasRestantes;

        public ResultadoCheckoutDTOBuilder transacaoId(String transacaoId) {
            this.transacaoId = transacaoId;
            return this;
        }

        public ResultadoCheckoutDTOBuilder status(StatusResultadoCheckout status) {
            this.status = status;
            return this;
        }

        public ResultadoCheckoutDTOBuilder pedidoId(Long pedidoId) {
            this.pedidoId = pedidoId;
            return this;
        }

        public ResultadoCheckoutDTOBuilder mensagem(String mensagem) {
            this.mensagem = mensagem;
            return this;
        }

        public ResultadoCheckoutDTOBuilder tentativasRestantes(Integer tentativasRestantes) {
            this.tentativasRestantes = tentativasRestantes;
            return this;
        }

        public ResultadoCheckoutDTO build() {
            return new ResultadoCheckoutDTO(transacaoId, status, pedidoId, mensagem, tentativasRestantes);
        }
    }

    /**
     * Enum para status do resultado do checkout
     */
    public enum StatusResultadoCheckout {
        SUCESSO,
        PAGAMENTO_REPROVADO,
        BLOQUEADO,
        ERRO
    }

    /**
     * Factory method para resultado de sucesso
     */
    public static ResultadoCheckoutDTO sucesso(String transacaoId, Long pedidoId) {
        return ResultadoCheckoutDTO.builder()
                .transacaoId(transacaoId)
                .status(StatusResultadoCheckout.SUCESSO)
                .pedidoId(pedidoId)
                .mensagem("Compra finalizada com sucesso!")
                .build();
    }

    /**
     * Factory method para pagamento reprovado
     */
    public static ResultadoCheckoutDTO pagamentoReprovado(String transacaoId, String motivo, int tentativasRestantes) {
        return ResultadoCheckoutDTO.builder()
                .transacaoId(transacaoId)
                .status(StatusResultadoCheckout.PAGAMENTO_REPROVADO)
                .mensagem(motivo)
                .tentativasRestantes(tentativasRestantes)
                .build();
    }

    /**
     * Factory method para carrinho bloqueado
     */
    public static ResultadoCheckoutDTO bloqueado(String transacaoId) {
        return ResultadoCheckoutDTO.builder()
                .transacaoId(transacaoId)
                .status(StatusResultadoCheckout.BLOQUEADO)
                .mensagem("Carrinho bloqueado devido a multiplas tentativas de pagamento reprovadas. Entre em contato com o suporte.")
                .tentativasRestantes(0)
                .build();
    }

    /**
     * Factory method para erro generico
     */
    public static ResultadoCheckoutDTO erro(String transacaoId, String mensagem) {
        return ResultadoCheckoutDTO.builder()
                .transacaoId(transacaoId)
                .status(StatusResultadoCheckout.ERRO)
                .mensagem(mensagem)
                .build();
    }
}
