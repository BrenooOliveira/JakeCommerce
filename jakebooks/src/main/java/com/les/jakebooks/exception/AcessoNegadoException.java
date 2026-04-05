package com.les.jakebooks.exception;

/**
 * Exceção lançada quando um usuário tenta acessar ou modificar um recurso que não pertence a ele.
 * Utilizada principalmente para garantir que clientes só acessem seus próprios dados.
 */
public class AcessoNegadoException extends ValidacaoNegocioException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
