package com.les.jakebooks.exception;

/**
 * Classe base para todas as exceções de negócio do sistema.
 * 
 * Todas as violações de regras de negócio (RN) devem herdar desta exceção.
 * Permite tratamento centralizado e diferenciado de erros de negócio versus erros técnicos.
 */
public class NegocioException extends RuntimeException {

    private String codigoRN;  // Código da regra de negócio violada (ex: RN0031)

    public NegocioException(String mensagem) {
        super(mensagem);
    }

    public NegocioException(String mensagem, String codigoRN) {
        super(mensagem);
        this.codigoRN = codigoRN;
    }

    public NegocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public NegocioException(String mensagem, String codigoRN, Throwable causa) {
        super(mensagem, causa);
        this.codigoRN = codigoRN;
    }

    public String getCodigoRN() {
        return codigoRN;
    }

    public void setCodigoRN(String codigoRN) {
        this.codigoRN = codigoRN;
    }

    @Override
    public String toString() {
        if (codigoRN != null) {
            return super.toString() + " [" + codigoRN + "]";
        }
        return super.toString();
    }
}
