package com.les.jakebooks.controller;

import com.les.jakebooks.dto.*;
import com.les.jakebooks.exception.RecursoNaoEncontradoException;
import com.les.jakebooks.exception.ValidacaoNegocioException;
import com.les.jakebooks.model.enums.BandeiraCartao;
import com.les.jakebooks.model.enums.StatusCliente;
import com.les.jakebooks.model.enums.TipoEndereco;
import com.les.jakebooks.model.enums.TipoResidencia;
import com.les.jakebooks.repository.ClienteRepository;
import com.les.jakebooks.services.ClienteService;
import com.les.jakebooks.services.PedidoService;
import com.les.jakebooks.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

/**
 * Controller responsável pela gerência de clientes.
 * Segue padrão Frontend: sem lógica de negócio, apenas chamadas a Services.
 * RF0021-RF0028: Operações com clientes e relacionados (endereços, cartões, senhas)
 */
@Controller
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Lista clientes com busca opcional.
     * GET /clientes
     * RF0024: Consultar cliente
     *
     * @param nome nome do cliente (busca parcial, opcional)
     * @param cpf CPF do cliente (busca parcial, opcional)
     * @param model Model para adicionar atributos à view
     * @return view name "clientes/lista"
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            Model model) {

        // Simular busca (em produção, usar repository com specifications/predicates)
        List<ClienteDetalheDTO> clientes;
        
        if ((nome != null && !nome.isEmpty()) || (cpf != null && !cpf.isEmpty())) {
            clientes = clienteRepository.findAll().stream()
                    .map(cliente -> clienteService.buscarPorCodigo(cliente.getCodigo()))
                    .filter(cliente -> {
                        boolean nomeMatch = true;
                        boolean cpfMatch = true;
                        
                        if (nome != null && !nome.isEmpty()) {
                            nomeMatch = cliente.nome().toLowerCase().contains(nome.toLowerCase());
                        }
                        
                        if (cpf != null && !cpf.isEmpty()) {
                            cpfMatch = cliente.cpf().contains(cpf);
                        }
                        
                        return nomeMatch && cpfMatch;
                    })
                    .toList();
        } else {
            clientes = clienteRepository.findAll().stream()
                    .map(cliente -> clienteService.buscarPorCodigo(cliente.getCodigo()))
                    .toList();
        }

        model.addAttribute("clientes", clientes);
        model.addAttribute("nome", nome);
        model.addAttribute("cpf", cpf);
        model.addAttribute("isAdmin", SecurityUtil.isAdmin());

        return "clientes/lista";
    }

    /**
     * Exibe formulário para novo cliente.
     * GET /clientes/novo
     * RF0021: Cadastrar cliente
     *
     * @param model Model para adicionar atributos à view
     * @return view name "clientes/form-cadastro"
     */
    @GetMapping("/novo")
    public String formularioNovo(Model model) {
        model.addAttribute("clienteForm", new ClienteCadastroDTO(
                null, null, null, null, null, null, null, null
        ));
        model.addAttribute("isAdmin", SecurityUtil.isAdmin());
        return "clientes/form-cadastro";
    }

