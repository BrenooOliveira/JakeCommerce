package com.les.jakebooks.controller;

import com.les.jakebooks.domain.Carrinho;
import com.les.jakebooks.domain.Cliente;
import com.les.jakebooks.domain.Endereco;
import com.les.jakebooks.domain.Pagamento;
import com.les.jakebooks.domain.Pedido;
import com.les.jakebooks.dto.CarrinhoDTO;
import com.les.jakebooks.dto.CheckoutDTO;
import com.les.jakebooks.dto.CupomAplicadoDTO;
import com.les.jakebooks.dto.CupomDTO;
import com.les.jakebooks.dto.EnderecoDTO;
import com.les.jakebooks.dto.FreteDTO;
import com.les.jakebooks.dto.OpcoesPagamentoDTO;
import com.les.jakebooks.dto.PagamentoFormDTO;
import com.les.jakebooks.dto.ProcessarPagamentoDTO;
import com.les.jakebooks.dto.ResultadoCompraDTO;
import com.les.jakebooks.dto.ResultadoPagamentoDTO;
import com.les.jakebooks.dto.SelecaoPagamentoDTO;
import com.les.jakebooks.exception.AcessoNegadoException;
import com.les.jakebooks.exception.CarrinhoBloqueadoPagamentoException;
import com.les.jakebooks.exception.CarrinhoExpiradoException;
import com.les.jakebooks.exception.CarrinhoNaoEncontradoException;
import com.les.jakebooks.exception.CarrinhoVazioException;
import com.les.jakebooks.exception.CupomInvalidoException;
import com.les.jakebooks.exception.CupomJaUtilizadoException;
import com.les.jakebooks.exception.CupomNaoEncontradoException;
import com.les.jakebooks.exception.CupomPromocionalDuplicadoException;
import com.les.jakebooks.exception.EnderecoEntregaNaoEncontradoException;
import com.les.jakebooks.exception.EstoqueInsuficienteException;
import com.les.jakebooks.exception.EstoqueNaoEncontradoException;
import com.les.jakebooks.exception.LimiteItensExcedidoException;
import com.les.jakebooks.exception.LivroInativoException;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.exception.ValorMinimoCartaoException;
import com.les.jakebooks.exception.ValorPagamentoInsuficienteException;
import com.les.jakebooks.model.enums.StatusCarrinho;
import com.les.jakebooks.model.enums.StatusPagamento;
import com.les.jakebooks.repository.CarrinhoRepository;
import com.les.jakebooks.repository.ClienteRepository;
import com.les.jakebooks.repository.EnderecoRepository;
import com.les.jakebooks.repository.PagamentoRepository;
import com.les.jakebooks.repository.PedidoRepository;
import com.les.jakebooks.services.CarrinhoService;
import com.les.jakebooks.services.CompraService;
import com.les.jakebooks.services.CupomService;
import com.les.jakebooks.services.EnderecoService;
import com.les.jakebooks.services.FreteService;
import com.les.jakebooks.services.PagamentoService;
import com.les.jakebooks.util.SecurityUtil;
import com.les.jakebooks.validator.CompraValidator;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller responsável pelo fluxo de checkout.
 * Segue padrão Frontend: sem lógica de negócio, apenas chamadas a Services.
 * RF0033: Realizar compra
 * RF0035: Selecionar endereço de entrega
 * RF0036: Selecionar pagamento (cartão, cupom promocional, cupom de troca)
 */
@Controller
@RequestMapping("/checkout")
@PreAuthorize("isAuthenticated()")
public class CheckoutController {

    @Autowired
    private EnderecoService enderecoService;

    @Autowired
    private FreteService freteService;

    @Autowired
    private CarrinhoService carrinhoService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private CupomService cupomService;

    @Autowired
    private CompraValidator compraValidator;

