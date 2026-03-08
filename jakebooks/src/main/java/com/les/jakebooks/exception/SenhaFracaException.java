package com.les.jakebooks.exception;

/**
 * Exceção lançada quando a senha não atende aos requisitos de segurança.
 * 
 * Código RN: Cadastro de Cliente
 * Requisito: Senha forte (mínimo 8 caracteres, maiúsculas, minúsculas e especiais)
 */
public class SenhaFracaException extends NegocioException {

    private String motivoRejeicao;

    public SenhaFracaException(String mensagem) {
        super(mensagem, "RN_SENHA_FRACA");
    }

    public SenhaFracaException(String mensagem, String motivoRejeicao) {
        super(mensagem, "RN_SENHA_FRACA");
        this.motivoRejeicao = motivoRejeicao;
    }

    public SenhaFracaException(String mensagem, String motivoRejeicao, Throwable causa) {
        super(mensagem, "RN_SENHA_FRACA", causa);
        this.motivoRejeicao = motivoRejeicao;
    }

    public String getMotivoRejeicao() {
        return motivoRejeicao;
    }
}
