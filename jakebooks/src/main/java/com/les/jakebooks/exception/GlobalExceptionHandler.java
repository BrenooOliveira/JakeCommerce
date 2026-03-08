package com.les.jakebooks.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.les.jakebooks.dto.ErrorResponse;

/**
 * Manipulador global de exceções para a aplicação.
 * 
 * Regra obrigatória: Toda violação de RN lança exceção específica
 * Exceções de negócio retornam status HTTP 422 (Unprocessable Entity)
 * 
 * RNF0012: Log de transações com data, hora, usuário, operação e dados alterados.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Tratamento para RecursoNaoEncontradoException.
     * Retorna HTTP 404 (Not Found).
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        ErrorResponse erro = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Recurso não encontrado",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    /**
     * Tratamento para ValidacaoNegocioException.
     * Retorna HTTP 422 (Unprocessable Entity).
     */
    @ExceptionHandler(ValidacaoNegocioException.class)
    public ResponseEntity<ErrorResponse> handleValidacaoNegocio(ValidacaoNegocioException ex) {
        ErrorResponse erro = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Violação de regra de negócio",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
    }

    /**
     * Tratamento para EstoqueInsuficienteException (RN0031, RN0032).
     * Retorna HTTP 422 (Unprocessable Entity).
     */
    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleEstoqueInsuficiente(EstoqueInsuficienteException ex) {
        String detalhes = String.format(
            "Livro: %s | Solicitado: %d | Disponível: %d",
            ex.getCodigoLivro(), 
            ex.getQuantidadeSolicitada(), 
            ex.getQuantidadeDisponivel()
        );
        ErrorResponse erro = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Estoque insuficiente",
            ex.getMessage(),
            detalhes
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
    }

    /**
     * Tratamento para PagamentoReprovadoException (RN0037, RN0038, RN0065).
     * Retorna HTTP 422 (Unprocessable Entity).
     */
    @ExceptionHandler(PagamentoReprovadoException.class)
    public ResponseEntity<ErrorResponse> handlePagamentoReprovado(PagamentoReprovadoException ex) {
        String detalhes = ex.getTentativasConsecutivas() != null ? 
            "Tentativas consecutivas falhadas: " + ex.getTentativasConsecutivas() : "";
        
        ErrorResponse erro = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Pagamento reprovado",
            ex.getMessage(),
            detalhes
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
    }

    /**
     * Tratamento para CarrinhoExpiradoException (RN0044, RN0045).
     * Retorna HTTP 422 (Unprocessable Entity).
     */
    @ExceptionHandler(CarrinhoExpiradoException.class)
    public ResponseEntity<ErrorResponse> handleCarrinhoExpirado(CarrinhoExpiradoException ex) {
        ErrorResponse erro = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Carrinho expirado",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
    }

    /**
     * Tratamento para ClienteBloqueadoException.
     * Retorna HTTP 403 (Forbidden).
     */
    @ExceptionHandler(ClienteBloqueadoException.class)
    public ResponseEntity<ErrorResponse> handleClienteBloqueado(ClienteBloqueadoException ex) {
        ErrorResponse erro = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Operação não permitida - Cliente bloqueado",
            ex.getMessage(),
            ex.getMotivo()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }

    /**
     * Tratamento para TrocaNaoPermitidaException (RN0043).
     * Retorna HTTP 422 (Unprocessable Entity).
     */
    @ExceptionHandler(TrocaNaoPermitidaException.class)
    public ResponseEntity<ErrorResponse> handleTrocaNaoPermitida(TrocaNaoPermitidaException ex) {
        ErrorResponse erro = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Troca não permitida",
            ex.getMessage(),
            ex.getMotivoRejeicao()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
    }

    /**
     * Tratamento para SenhaInseguraException.
     * Retorna HTTP 422 (Unprocessable Entity).
     */
    @ExceptionHandler(SenhaInseguraException.class)
    public ResponseEntity<ErrorResponse> handleSenhaInsegura(SenhaInseguraException ex) {
        ErrorResponse erro = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Senha não atende aos requisitos de segurança",
            ex.getMessage(),
            "Mínimo 8 caracteres, com maiúsculas, minúsculas e caracteres especiais"
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
    }

    /**
     * Tratamento padrão para exceções não previstas.
     * Retorna HTTP 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse erro = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Erro interno da aplicação",
            "Contacte o administrador do sistema"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