    @Autowired
    private CompraService compraService;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    /**
     * Exibe tela de seleção de endereço de entrega.
     * GET /checkout/endereco
     * RF0035: Selecionar endereço de entrega
     * RN0022: Cliente deve ter pelo menos um endereço de entrega
     *
     * @param model Model para adicionar atributos à view
     * @return view name "checkout/endereco"
     */
    @GetMapping("/endereco")
    public String exibirSelecaoEndereco(Model model) {
        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // CHK-02: Validar pré-condições do carrinho antes de iniciar checkout
        Optional<Carrinho> carrinhoOpt = carrinhoRepository.findByClienteIdAndStatusEquals(
                cliente.getId(), StatusCarrinho.ABERTO);

        if (carrinhoOpt.isPresent()) {
            Carrinho carrinho = carrinhoOpt.get();
            compraValidator.validarCarrinhoParaCheckout(carrinho);
        }

        try {
            // Buscar endereços de entrega do cliente
            List<EnderecoDTO> enderecos = enderecoService.listarEnderecosEntrega(cliente.getId());
            model.addAttribute("enderecos", enderecos);
            model.addAttribute("cliente", cliente);
            return "checkout/endereco";

        } catch (EnderecoEntregaNaoEncontradoException e) {
            // Cliente não tem endereços de entrega cadastrados
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("redirecionarCadastro", true);
            return "checkout/endereco";
        }
    }

    /**
     * Processa seleção de endereço de entrega.
     * POST /checkout/endereco
     * RF0035: Selecionar endereço de entrega
     *
     * Valida endereço e armazena na sessão do checkout.
     * Redireciona para cálculo de frete (TASK-SHP-03).
     *
     * @param enderecoId ID do endereço selecionado
     * @param session HttpSession para armazenar dados do checkout
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @return redirect para /checkout/frete ou /checkout/endereco em caso de erro
     */
    @PostMapping("/endereco")
    public String selecionarEndereco(
            @RequestParam Long enderecoId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        try {
            // Validar e selecionar endereço
            EnderecoDTO endereco = enderecoService.selecionarEnderecoEntrega(
                    cliente.getId(), enderecoId
            );

            // Obter ou criar CheckoutDTO na sessão
            CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");
            if (checkout == null) {
                checkout = new CheckoutDTO();
            }

            // Armazenar endereço selecionado
            checkout.setEnderecoEntregaId(enderecoId);
            session.setAttribute("checkout", checkout);

            // Adicionar mensagem de sucesso
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Endereço de entrega selecionado com sucesso!");

            // Redirecionar para cálculo de frete (TASK-SHP-03)
            return "redirect:/checkout/frete";

        } catch (RecursoNaoEncontradoException | AcessoNegadoException | EnderecoEntregaNaoEncontradoException e) {
            // Adicionar mensagem de erro e retornar para seleção
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/checkout/endereco";
        }
    }

    /**
     * Exibe tela de cálculo e confirmação do frete.
     * GET /checkout/frete
     * RF0034: Calcular frete
     * RN0064: Pedido mínimo R$20 para frete grátis
     *
     * @param session HttpSession para recuperar dados do checkout
     * @param model Model para adicionar atributos à view
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @return view name "checkout/frete" ou redirect se endereço não selecionado
     */
    @GetMapping("/frete")
    public String exibirFrete(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Obter checkout da sessão
        CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");

        // Validar se endereço foi selecionado
        if (checkout == null || checkout.getEnderecoEntregaId() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Selecione um endereço de entrega primeiro");
            return "redirect:/checkout/endereco";
        }

        try {
            // Buscar endereço selecionado
            EnderecoDTO endereco = enderecoService.selecionarEnderecoEntrega(
                    cliente.getId(), checkout.getEnderecoEntregaId()
            );

            // Buscar carrinho do cliente
            CarrinhoDTO carrinho = carrinhoService.obterOuCriar(cliente.getCodigo());
            BigDecimal valorCarrinho = carrinho.valorTotal();

            // Calcular frete
            FreteDTO frete = freteService.calcularFrete(
                    checkout.getEnderecoEntregaId(),
                    valorCarrinho
            );

            // Armazenar frete na sessão
            checkout.setFrete(frete);
            session.setAttribute("checkout", checkout);

            // Calcular valor total (carrinho + frete)
            BigDecimal valorTotal = valorCarrinho.add(frete.getValor());

            // Adicionar dados ao modelo
            model.addAttribute("endereco", endereco);
            model.addAttribute("frete", frete);
            model.addAttribute("valorCarrinho", valorCarrinho);
            model.addAttribute("valorTotal", valorTotal);

            return "checkout/frete";

        } catch (RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/checkout/endereco";
        }
    }

