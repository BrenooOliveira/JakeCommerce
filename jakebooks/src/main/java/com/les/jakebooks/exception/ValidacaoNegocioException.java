package com.les.jakebooks.exception;

/**
 * Exceção lançada quando uma regra de negócio é violada.
 */
public class ValidacaoNegocioException extends RuntimeException {

    public ValidacaoNegocioException(String mensagem) {
        super(mensagem);
    }

    public ValidacaoNegocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
