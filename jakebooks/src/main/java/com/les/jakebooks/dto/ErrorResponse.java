package com.les.jakebooks.dto;

import java.time.LocalDateTime;

/**
 * DTO para resposta padrão de erro.
 * Usado pelo GlobalExceptionHandler para retornar erros em formato consistente.
 */
public class ErrorResponse {

    private LocalDateTime timestamp;
    private Integer status;
    private String mensagem;
    private String detalhes;
    private String campo;

    public ErrorResponse(Integer status, String mensagem) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.mensagem = mensagem;
    }

    public ErrorResponse(Integer status, String mensagem, String detalhes) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.mensagem = mensagem;
        this.detalhes = detalhes;
    }

    public ErrorResponse(Integer status, String mensagem, String detalhes, String campo) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.mensagem = mensagem;
        this.detalhes = detalhes;
        this.campo = campo;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }
}