/**
     * Cria novo cliente.
     * POST /clientes
     * RF0021: Cadastrar cliente
     *
     * @param dto DTO com dados do cliente
     * @param result resultado da validação
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /clientes/{codigo} em caso de sucesso
     */
    @PostMapping
    public String criar(
            @Valid @ModelAttribute("clienteForm") ClienteCadastroDTO dto,
            BindingResult result,
            RedirectAttributes attrs,
            Model model) {

        // Validar senhas antes de tudo
        if (!dto.senha().equals(dto.confirmacaoSenha())) {
            result.rejectValue("confirmacaoSenha", "senhas.diferentes", "Senhas não conferem");
        }

        if (result.hasErrors()) {
            // Retorna a view direto — mantém BindingResult e objeto preenchido
            return "clientes/form-cadastro";
        }

        try {
            ClienteDetalheDTO cliente = clienteService.cadastrar(dto);
            attrs.addFlashAttribute("mensagemSucesso", "Cliente cadastrado! Código: " + cliente.codigo());
            return "redirect:/clientes/" + cliente.codigo();
        } catch (ValidacaoNegocioException e) {
            model.addAttribute("mensagemErro", e.getMessage());
            return "clientes/form-cadastro"; // retorna a view com a mensagem de erro
        }
    }     
    /**
     * Exibe detalhes de um cliente.
     * GET /clientes/{codigo}
     * RF0024: Consultar cliente
     *
     * @param codigo código único do cliente
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "clientes/detalhe" ou redirect se não encontrado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{codigo}")
    public String detalhe(
            @PathVariable String codigo,
            Model model,
            RedirectAttributes attrs) {

        try {
            ClienteDetalheDTO cliente = clienteService.buscarPorCodigo(codigo);
            List<PedidoResumoDTO> pedidos = clienteService.buscarTransacoes(codigo);
            
            model.addAttribute("cliente", cliente);
            model.addAttribute("pedidos", pedidos);
            model.addAttribute("isAdmin", SecurityUtil.isAdmin());
            return "clientes/detalhe";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Cliente não encontrado");
            return "redirect:/clientes";
        }
    }

    /**
     * Exibe formulário para editar um cliente existente.
     * GET /clientes/{codigo}/editar
     * RF0022: Alterar cliente
     *
     * @param codigo código único do cliente
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "clientes/form-edicao" ou redirect se não encontrado
     */
    @GetMapping("/{codigo}/editar")
    public String formularioEditar(
            @PathVariable String codigo,
            Model model,
            RedirectAttributes attrs) {

        try {
            ClienteDetalheDTO clienteExistente = clienteService.buscarPorCodigo(codigo);

            // Converter para AlteracaoDTO
            ClienteAlteracaoDTO clienteForm = new ClienteAlteracaoDTO(
                    clienteExistente.nome(),
                    clienteExistente.genero(),
                    clienteExistente.dataNascimento(),
                    clienteExistente.telefone(),
                    clienteExistente.email(),
                    clienteExistente.status()
            );

            model.addAttribute("clienteForm", clienteForm);
            model.addAttribute("clienteExistente", clienteExistente);
            model.addAttribute("statusClientes", StatusCliente.values());
            model.addAttribute("isAdmin", SecurityUtil.isAdmin());

            return "clientes/form-edicao";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Cliente não encontrado");
            return "redirect:/clientes";
        }
    }

    /**
     * Atualiza um cliente existente.
     * POST /clientes/{codigo}
     * RF0022: Alterar cliente
     *
     * @param codigo código único do cliente
     * @param dto DTO com novos dados
     * @param result resultado da validação
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /clientes/{codigo}
     */
    @PostMapping("/{codigo}")
    public String atualizar(
            @PathVariable String codigo,
            @Valid @ModelAttribute("clienteForm") ClienteAlteracaoDTO dto,
            BindingResult result,
            RedirectAttributes attrs) {

        if (result.hasErrors()) {
            attrs.addFlashAttribute("mensagemErro", "Verifique os erros abaixo");
            return "redirect:/clientes/" + codigo + "/editar";
        }

        try {
            clienteService.alterar(codigo, dto);
            attrs.addFlashAttribute("mensagemSucesso", "Cliente atualizado com sucesso!");
            return "redirect:/clientes/" + codigo;
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/clientes/" + codigo + "/editar";
        }
    }

    /**
     * Altera senha do cliente.
     * POST /clientes/{codigo}/senha
     * RF0028: Alterar apenas senha
     *
     * @param codigo código único do cliente
     * @param dto DTO com senhas
     * @param result resultado da validação
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /clientes/{codigo}
     */
    @PostMapping("/{codigo}/senha")
    public String alterarSenha(
            @PathVariable String codigo,
            @Valid @ModelAttribute AlteraSenhaDTO dto,
            BindingResult result,
            RedirectAttributes attrs) {

        if (result.hasErrors()) {
            attrs.addFlashAttribute("mensagemErro", "Verifique os erros abaixo");
            return "redirect:/clientes/" + codigo;
        }

        // Validar confirmação de senha
        if (!dto.novaSenha().equals(dto.confirmacaoNovaSenha())) {
            attrs.addFlashAttribute("mensagemErro", "Novas senhas não conferem");
            return "redirect:/clientes/" + codigo;
        }

        try {
            clienteService.alterarSenha(codigo, dto);
            attrs.addFlashAttribute("mensagemSucesso", "Senha alterada com sucesso!");
            return "redirect:/clientes/" + codigo;
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/clientes/" + codigo;
        }
    }

    /**
     * Inativa um cliente.
     * POST /clientes/{codigo}/inativar
     * RF0023: Inativar cliente
     *
     * @param codigo código único do cliente
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /clientes
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{codigo}/inativar")
    public String inativar(
            @PathVariable String codigo,
            RedirectAttributes attrs) {

        try {
            clienteService.inativar(codigo);
            attrs.addFlashAttribute("mensagemSucesso", "Cliente inativado com sucesso!");
            return "redirect:/clientes";
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/clientes/" + codigo;
        }
    }

    /**
     * Exibe perfil do próprio cliente autenticado.
     * GET /clientes/perfil
     * RF0024: Consultar cliente (próprio perfil)
     *
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "clientes/perfil" ou redirect se não encontrado
     */
    @GetMapping("/perfil")
    public String perfil(Model model, RedirectAttributes attrs) {
        try {
            String email = SecurityUtil.getEmailUsuarioLogado();
            ClienteDetalheDTO cliente = clienteService.buscarPorEmail(email);
            List<PedidoResumoDTO> pedidos = clienteService.buscarTransacoes(cliente.codigo());

            model.addAttribute("cliente", cliente);
            model.addAttribute("pedidos", pedidos);
            model.addAttribute("isAdmin", SecurityUtil.isAdmin());
            return "clientes/perfil";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Perfil não encontrado");
            return "redirect:/";
        }
    }

    /**
     * Exibe formulário para editar próprio perfil.
     * GET /clientes/perfil/editar
     * RF0022: Alterar cliente (próprio perfil)
     *
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "clientes/form-perfil" ou redirect se não encontrado
     */
    @GetMapping("/perfil/editar")
    public String editarPerfil(Model model, RedirectAttributes attrs) {
        try {
            String email = SecurityUtil.getEmailUsuarioLogado();
            ClienteDetalheDTO clienteExistente = clienteService.buscarPorEmail(email);

            ClienteAlteracaoDTO clienteForm = new ClienteAlteracaoDTO(
                    clienteExistente.nome(),
                    clienteExistente.genero(),
                    clienteExistente.dataNascimento(),
                    clienteExistente.telefone(),
                    clienteExistente.email(),
                    clienteExistente.status()
            );

            model.addAttribute("clienteForm", clienteForm);
            model.addAttribute("clienteExistente", clienteExistente);
            model.addAttribute("isAdmin", SecurityUtil.isAdmin());

            return "clientes/form-perfil";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Perfil não encontrado");
            return "redirect:/";
        }
    }

    /**
     * Atualiza próprio perfil.
     * POST /clientes/perfil/editar
     * RF0022: Alterar cliente (próprio perfil)
     *
     * @param dto DTO com novos dados
     * @param result resultado da validação
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /clientes/perfil
     */
    @PostMapping("/perfil/editar")
    public String salvarEdicaoPerfil(
            @Valid @ModelAttribute("clienteForm") ClienteAlteracaoDTO dto,
            BindingResult result,
            RedirectAttributes attrs) {

        if (result.hasErrors()) {
            attrs.addFlashAttribute("mensagemErro", "Verifique os erros abaixo");
            return "redirect:/clientes/perfil/editar";
        }

        try {
            String email = SecurityUtil.getEmailUsuarioLogado();
            ClienteDetalheDTO cliente = clienteService.buscarPorEmail(email);
            clienteService.alterar(cliente.codigo(), dto);
            attrs.addFlashAttribute("mensagemSucesso", "Perfil atualizado com sucesso!");
            return "redirect:/clientes/perfil";
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/clientes/perfil/editar";
        }
    }

    /**
     * Exibe formulário para alterar própria senha.
     * GET /clientes/alterar-senha
     * RF0028: Alterar apenas senha
     *
     * @param model Model para adicionar atributos à view
     * @return view name "clientes/form-alterar-senha"
     */
    @GetMapping("/alterar-senha")
    public String exibirFormularioAlterarSenha(Model model) {
        model.addAttribute("alteraSenhaForm", new AlteraSenhaDTO(null, null, null));
        model.addAttribute("isAdmin", SecurityUtil.isAdmin());
        return "clientes/form-alterar-senha";
    }

    /**
     * Altera própria senha.
     * POST /clientes/alterar-senha
     * RF0028: Alterar apenas senha
     *
     * @param dto DTO com senhas
     * @param result resultado da validação
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /clientes/perfil
     */
    @PostMapping("/alterar-senha")
    public String alterarSenhaPropria(
            @Valid @ModelAttribute("alteraSenhaForm") AlteraSenhaDTO dto,
            BindingResult result,
            RedirectAttributes attrs) {

        if (result.hasErrors()) {
            attrs.addFlashAttribute("mensagemErro", "Verifique os erros abaixo");
            return "redirect:/clientes/alterar-senha";
        }

        // Validar confirmação de senha
        if (!dto.novaSenha().equals(dto.confirmacaoNovaSenha())) {
            attrs.addFlashAttribute("mensagemErro", "Novas senhas não conferem");
            return "redirect:/clientes/alterar-senha";
        }

        try {
            String email = SecurityUtil.getEmailUsuarioLogado();
            ClienteDetalheDTO cliente = clienteService.buscarPorEmail(email);
            clienteService.alterarSenha(cliente.codigo(), dto);
            attrs.addFlashAttribute("mensagemSucesso", "Senha alterada com sucesso!");
            return "redirect:/clientes/perfil";
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/clientes/alterar-senha";
        }
    }

    /**
     * Exibe formulário para novo endereço.
     * GET /clientes/{codigo}/enderecos/novo
     * RF0026: Cadastrar múltiplos endereços
     *
     * @param codigo código do cliente
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "clientes/form-endereco" ou redirect se cliente não encontrado
     */
    @GetMapping("/{codigo}/enderecos/novo")
    public String formularioNovoEndereco(
            @PathVariable String codigo,
            Model model,
            RedirectAttributes attrs) {

        try {
            ClienteDetalheDTO cliente = clienteService.buscarPorCodigo(codigo);

            model.addAttribute("endereco", new EnderecoDTO(null, null, null, null, null, null, null, null, null, null, null));
            model.addAttribute("clienteCodigo", codigo);
            model.addAttribute("clienteNome", cliente.nome());
            model.addAttribute("tiposResidencia", TipoResidencia.values());
            model.addAttribute("tiposEndereco", TipoEndereco.values());
            model.addAttribute("isAdmin", SecurityUtil.isAdmin());

            return "clientes/form-endereco";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Cliente não encontrado");
            return "redirect:/clientes";
        }
    }

    /**
     * Adiciona novo endereço ao cliente.
     * POST /clientes/{codigo}/enderecos
     * RF0026: Cadastrar múltiplos endereços
     *
     * @param codigo código do cliente
     * @param dto DTO com dados do endereço
     * @param result resultado da validação
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /clientes/{codigo}
     */
    @PostMapping("/{codigo}/enderecos")
    public String novoEndereco(
            @PathVariable String codigo,
            @Valid @ModelAttribute EnderecoDTO dto,
            BindingResult result,
            RedirectAttributes attrs) {

        if (result.hasErrors()) {
            attrs.addFlashAttribute("mensagemErro", "Verifique os erros abaixo");
            return "redirect:/clientes/" + codigo + "/enderecos/novo";
        }

        try {
            clienteService.adicionarEndereco(codigo, dto);
            attrs.addFlashAttribute("mensagemSucesso", "Endereço adicionado com sucesso!");
            return "redirect:/clientes/" + codigo;
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/clientes/" + codigo + "/enderecos/novo";
        }
    }

    /**
     * Exibe formulário para novo cartão.
     * GET /clientes/{codigo}/cartoes/novo
     * RF0027: Cadastrar múltiplos cartões
     *
     * @param codigo código do cliente
     * @param model Model para adicionar atributos à view
     * @param attrs RedirectAttributes para mensagens de erro
     * @return view name "clientes/form-cartao" ou redirect se cliente não encontrado
     */
    @GetMapping("/{codigo}/cartoes/novo")
    public String formularioNovoCartao(
            @PathVariable String codigo,
            Model model,
            RedirectAttributes attrs) {

        try {
            ClienteDetalheDTO cliente = clienteService.buscarPorCodigo(codigo);

            model.addAttribute("cartao", new CartaoDTO(null, null, null, null, null, false));
            model.addAttribute("cliente", cliente);
            model.addAttribute("clienteCodigo", cliente.codigo());
            model.addAttribute("clienteNome", cliente.nome());
            model.addAttribute("bandeiras", BandeiraCartao.values());
            model.addAttribute("isAdmin", SecurityUtil.isAdmin());

            return "clientes/form-cartao";
        } catch (RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", "Cliente não encontrado");
            return "redirect:/clientes";
        }
    }

    /**
     * Adiciona novo cartão ao cliente.
     * POST /clientes/{codigo}/cartoes
     * RF0027: Cadastrar múltiplos cartões
     *
     * @param codigo código do cliente
     * @param dto DTO com dados do cartão
     * @param result resultado da validação
     * @param attrs RedirectAttributes para mensagens flash
     * @return redirect para /clientes/{codigo}
     */
    @PostMapping("/{codigo}/cartoes")
    public String novoCartao(
            @PathVariable String codigo,
            @Valid @ModelAttribute CartaoDTO dto,
            BindingResult result,
            RedirectAttributes attrs) {

        if (result.hasErrors()) {
            attrs.addFlashAttribute("mensagemErro", "Verifique os erros abaixo");
            return "redirect:/clientes/" + codigo + "/cartoes/novo";
        }

        try {
            clienteService.adicionarCartao(codigo, dto);
            attrs.addFlashAttribute("mensagemSucesso", "Cartão adicionado com sucesso!");
            return "redirect:/clientes/" + codigo;
        } catch (ValidacaoNegocioException | RecursoNaoEncontradoException e) {
            attrs.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/clientes/" + codigo + "/cartoes/novo";
        }
    }
}
