package com.les.jakebooks.controller;

import com.les.jakebooks.dto.DadosGraficoDTO;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.services.AnaliseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller responsável pelas análises de vendas.
 * Segue padrão Frontend: sem lógica de negócio, apenas chamadas a Services.
 * RF0055: Analisar histórico por período comparando produtos ou categorias.
 * RNF0055: Exibição em gráfico de linhas.
 */
@Controller
@RequestMapping("/analise")
public class AnaliseController {

    @Autowired
    private AnaliseService analiseService;

    /**
     * Exibe dashboard de análise com formulário de filtros.
     * GET /analise
     * RF0055: Analisar histórico por período.
     *
     * @param model Model para adicionar atributos à view
     * @return view name "analise/dashboard"
     */
    @GetMapping
    public String dashboard(Model model) {
        // Dados padrão: última 30 dias
        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusDays(30);

        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("agrupamento", "PRODUTO");
        model.addAttribute("isAdmin", true);

        return "analise/dashboard";
    }

    /**
     * Retorna dados em JSON para renderizar no gráfico via Chart.js.
     * GET /analise/dados
     * RF0055: Analisar histórico por período.
     * RNF0055: Exibição em gráfico de linhas.
     *
     * @param dataInicio   data de início (formato: yyyy-MM-dd)
     * @param dataFim      data de fim (formato: yyyy-MM-dd)
     * @param agrupamento  tipo de agrupamento: "PRODUTO" ou "CATEGORIA"
     * @return ResponseEntity com lista de DadosGraficoDTO ou erro
     */
    @GetMapping("/dados")
    @ResponseBody
    public ResponseEntity<?> obterDados(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam String agrupamento) {

        try {
            // Validar parâmetros
            if (dataInicio == null || dataFim == null) {
                return ResponseEntity.badRequest().body("Datas são obrigatórias");
            }

            if (agrupamento == null || agrupamento.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Agrupamento é obrigatório");
            }

            // Buscar dados
            List<DadosGraficoDTO> dados = analiseService.analisarVendasPorPeriodo(
                    dataInicio, dataFim, agrupamento);

            // Retornar JSON
            return ResponseEntity.ok(dados);
        } catch (ValidacaoNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao processar análise: " + e.getMessage());
        }
    }
}
