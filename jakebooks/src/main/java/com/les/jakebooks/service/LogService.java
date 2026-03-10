package com.les.jakebooks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.les.jakebooks.domain.LogTransacao;
import com.les.jakebooks.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Serviço para registrar e consultar logs de transações do sistema.
 * 
 * RNF0012: Log de transações com data, hora, usuário, operação e dados alterados.
 * 
 * Responsabilidades:
 * - Registrar operações (CREATE, UPDATE, DELETE, PAYMENT, etc)
 * - Capturar usuário autenticado via SecurityContextHolder
 * - Converter objetos para JSON
 * - Fornecer métodos de consulta de auditoria
 * - Tratar erros sem interromper fluxo principal
 */
@Service
public class LogService {

    private static final Logger logger = Logger.getLogger(LogService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LogRepository logRepository;

    /**
     * Registra uma transação no banco de dados.
     * Captura automaticamente o usuário autenticado.
     * 
     * @param operacao tipo de operação (CRIAR, ALTERAR, DELETAR, PAGAR)
     * @param entidade nome da entidade afetada (Cliente, Livro, Pedido)
     * @param dadosAnteriores estado anterior (null para criações)
     * @param dadosNovos estado novo
     */
    @Transactional
    public void registrar(String operacao, String entidade, Object dadosAnteriores, Object dadosNovos) {
        try {
            String usuario = obterUsuarioAutenticado();
            LocalDateTime dataHora = LocalDateTime.now();

            String dadosAnterioresJson = convertToJson(dadosAnteriores);
            String dadosNovosJson = convertToJson(dadosNovos);

            LogTransacao log = new LogTransacao(
                dataHora,
                usuario,
                operacao,
                entidade,
                dadosAnterioresJson,
                dadosNovosJson
            );

            logRepository.save(log);
            logger.info(String.format("Log registrado: %s | %s | %s", usuario, operacao, entidade));

        } catch (Exception e) {
            // Não interromper o fluxo principal se o log falhar
            logger.warning("Erro ao registrar log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Registra uma transação com descrição adicional.
     * 
     * @param operacao tipo de operação
     * @param entidade nome da entidade
     * @param dadosAnteriores estado anterior
     * @param dadosNovos estado novo
     * @param descricao descrição adicional
     */
    @Transactional
    public void registrar(String operacao, String entidade, Object dadosAnteriores, 
                         Object dadosNovos, String descricao) {
        try {
            String usuario = obterUsuarioAutenticado();
            LocalDateTime dataHora = LocalDateTime.now();

            String dadosAnterioresJson = convertToJson(dadosAnteriores);
            String dadosNovosJson = convertToJson(dadosNovos);

            LogTransacao log = new LogTransacao(
                dataHora,
                usuario,
                operacao,
                entidade,
                dadosAnterioresJson,
                dadosNovosJson
            );
            log.setDescricao(descricao);

            logRepository.save(log);
            logger.info(String.format("Log registrado: %s | %s | %s | %s", 
                usuario, operacao, entidade, descricao));

        } catch (Exception e) {
            logger.warning("Erro ao registrar log: " + e.getMessage());
        }
    }

    /**
     * Obtém o email do usuário autenticado no SecurityContext.
     * Retorna "SISTEMA" para operações automáticas (sem usuário autenticado).
     * 
     * @return email do usuário ou "SISTEMA"
     */
    private String obterUsuarioAutenticado() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                return auth.getName();  // email do cliente
            }
        } catch (Exception e) {
            logger.warning("Erro ao obter usuário autenticado: " + e.getMessage());
        }
        return "SISTEMA";
    }

    /**
     * Converte um objeto para JSON string.
     * Retorna null se o objeto for null.
     * 
     * @param objeto objeto a converter
     * @return JSON string ou null
     */
    private String convertToJson(Object objeto) {
        if (objeto == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (Exception e) {
            logger.warning("Erro ao converter objeto para JSON: " + e.getMessage());
            return objeto.toString();
        }
    }

    // === Métodos de Consulta ===

    /**
     * Busca logs de um usuário específico.
     * 
     * @param usuario email do usuário
     * @return lista de transações ordenadas por data descrescente
     */
    public List<LogTransacao> buscarPorUsuario(String usuario) {
        return logRepository.findByUsuarioOrderByDataHoraDesc(usuario);
    }

    /**
     * Busca logs de uma entidade específica.
     * Útil para ver histórico completo de um cliente, livro, pedido, etc.
     * 
     * @param entidade nome da entidade
     * @return lista de transações
     */
    public List<LogTransacao> buscarPorEntidade(String entidade) {
        return logRepository.findByEntidadeOrderByDataHoraDesc(entidade);
    }

    /**
     * Busca logs de uma operação específica.
     * 
     * @param operacao tipo de operação (CRIAR, ALTERAR, DELETAR, PAGAR)
     * @return lista de transações
     */
    public List<LogTransacao> buscarPorOperacao(String operacao) {
        return logRepository.findByOperacaoOrderByDataHoraDesc(operacao);
    }

    /**
     * Busca logs em um período de tempo.
     * 
     * @param dataInicio data/hora inicial
     * @param dataFim data/hora final
     * @return lista de transações no período
     */
    public List<LogTransacao> buscarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return logRepository.findByPeriodo(dataInicio, dataFim);
    }

    /**
     * Busca logs de um usuário em um período específico.
     * 
     * @param usuario email do usuário
     * @param dataInicio data/hora inicial
     * @param dataFim data/hora final
     * @return lista de transações
     */
    public List<LogTransacao> buscarPorUsuarioAndPeriodo(String usuario, LocalDateTime dataInicio, LocalDateTime dataFim) {
        return logRepository.findByUsuarioAndPeriodo(usuario, dataInicio, dataFim);
    }

    /**
     * Busca logs de uma entidade em um período específico.
     * 
     * @param entidade nome da entidade
     * @param dataInicio data/hora inicial
     * @param dataFim data/hora final
     * @return lista de transações
     */
    public List<LogTransacao> buscarPorEntidadeAndPeriodo(String entidade, LocalDateTime dataInicio, LocalDateTime dataFim) {
        return logRepository.findByEntidadeAndPeriodo(entidade, dataInicio, dataFim);
    }

    /**
     * Busca logs de um usuário para uma operação específica.
     * 
     * @param usuario email do usuário
     * @param operacao tipo de operação
     * @return lista de transações
     */
    public List<LogTransacao> buscarPorUsuarioAndOperacao(String usuario, String operacao) {
        return logRepository.findByUsuarioAndOperacaoOrderByDataHoraDesc(usuario, operacao);
    }

    /**
     * Conta transações em um período.
     * 
     * @param dataInicio data/hora inicial
     * @param dataFim data/hora final
     * @return número de transações
     */
    public Long contarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return logRepository.countByPeriodo(dataInicio, dataFim);
    }
}
