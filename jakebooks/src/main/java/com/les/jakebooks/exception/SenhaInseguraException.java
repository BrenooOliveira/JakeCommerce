package com.les.jakebooks.exception;

/**
 * Exceção lançada quando a senha não atende aos requisitos de segurança.
 * Corresponde ao requisito de senha forte (mínimo 8 caracteres, maiúsculas, minúsculas e especiais).
 */
public class SenhaInseguraException extends RuntimeException {

    public SenhaInseguraException(String mensagem) {
        super(mensagem);
    }

    public SenhaInseguraException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
