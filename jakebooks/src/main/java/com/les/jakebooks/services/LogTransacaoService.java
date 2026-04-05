package com.les.jakebooks.services;

import com.les.jakebooks.dto.ResultadoCheckoutDTO;
import com.les.jakebooks.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service especializado para logs de transacoes de checkout.
 * TASK-CHK-05: Gerenciar Estado da Transacao
 *
 * Centraliza toda a logica de logging relacionada ao processo
 * de checkout, garantindo rastreabilidade completa (RNF0012).
 */
@Service
public class LogTransacaoService {

    @Autowired
    private LogService logService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Registra inicio de uma transacao de checkout.
     * RNF0012: Log de transacao deve conter data, hora, usuario e dados alterados
     *
     * @param transacaoId ID unico da transacao
     * @param carrinhoId ID do carrinho sendo processado
     */
    public void iniciarTransacao(String transacaoId, Long carrinhoId) {
        String dataHora = LocalDateTime.now().format(FORMATTER);

        logService.registrar(
            "INICIAR_CHECKOUT",
            "Transacao",
            String.format("Transacao ID: %s | Carrinho ID: %d", transacaoId, carrinhoId),
            String.format("Data/Hora: %s | Status: INICIADA", dataHora),
            String.format("Transacao de checkout iniciada para carrinho %d", carrinhoId)
        );
    }

    /**
     * Registra finalizacao bem-sucedida de uma transacao.
     * RNF0012: Log completo da operacao
     *
     * @param transacaoId ID da transacao
     * @param pedidoId ID do pedido criado
     */
    public void finalizarTransacao(String transacaoId, Long pedidoId) {
        String dataHora = LocalDateTime.now().format(FORMATTER);

        logService.registrar(
            "FINALIZAR_CHECKOUT",
            "Transacao",
            String.format("Transacao ID: %s", transacaoId),
            String.format("Data/Hora: %s | Status: SUCESSO | Pedido ID: %d", dataHora, pedidoId),
            String.format("Transacao %s finalizada com sucesso. Pedido %d criado.", transacaoId, pedidoId)
        );
    }

    /**
     * Registra finalizacao com falha de uma transacao.
     * RNF0012: Log completo da operacao
     *
     * @param transacaoId ID da transacao
     * @param motivo motivo da falha
     */
    public void finalizarTransacao(String transacaoId, String motivo) {
        String dataHora = LocalDateTime.now().format(FORMATTER);

        logService.registrar(
            "FALHA_CHECKOUT",
            "Transacao",
            String.format("Transacao ID: %s", transacaoId),
            String.format("Data/Hora: %s | Status: FALHA | Motivo: %s", dataHora, motivo),
            String.format("Transacao %s falhou: %s", transacaoId, motivo)
        );
    }

    /**
     * Registra tentativa de pagamento reprovada.
     * RN0065: Controle de tentativas reprovadas
     *
     * @param transacaoId ID da transacao
     * @param carrinhoId ID do carrinho
     * @param tentativasRestantes tentativas restantes antes de bloqueio
     */
    public void registrarTentativaReprovada(String transacaoId, Long carrinhoId, int tentativasRestantes) {
        String dataHora = LocalDateTime.now().format(FORMATTER);

        logService.registrar(
            "PAGAMENTO_REPROVADO",
            "Transacao",
            String.format("Transacao ID: %s | Carrinho ID: %d", transacaoId, carrinhoId),
            String.format("Data/Hora: %s | Tentativas Restantes: %d", dataHora, tentativasRestantes),
            String.format("Pagamento reprovado na transacao %s. Restam %d tentativas.", transacaoId, tentativasRestantes)
        );
    }

    /**
     * Registra bloqueio de carrinho por tentativas reprovadas.
     * RN0065: 3 reprovacoes consecutivas bloqueiam carrinho
     *
     * @param carrinhoId ID do carrinho bloqueado
     * @param tentativas numero total de tentativas que causaram o bloqueio
     */
    public void registrarBloqueioCarrinho(Long carrinhoId, int tentativas) {
        String dataHora = LocalDateTime.now().format(FORMATTER);

        logService.registrar(
            "BLOQUEAR_CARRINHO",
            "Carrinho",
            String.format("Carrinho ID: %d", carrinhoId),
            String.format("Data/Hora: %s | Tentativas Reprovadas: %d | Status: BLOQUEADO",
                    dataHora, tentativas),
            String.format("Carrinho %d bloqueado apos %d tentativas de pagamento reprovadas",
                    carrinhoId, tentativas)
        );
    }

    /**
     * Registra validacao de pre-condicoes.
     * TASK-CHK-02: Validar pre-condicoes
     *
     * @param transacaoId ID da transacao
     * @param carrinhoId ID do carrinho
     * @param sucesso se validacao passou
     * @param motivo motivo da falha (se houver)
     */
    public void registrarValidacao(String transacaoId, Long carrinhoId, boolean sucesso, String motivo) {
        String dataHora = LocalDateTime.now().format(FORMATTER);

        logService.registrar(
            sucesso ? "VALIDACAO_SUCESSO" : "VALIDACAO_FALHA",
            "Transacao",
            String.format("Transacao ID: %s | Carrinho ID: %d", transacaoId, carrinhoId),
            sucesso
                ? String.format("Data/Hora: %s | Status: APROVADO", dataHora)
                : String.format("Data/Hora: %s | Status: REPROVADO | Motivo: %s", dataHora, motivo),
            sucesso
                ? "Pre-condicoes validadas com sucesso"
                : String.format("Validacao falhou: %s", motivo)
        );
    }

    /**
     * Registra resultado completo da transacao.
     * Usado para auditoria completa
     *
     * @param resultado resultado da transacao
     * @param carrinhoId ID do carrinho
     */
    public void registrarResultado(ResultadoCheckoutDTO resultado, Long carrinhoId) {
        String dataHora = LocalDateTime.now().format(FORMATTER);

        String statusLog = switch (resultado.getStatus()) {
            case SUCESSO -> "RESULTADO_SUCESSO";
            case PAGAMENTO_REPROVADO -> "RESULTADO_REPROVADO";
            case BLOQUEADO -> "RESULTADO_BLOQUEADO";
            case ERRO -> "RESULTADO_ERRO";
        };

        logService.registrar(
            statusLog,
            "Checkout",
            String.format("Transacao ID: %s | Carrinho ID: %d", resultado.getTransacaoId(), carrinhoId),
            String.format("Data/Hora: %s | Status: %s | Pedido ID: %s | Mensagem: %s",
                    dataHora,
                    resultado.getStatus(),
                    resultado.getPedidoId() != null ? resultado.getPedidoId().toString() : "N/A",
                    resultado.getMensagem()),
            String.format("Checkout finalizado com status: %s", resultado.getStatus())
        );
    }
}