    /**
     * Confirma o frete e prossegue para pagamento.
     * POST /checkout/frete
     * RF0034: Calcular frete
     *
     * @param session HttpSession para validar dados do checkout
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @return redirect para /checkout/pagamento ou /checkout/frete se frete não calculado
     */
    @PostMapping("/frete")
    public String confirmarFrete(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Obter checkout da sessão
        CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");

        // Validar se frete foi calculado
        if (checkout == null || checkout.getFrete() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Frete não foi calculado. Tente novamente.");
            return "redirect:/checkout/frete";
        }

        // Frete já calculado, prosseguir para pagamento
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Frete confirmado! Selecione a forma de pagamento.");

        return "redirect:/checkout/pagamento";
    }

    /**
     * Exibe tela de seleção de pagamento.
     * GET /checkout/pagamento
     * RF0036: Selecionar pagamento (cartão, cupom promocional, cupom de troca)
     * RN0033: Apenas um cupom promocional por compra
     * RN0034: Múltiplos cartões permitidos (mínimo R$10 por cartão)
     * RN0035: Consumir cupons antes do cartão
     *
     * @param session HttpSession para recuperar dados do checkout
     * @param model Model para adicionar atributos à view
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @return view name "checkout/pagamento" ou redirect se endereço/frete não selecionados
     */
    @GetMapping("/pagamento")
    public String exibirPagamento(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Obter checkout da sessão
        CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");

        // Validar se frete foi calculado
        if (checkout == null || !checkout.isProntoParaPagamento()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Complete as etapas anteriores do checkout.");
            return "redirect:/checkout/endereco";
        }

        try {
            // Verificar se cliente está bloqueado por pagamentos reprovados
            pagamentoService.verificarBloqueio(cliente.getId());

            // Buscar carrinho do cliente
            CarrinhoDTO carrinho = carrinhoService.obterOuCriar(cliente.getCodigo());
            BigDecimal valorProdutos = carrinho.valorTotal();
            BigDecimal valorFrete = checkout.getFrete().getValor();
            BigDecimal valorTotal = valorProdutos.add(valorFrete);

            // Armazenar valores no checkout - PAY-06
            checkout.setCarrinhoId(carrinho.id());
            checkout.setValorProdutos(valorProdutos);
            checkout.setValorTotal(valorTotal);
            session.setAttribute("checkout", checkout);

            // Montar opções de pagamento
            OpcoesPagamentoDTO opcoes = pagamentoService.montarOpcoesPagamento(
                    cliente, valorProdutos, valorFrete);

            // Buscar endereço selecionado
            EnderecoDTO endereco = enderecoService.selecionarEnderecoEntrega(
                    cliente.getId(), checkout.getEnderecoEntregaId());

            // Adicionar dados ao modelo
            model.addAttribute("opcoes", opcoes);
            model.addAttribute("endereco", endereco);
            model.addAttribute("frete", checkout.getFrete());
            model.addAttribute("valorProdutos", valorProdutos);
            model.addAttribute("valorFrete", valorFrete);
            model.addAttribute("valorTotal", valorTotal);
            model.addAttribute("carrinho", carrinho);

            // Inicializar seleção de pagamento se não existir
            if (checkout.getSelecaoPagamento() == null) {
                model.addAttribute("selecao", new SelecaoPagamentoDTO());
            } else {
                model.addAttribute("selecao", checkout.getSelecaoPagamento());
            }

            return "checkout/pagamento";

        } catch (CarrinhoBloqueadoPagamentoException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/carrinho/view";

        } catch (RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/checkout/endereco";
        }
    }

