package com.les.jakebooks.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interceptor para log de transações.
 * 
 * RNF0012: Log de transações com data, hora, usuário, operação e dados alterados.
 * 
 * Este interceptor registra todas as requisições HTTP, incluindo:
 * - Data e hora da operação
 * - Usuário (quando autenticado)
 * - Operação (método HTTP e rota)
 * - Dados enviados (quando aplicável)
 * - Status da resposta
 * - Tempo de processamento
 */
@Component
public class TransacaoInterceptor implements HandlerInterceptor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String TEMPO_INICIO = "tempo_inicio";

    /**
     * Executado antes da requisição ser processada.
     * Registra o início da transação.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        LocalDateTime dataHora = LocalDateTime.now();
        String usuario = obterUsuario(request);
        String metodo = request.getMethod();
        String rota = request.getRequestURI();
        String parametros = obterParametros(request);

        // Armazenar tempo de início para calcular duração
        request.setAttribute(TEMPO_INICIO, System.currentTimeMillis());

        logOperacao(dataHora, usuario, metodo, rota, parametros, "INICIADA");

        return true;
    }

    /**
     * Executado após a resposta ser preparada.
     * Registra o fim da transação.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) throws Exception {
        
        LocalDateTime dataHora = LocalDateTime.now();
        String usuario = obterUsuario(request);
        String metodo = request.getMethod();
        String rota = request.getRequestURI();
        int statusCode = response.getStatus();
        long tempoInicio = (Long) request.getAttribute(TEMPO_INICIO);
        long duracao = System.currentTimeMillis() - tempoInicio;

        String status = ex != null ? "ERRO" : "CONCLUÍDA";
        
        logOperacao(dataHora, usuario, metodo, rota, 
                   "Status: " + statusCode + " | Duração: " + duracao + "ms", status);

        if (ex != null) {
            logErro(dataHora, usuario, metodo, rota, ex);
        }
    }

    /**
     * Obtém o usuário autenticado da requisição.
     * 
     * @param request requisição HTTP
     * @return usuario ou "ANONIMO" se não autenticado
     */
    private String obterUsuario(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }
        return "ANONIMO";
    }

    /**
     * Obtém os parâmetros da requisição.
     * Exclui senhas e dados sensíveis.
     * 
     * @param request requisição HTTP
     * @return string com parâmetros
     */
    private String obterParametros(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String contentType = request.getContentType();
        
        if (queryString != null && !queryString.isEmpty()) {
            // Remover parâmetros sensíveis
            queryString = queryString.replaceAll("(?i)senha=[^&]*", "senha=***");
            queryString = queryString.replaceAll("(?i)password=[^&]*", "password=***");
            return queryString;
        }
        
        if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
            return "Dados de formulário";
        }
        
        return "Sem parâmetros";
    }

    /**
     * Registra a operação no log.
     * 
     * @param dataHora data/hora da operação
     * @param usuario usuário que realizou a operação
     * @param metodo método HTTP
     * @param rota rota acessada
     * @param dados dados adicionais
     * @param status status da operação
     */
    private void logOperacao(LocalDateTime dataHora, String usuario, String metodo, 
                            String rota, String dados, String status) {
        String mensagem = String.format(
            "[%s] TRANSACAO %s | Usuario: %s | Método: %s | Rota: %s | Dados: %s",
            dataHora.format(FORMATTER),
            status,
            usuario,
            metodo,
            rota,
            dados
        );
        System.out.println(mensagem);
    }

    /**
     * Registra erros de operação no log.
     * 
     * @param dataHora data/hora da operação
     * @param usuario usuário que realizou a operação
     * @param metodo método HTTP
     * @param rota rota acessada
     * @param ex exceção ocorrida
     */
    private void logErro(LocalDateTime dataHora, String usuario, String metodo, 
                        String rota, Exception ex) {
        String mensagem = String.format(
            "[%s] ERRO NA TRANSACAO | Usuario: %s | Método: %s | Rota: %s | Erro: %s",
            dataHora.format(FORMATTER),
            usuario,
            metodo,
            rota,
            ex.getMessage()
        );
        System.err.println(mensagem);
        ex.printStackTrace();
    }
}
