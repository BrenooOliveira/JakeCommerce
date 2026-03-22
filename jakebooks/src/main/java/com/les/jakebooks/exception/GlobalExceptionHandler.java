package com.les.jakebooks.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.les.jakebooks.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Manipulador global de exceções para a aplicação.
 * 
 * Estratégia de tratamento:
 * 1. NegocioException: 
 *    - Se request aceita JSON: retorna JSON com HTTP 422
 *    - Se request é HTML: redireciona com mensagem de erro
 * 
 * 2. MethodArgumentNotValidException (@Valid):
 *    - Se request aceita JSON: retorna JSON com erros de validação
 *    - Se request é HTML: redireciona com erros de campo
 * 
 * 3. Exception genérica:
 *    - Se request aceita JSON: retorna JSON com HTTP 500
 *    - Se request é HTML: redireciona para página de erro
 * 
 * Regra obrigatória: Toda violação de RN lança exceção específica
 * Exceções de negócio retornam status HTTP 422 (Unprocessable Entity)
 * 
 * RNF0012: Log de transações com data, hora, usuário, operação e dados alterados.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Verifica se a requisição aceita JSON.
     * Usado para determinar se deve retornar JSON ou redirecionar para HTML.
     */
    private boolean aceitaJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * Tratamento genérico para NegocioException (classe base de todas as RN).
     * Captura: LivroNaoEncontradoException, EstoqueInsuficienteException, etc.
     *
     * Estratégia:
     * - Se aceita JSON: retorna ResponseEntity com HTTP 422
     * - Se não aceita JSON: redireciona com flash attributes (para Thymeleaf)
     */
    @ExceptionHandler(NegocioException.class)
    public Object handleNegocioException(NegocioException ex,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        
        if (aceitaJson(request)) {
            ErrorResponse erro = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                obterMensagemPorTipo(ex),
                ex.getMessage(),
                ex.getCodigoRN()
            );
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
        } else {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
            redirectAttributes.addFlashAttribute("codigoRN", ex.getCodigoRN());
            return "redirect:" + obterUrlRedirecionamento(ex);
        }
    }

    /**
     * Tratamento específico para ClienteNaoEncontradoException.
     */
    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public Object handleClienteNaoEncontrado(ClienteNaoEncontradoException ex,
                                            HttpServletRequest request,
                                            RedirectAttributes redirectAttributes) {
        
        if (aceitaJson(request)) {
            ErrorResponse erro = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Cliente não encontrado",
                ex.getMessage(),
                ex.getCodigoCliente()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
        } else {
            redirectAttributes.addFlashAttribute("erro", "Cliente não encontrado: " + ex.getCodigoCliente());
            return "redirect:/clientes";
        }
    }

    /**
     * Tratamento específico para EstoqueInsuficienteException (RN0031, RN0032).
     */
    @ExceptionHandler(EstoqueInsuficienteException.class)
    public Object handleEstoqueInsuficiente(EstoqueInsuficienteException ex,
                                           HttpServletRequest request,
                                           RedirectAttributes redirectAttributes) {
        
        if (aceitaJson(request)) {
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
        } else {
            String msg = String.format(
                "Estoque insuficiente para o livro %s. Disponível: %d | Solicitado: %d",
                ex.getCodigoLivro(),
                ex.getQuantidadeDisponivel(),
                ex.getQuantidadeSolicitada()
            );
            redirectAttributes.addFlashAttribute("erro", msg);
            return "redirect:/carrinho";
        }
    }

    /**
     * Tratamento específico para PagamentoReprovadoException (RN0037, RN0038, RN0065).
     */
    @ExceptionHandler(PagamentoReprovadoException.class)
    public Object handlePagamentoReprovado(PagamentoReprovadoException ex,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        
        if (aceitaJson(request)) {
            String detalhes = ex.getTentativasConsecutivas() != null ? 
                "Tentativas falhadas: " + ex.getTentativasConsecutivas() : "";
            
            ErrorResponse erro = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Pagamento reprovado",
                ex.getMessage(),
                detalhes
            );
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
        } else {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/carrinho/checkout";
        }
    }

    /**
     * Tratamento específico para CarrinhoExpiradoException (RN0044, RN0045).
     */
    @ExceptionHandler(CarrinhoExpiradoException.class)
    public Object handleCarrinhoExpirado(CarrinhoExpiradoException ex,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        
        if (aceitaJson(request)) {
            ErrorResponse erro = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Carrinho expirado",
                ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
        } else {
            redirectAttributes.addFlashAttribute("erro", "Seu carrinho expirou. Por favor, adicione itens novamente.");
            return "redirect:/carrinho";
        }
    }

    /**
     * Tratamento específico para TrocaNaoPermitidaException (RN0043).
     */
    @ExceptionHandler(TrocaNaoPermitidaException.class)
    public Object handleTrocaNaoPermitida(TrocaNaoPermitidaException ex,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        
        if (aceitaJson(request)) {
            ErrorResponse erro = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Troca não permitida",
                ex.getMessage(),
                ex.getMotivoRejeicao()
            );
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
        } else {
            redirectAttributes.addFlashAttribute("erro", "Troca não permitida: " + ex.getMotivoRejeicao());
            return "redirect:/pedidos";
        }
    }

    /**
     * Tratamento específico para CupomInvalidoException (RN0033, RN0035, RN0036).
     */
    @ExceptionHandler(CupomInvalidoException.class)
    public Object handleCupomInvalido(CupomInvalidoException ex,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        
        if (aceitaJson(request)) {
            ErrorResponse erro = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Cupom inválido",
                ex.getMessage(),
                ex.getMotivoInvalid()
            );
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
        } else {
            redirectAttributes.addFlashAttribute("erro", "Cupom inválido: " + ex.getMotivoInvalid());
            return "redirect:/carrinho/checkout";
        }
    }

    /**
     * Tratamento específico para LimitePedidoException (RN0063, RN0064, RN0065).
     */
    @ExceptionHandler(LimitePedidoException.class)
    public Object handleLimitePedido(LimitePedidoException ex,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        
        if (aceitaJson(request)) {
            ErrorResponse erro = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Limite de pedido violado",
                ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
        } else {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/carrinho";
        }
    }

    /**
     * Tratamento específico para ValorAbaixoDaMargemException (RN0013, RN0014).
     */
    @ExceptionHandler(ValorAbaixoDaMargemException.class)
    public Object handleValorAbaixoDaMargem(ValorAbaixoDaMargemException ex,
                                           HttpServletRequest request,
                                           RedirectAttributes redirectAttributes) {
        
        if (aceitaJson(request)) {
            ErrorResponse erro = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Valor abaixo da margem",
                ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
        } else {
            redirectAttributes.addFlashAttribute("erro", "Valor de venda abaixo da margem. Redução exige autorização.");
            return "redirect:/livros";
        }
    }

    /**
     * Tratamento específico para SenhaFracaException.
     */
    @ExceptionHandler(SenhaFracaException.class)
    public Object handleSenhaFraca(SenhaFracaException ex,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        
        if (aceitaJson(request)) {
            ErrorResponse erro = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Senha não atende aos requisitos",
                ex.getMessage(),
                ex.getMotivoRejeicao()
            );
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
        } else {
            redirectAttributes.addFlashAttribute("erro", "Senha fraca: " + ex.getMotivoRejeicao());
            return "redirect:/clientes/cadastro";
        }
    }

    /**
     * Tratamento padrão para exceções não previstas (Exception genérica).
     * Redireciona para página de erro 500.
     */
    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        
        // Log de erro para debug
        ex.printStackTrace();
        
        if (aceitaJson(request)) {
            // Para APIs REST
            ErrorResponse erro = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno da aplicação",
                "Contacte o administrador do sistema"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        } else {
            // Para requisições HTML
            redirectAttributes.addFlashAttribute("erro", "Erro interno da aplicação. Contacte o suporte.");
            return "redirect:/error/500";
        }
    }

    /**
     * Obtém mensagem de erro apropriada baseada no tipo de exceção.
     */
    private String obterMensagemPorTipo(NegocioException ex) {
        if (ex instanceof LivroNaoEncontradoException) {
            return "Livro não encontrado";
        } else if (ex instanceof EstoqueInsuficienteException) {
            return "Estoque insuficiente";
        } else if (ex instanceof ClienteNaoEncontradoException) {
            return "Cliente não encontrado";
        } else if (ex instanceof SenhaFracaException) {
            return "Senha não atende aos requisitos";
        } else if (ex instanceof CarrinhoExpiradoException) {
            return "Carrinho expirado";
        } else if (ex instanceof PagamentoReprovadoException) {
            return "Pagamento reprovado";
        } else if (ex instanceof TrocaNaoPermitidaException) {
            return "Troca não permitida";
        } else if (ex instanceof CupomInvalidoException) {
            return "Cupom inválido";
        } else if (ex instanceof LimitePedidoException) {
            return "Limite de pedido violado";
        } else if (ex instanceof ValorAbaixoDaMargemException) {
            return "Valor abaixo da margem";
        }
        return "Erro de negócio";
    }

    /**
     * Obtém URL de redirecionamento baseada no tipo de exceção.
     */
    private String obterUrlRedirecionamento(NegocioException ex) {
        if (ex instanceof LivroNaoEncontradoException) {
            return "/livros";
        } else if (ex instanceof ClienteNaoEncontradoException) {
            return "/clientes";
        } else if (ex instanceof EstoqueInsuficienteException || 
                   ex instanceof CarrinhoExpiradoException ||
                   ex instanceof LimitePedidoException) {
            return "/carrinho";
        } else if (ex instanceof PagamentoReprovadoException ||
                   ex instanceof CupomInvalidoException) {
            return "/carrinho/checkout";
        } else if (ex instanceof TrocaNaoPermitidaException) {
            return "/pedidos";
        }
        return "/";
    }

    /**
     * Constrói ErrorResponse com status HTTP e mensagens.
     */
    private ErrorResponse construirErrorResponse(HttpStatus status, String titulo, String mensagem, String campo) {
        return new ErrorResponse(status.value(), titulo, mensagem, campo);
    }
}