    /**
     * Processa seleção de pagamento e redireciona para confirmação.
     * POST /checkout/pagamento
     * RF0036: Selecionar pagamento
     *
     * @param selecao dados da seleção de pagamento
     * @param session HttpSession para armazenar dados
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @return redirect para /checkout/confirmar ou /checkout/pagamento em caso de erro
     */
    @PostMapping("/pagamento")
    public String processarPagamento(
            @ModelAttribute SelecaoPagamentoDTO selecao,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Obter checkout da sessão
        CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");

        if (checkout == null || checkout.getValorTotal() == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Sessão expirada. Reinicie o checkout.");
            return "redirect:/checkout/endereco";
        }

        try {
            // Validar seleção de pagamento
            pagamentoService.validarSelecaoPagamento(
                    selecao, checkout.getValorTotal(), cliente.getId());

            // Armazenar seleção no checkout
            checkout.setSelecaoPagamento(selecao);

            // Se tem cupom promocional, validar e armazenar
            if (selecao.temCupomPromocional()) {
                CupomDTO cupom = cupomService.validarCupomPromocional(
                        selecao.getCodigoCupomPromocional());
                checkout.setCupomPromocionalValidado(cupom);
            }

            session.setAttribute("checkout", checkout);

            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Forma de pagamento selecionada! Confirme seu pedido.");

            // Redirecionar para confirmação (próxima task)
            return "redirect:/checkout/confirmar";

        } catch (CupomInvalidoException | CupomPromocionalDuplicadoException e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Cupom inválido: " + e.getMessage());
            return "redirect:/checkout/pagamento";

        } catch (ValorMinimoCartaoException e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Valor mínimo por cartão é R$ 10,00.");
            return "redirect:/checkout/pagamento";

        } catch (ValorPagamentoInsuficienteException e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Valor de pagamento insuficiente. " + e.getMessage());
            return "redirect:/checkout/pagamento";

        } catch (ValidacaoNegocioException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/checkout/pagamento";
        }
    }

    /**
     * Valida cupom promocional via AJAX.
     * POST /checkout/validar-cupom
     * RN0033: Apenas um cupom promocional por compra
     *
     * @param codigo código do cupom
     * @return ResponseEntity com CupomDTO ou erro
     */
    @PostMapping("/validar-cupom")
    @ResponseBody
    public ResponseEntity<?> validarCupomPromocional(@RequestParam String codigo) {
        try {
            CupomDTO cupom = cupomService.validarCupomPromocional(codigo);
            return ResponseEntity.ok(cupom);

        } catch (CupomInvalidoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", e.getMotivoInvalid());
            return ResponseEntity.badRequest().body(erro);
        }
    }

    /**
     * Calcula valor restante após aplicação de cupons via AJAX.
     * POST /checkout/calcular-restante
     * RN0035: Consumir cupons antes do cartão
     *
     * @param requestBody dados da requisição com cuponsIds e codigoCupomPromocional
     * @param session HttpSession para recuperar valor total
     * @return ResponseEntity com valor restante
     */
    @PostMapping("/calcular-restante")
    @ResponseBody
    public ResponseEntity<?> calcularValorRestante(
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {

        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Obter checkout da sessão
        CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");

        if (checkout == null || checkout.getValorTotal() == null) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Sessão expirada");
            return ResponseEntity.badRequest().body(erro);
        }

        try {
            @SuppressWarnings("unchecked")
            List<Long> cuponsIds = requestBody.get("cuponsIds") != null
                    ? ((List<Integer>) requestBody.get("cuponsIds")).stream()
                            .map(Integer::longValue)
                            .toList()
                    : null;

            String codigoCupomPromocional = (String) requestBody.get("codigoCupomPromocional");

            BigDecimal valorRestante = pagamentoService.calcularValorRestante(
                    checkout.getValorTotal(),
                    cuponsIds,
                    codigoCupomPromocional,
                    cliente.getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("valorRestante", valorRestante);
            response.put("precisaCartao", valorRestante.compareTo(BigDecimal.ZERO) > 0);
            response.put("temExcedente", valorRestante.compareTo(BigDecimal.ZERO) < 0);

            if (valorRestante.compareTo(BigDecimal.ZERO) < 0) {
                response.put("valorExcedente", valorRestante.negate());
            }

            return ResponseEntity.ok(response);

        } catch (CupomInvalidoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(erro);
        }
    }

    /**
     * Aplica cupom promocional na sessao via AJAX.
     * POST /checkout/pagamento/aplicar-cupom
     * RN0033: Apenas um cupom promocional por compra
     *
     * @param codigo codigo do cupom promocional
     * @param session HttpSession para armazenar cupom aplicado
     * @return ResponseEntity com sucesso ou erro
     */
    @PostMapping("/pagamento/aplicar-cupom")
    @ResponseBody
    public ResponseEntity<?> aplicarCupomPromocional(
            @RequestParam String codigo,
            HttpSession session) {

        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente nao encontrado"));

        // Obter checkout da sessao
        CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");

        if (checkout == null) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Sessao expirada");
            return ResponseEntity.badRequest().body(erro);
        }

        // Verificar se ja tem cupom promocional aplicado (RN0033)
        if (checkout.getCupomPromocionalValidado() != null) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Ja existe um cupom promocional aplicado");
            erro.put("motivo", "DUPLICADO");
            return ResponseEntity.badRequest().body(erro);
        }

        try {
            // Validar e aplicar cupom promocional
            CupomDTO cupom = cupomService.validarCupomPromocional(codigo);

            // Armazenar na sessao
            checkout.setCupomPromocionalValidado(cupom);

            // Inicializar selecaoPagamento se necessario
            if (checkout.getSelecaoPagamento() == null) {
                checkout.setSelecaoPagamento(new SelecaoPagamentoDTO());
            }
            checkout.getSelecaoPagamento().setCodigoCupomPromocional(codigo);

            session.setAttribute("checkout", checkout);

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("cupom", cupom);
            response.put("mensagem", "Cupom aplicado com sucesso!");

            return ResponseEntity.ok(response);

        } catch (CupomInvalidoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", e.getMotivoInvalid());
            return ResponseEntity.badRequest().body(erro);

        } catch (CupomJaUtilizadoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", "JA_UTILIZADO");
            return ResponseEntity.badRequest().body(erro);

        } catch (CupomNaoEncontradoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", "NAO_ENCONTRADO");
            return ResponseEntity.badRequest().body(erro);
        }
    }

    /**
     * Aplica cupons de troca e promocional ao pagamento via AJAX.
     * POST /checkout/aplicar-cupons
     * RN0033: Apenas um cupom promocional por compra
     * RN0035: Consumir cupons antes do cartao
     *
     * @param requestBody dados com cuponsTrocaIds e codigoPromocional
     * @param session HttpSession para recuperar dados
     * @return ResponseEntity com lista de cupons aplicados ou erro
     */
    @PostMapping("/aplicar-cupons")
    @ResponseBody
    public ResponseEntity<?> aplicarCupons(
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {

        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente nao encontrado"));

        try {
            @SuppressWarnings("unchecked")
            List<Long> cuponsTrocaIds = requestBody.get("cuponsTrocaIds") != null
                    ? ((List<Integer>) requestBody.get("cuponsTrocaIds")).stream()
                            .map(Integer::longValue)
                            .toList()
                    : List.of();

            String codigoPromocional = (String) requestBody.get("codigoPromocional");

            // Aplicar cupons usando o service
            List<CupomAplicadoDTO> cuponsAplicados = cupomService.aplicarCupons(
                    cuponsTrocaIds,
                    codigoPromocional,
                    cliente.getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("cuponsAplicados", cuponsAplicados);

            return ResponseEntity.ok(response);

        } catch (CupomInvalidoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", e.getMotivoInvalid());
            return ResponseEntity.badRequest().body(erro);

        } catch (CupomJaUtilizadoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", "JA_UTILIZADO");
            return ResponseEntity.badRequest().body(erro);

        } catch (CupomNaoEncontradoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", "NAO_ENCONTRADO");
            return ResponseEntity.badRequest().body(erro);

        } catch (CupomPromocionalDuplicadoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", "PROMOCIONAL_DUPLICADO");
            return ResponseEntity.badRequest().body(erro);
        }
    }

    /**
     * Calcula pagamento com cupons e gera excedente se necessario.
     * POST /checkout/pagamento/calcular
     * RN0036: Gerar cupom para excedente
     *
     * @param requestBody dados com cuponsTrocaIds e codigoPromocional
     * @param session HttpSession para recuperar dados do checkout
     * @return ResponseEntity com ResultadoPagamentoDTO
     */
    @PostMapping("/pagamento/calcular")
    @ResponseBody
    public ResponseEntity<?> calcularPagamento(
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {

        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente nao encontrado"));

        // Obter checkout da sessao
        CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");

        if (checkout == null || checkout.getValorTotal() == null) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Sessao expirada");
            return ResponseEntity.badRequest().body(erro);
        }

        try {
            @SuppressWarnings("unchecked")
            List<Long> cuponsTrocaIds = requestBody.get("cuponsTrocaIds") != null
                    ? ((List<Integer>) requestBody.get("cuponsTrocaIds")).stream()
                            .map(Integer::longValue)
                            .toList()
                    : List.of();

            String codigoPromocional = (String) requestBody.get("codigoCupomPromocional");

            // Aplicar cupons
            List<CupomAplicadoDTO> cuponsAplicados = cupomService.aplicarCupons(
                    cuponsTrocaIds,
                    codigoPromocional,
                    cliente.getId()
            );

            // Calcular resultado (inclui excedente se houver)
            ResultadoPagamentoDTO resultado = pagamentoService.processarPagamentoCupons(
                    cliente,
                    checkout.getValorTotal(),
                    cuponsAplicados
            );

            return ResponseEntity.ok(resultado);

        } catch (CupomInvalidoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", e.getMotivoInvalid());
            return ResponseEntity.badRequest().body(erro);

        } catch (CupomJaUtilizadoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", "JA_UTILIZADO");
            return ResponseEntity.badRequest().body(erro);

        } catch (CupomNaoEncontradoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", "NAO_ENCONTRADO");
            return ResponseEntity.badRequest().body(erro);

        } catch (CupomPromocionalDuplicadoException e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("motivo", "PROMOCIONAL_DUPLICADO");
            return ResponseEntity.badRequest().body(erro);
        }
    }

    /**
     * Processa o pagamento completo conforme PAY-05.
     * POST /checkout/processar-pagamento
     * RN0037: Validar pagamento antes de processar.
     * RN0038: Status pagamento: APROVADA ou REPROVADA.
     *
     * @param form dados do formulário de pagamento
     * @param session HttpSession para recuperar dados do checkout
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @param principal Principal para obter cliente autenticado
     * @return redirect baseado no resultado do pagamento
     */
    @PostMapping("/processar-pagamento")
    public String processarPagamentoCompleto(
            @ModelAttribute PagamentoFormDTO form,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        Long clienteId = getClienteId(principal);
        CheckoutDTO checkout = getCheckoutFromSession(session);

        try {
            // Verificar se carrinho está bloqueado (PAY-06)
            carrinhoService.verificarCarrinhoBloqueado(checkout.getCarrinhoId());

            // CHK-02: Re-validar estoque antes da finalização (RN0032)
            Carrinho carrinho = carrinhoRepository.findById(checkout.getCarrinhoId())
                .orElseThrow(() -> new CarrinhoNaoEncontradoException(checkout.getCarrinhoId()));
            compraValidator.revalidarEstoqueParaFinalizacao(carrinho);

            // Montar DTO de processamento
            ProcessarPagamentoDTO dto = new ProcessarPagamentoDTO();
            dto.setValorTotal(checkout.getValorTotal());
            dto.setCuponsAplicados(checkout.getCuponsAplicados());
            dto.setCartoesValores(form.getCartoesValores());

            // Processar pagamento
            Pagamento pagamento = pagamentoService.processarPagamento(dto, clienteId);

            checkout.setPagamentoId(pagamento.getId());
            checkout.setStatusPagamento(pagamento.getStatus());
            session.setAttribute("checkout", checkout);

            if (pagamento.getStatus() == StatusPagamento.APROVADA) {
                // Resetar tentativas (PAY-06)
                carrinhoService.resetarTentativasReprovadas(checkout.getCarrinhoId());
                return "redirect:/checkout/finalizar";
            } else {
                // Incrementar tentativas (PAY-06)
                try {
                    carrinhoService.registrarTentativaReprovada(checkout.getCarrinhoId());
                    int restantes = carrinhoService.getTentativasRestantes(checkout.getCarrinhoId());
                    redirectAttributes.addFlashAttribute("erro",
                        "Pagamento reprovado. Tentativas restantes: " + restantes);
                    redirectAttributes.addFlashAttribute("tentativasRestantes", restantes);
                } catch (CarrinhoBloqueadoPagamentoException e) {
                    redirectAttributes.addFlashAttribute("erroBloqueio", e.getMessage());
                    return "redirect:/checkout/bloqueado";
                }
                return "redirect:/checkout/pagamento";
            }

        } catch (CarrinhoBloqueadoPagamentoException e) {
            redirectAttributes.addFlashAttribute("erroBloqueio", e.getMessage());
            return "redirect:/checkout/bloqueado";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/checkout/pagamento";
        }
    }

    /**
     * Obtém o ID do cliente a partir do Principal
     */
    private Long getClienteId(Principal principal) {
        String emailLogado = principal.getName();
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return cliente.getId();
    }

    /**
     * Obtém o checkout da sessão com validação
     */
    private CheckoutDTO getCheckoutFromSession(HttpSession session) {
        CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");
        if (checkout == null) {
            throw new ValidacaoNegocioException("Sessão de checkout expirada");
        }
        return checkout;
    }

    /**
     * Exibe tela de confirmação final antes de finalizar compra.
     * GET /checkout/confirmar
     * TASK-CHK-03: Tela de confirmação antes de converter carrinho em pedido
     * RF0037: Finalizar compra
     *
     * @param session HttpSession para recuperar dados do checkout
     * @param model Model para adicionar atributos à view
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @return view name "checkout/confirmar" ou redirect se dados incompletos
     */
    @GetMapping("/confirmar")
    public String exibirConfirmacao(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Obter email do cliente logado
        String emailLogado = SecurityUtil.getEmailUsuarioLogado();

        // Buscar cliente pelo email
        Cliente cliente = clienteRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Obter checkout da sessão
        CheckoutDTO checkout = (CheckoutDTO) session.getAttribute("checkout");

        // Validar se pagamento foi processado
        if (checkout == null || checkout.getPagamentoId() == null ||
            checkout.getStatusPagamento() != StatusPagamento.APROVADA) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Complete o pagamento antes de finalizar a compra.");
            return "redirect:/checkout/pagamento";
        }

        try {
            // Buscar dados para exibição
            EnderecoDTO endereco = enderecoService.selecionarEnderecoEntrega(
                    cliente.getId(), checkout.getEnderecoEntregaId());

            CarrinhoDTO carrinho = carrinhoService.obterOuCriar(cliente.getCodigo());

            // Adicionar dados ao modelo
            model.addAttribute("cliente", cliente);
            model.addAttribute("endereco", endereco);
            model.addAttribute("carrinho", carrinho);
            model.addAttribute("frete", checkout.getFrete());
            model.addAttribute("valorProdutos", checkout.getValorProdutos());
            model.addAttribute("valorTotal", checkout.getValorTotal());
            model.addAttribute("cuponsAplicados", checkout.getCuponsAplicados());

            return "checkout/confirmar";

        } catch (RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/checkout/endereco";
        }
    }

    /**
     * Finaliza a compra convertendo carrinho em pedido.
     * POST /checkout/finalizar
     * TASK-CHK-03: Converter Carrinho em Pedido
     * RF0037: Finalizar compra (status inicial: EM_PROCESSAMENTO)
     * RN0028: Baixa estoque apenas após pagamento aprovado
     *
     * Executa:
     * 1. Conversão de carrinho em pedido via CompraService
     * 2. Baixa de estoque (CHK-04)
     * 3. Finalização do carrinho
     * 4. Registro de log
     *
     * @param session HttpSession para recuperar dados do checkout
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @param principal Principal para obter cliente autenticado
     * @return redirect para /checkout/sucesso ou /checkout/confirmar em caso de erro
     */
    @PostMapping("/finalizar")
    public String finalizarCompra(
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        Long clienteId = getClienteId(principal);
        CheckoutDTO checkout = getCheckoutFromSession(session);

        // Validar que pagamento foi aprovado
        if (checkout.getStatusPagamento() != StatusPagamento.APROVADA) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Pagamento não foi aprovado. Não é possível finalizar a compra.");
            return "redirect:/checkout/pagamento";
        }

        try {
            // Buscar endereço de entrega
            Endereco endereco = enderecoRepository.findById(checkout.getEnderecoEntregaId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Endereço de entrega não encontrado"));

            // Buscar pagamento processado
            Pagamento pagamento = pagamentoRepository.findById(checkout.getPagamentoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Pagamento não encontrado"));

            // Finalizar compra via CompraService (orquestrador)
            ResultadoCompraDTO resultado = compraService.finalizarCompra(
                    checkout.getCarrinhoId(),
                    endereco,
                    pagamento,
                    checkout.getFrete().getValor()
            );

            if (resultado.isSucesso()) {
                // Armazenar ID do pedido na sessão para exibir na tela de sucesso
                session.setAttribute("pedidoId", resultado.getPedidoId());
                session.setAttribute("mensagemSucesso", resultado.getMensagem());

                // Limpar dados do checkout da sessão
                session.removeAttribute("checkout");

                return "redirect:/checkout/sucesso";
            } else {
                // Falha na finalização
                redirectAttributes.addFlashAttribute("mensagemErro", resultado.getMensagem());
                return "redirect:/checkout/confirmar";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro ao finalizar compra: " + e.getMessage());
            return "redirect:/checkout/confirmar";
        }
    }

    /**
     * Exibe tela de sucesso após finalização da compra.
     * GET /checkout/sucesso
     * TASK-CHK-03: Tela de confirmação de compra finalizada
     * RF0037: Finalizar compra
     *
     * @param session HttpSession para recuperar dados do pedido
     * @param model Model para adicionar atributos à view
     * @param redirectAttributes RedirectAttributes para mensagens flash
     * @return view name "checkout/sucesso" ou redirect se dados incompletos
     */
    @GetMapping("/sucesso")
    public String exibirSucesso(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Recuperar ID do pedido da sessão
        Long pedidoId = (Long) session.getAttribute("pedidoId");
        String mensagemSucesso = (String) session.getAttribute("mensagemSucesso");

        if (pedidoId == null) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Nenhuma compra foi finalizada recentemente.");
            return "redirect:/carrinho";
        }

        try {
            // Buscar pedido criado
            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Pedido não encontrado"));

            // Adicionar dados ao modelo
            model.addAttribute("pedido", pedido);
            model.addAttribute("mensagemSucesso", mensagemSucesso);

            // Limpar dados da sessão após exibir
            session.removeAttribute("pedidoId");
            session.removeAttribute("mensagemSucesso");

            return "checkout/sucesso";

        } catch (RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/carrinho";
        }
    }

    // ===============================================
    // Exception Handlers - CHK-02
    // ===============================================

    /**
     * Tratamento para CarrinhoVazioException
     * Redireciona para o carrinho com mensagem de erro
     */
    @ExceptionHandler(CarrinhoVazioException.class)
    public String handleCarrinhoVazio(CarrinhoVazioException e, RedirectAttributes ra) {
        ra.addFlashAttribute("erro", e.getMessage());
        return "redirect:/carrinho";
    }

    /**
     * Tratamento para CarrinhoExpiradoException
     * Redireciona para o carrinho com mensagem de erro
     */
    @ExceptionHandler(CarrinhoExpiradoException.class)
    public String handleCarrinhoExpirado(CarrinhoExpiradoException e, RedirectAttributes ra) {
        ra.addFlashAttribute("erro", e.getMessage());
        return "redirect:/carrinho";
    }

    /**
     * Tratamento para exceções de validação de itens
     * Redireciona para revisão do carrinho
     */
    @ExceptionHandler({EstoqueInsuficienteException.class, LimiteItensExcedidoException.class, EstoqueNaoEncontradoException.class})
    public String handleValidacaoItem(RuntimeException e, RedirectAttributes ra) {
        ra.addFlashAttribute("erro", e.getMessage());
        return "redirect:/carrinho?action=revisar";
    }

    /**
     * Tratamento para LivroInativoException
     * Redireciona para remoção de livros inativos
     */
    @ExceptionHandler(LivroInativoException.class)
    public String handleLivroInativo(LivroInativoException e, RedirectAttributes ra) {
        ra.addFlashAttribute("erro", e.getMessage());
        return "redirect:/carrinho?action=remover-inativos";
    }
}
