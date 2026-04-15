Objetivo: Simplificar os dois services sobrepostos do checkout.
Contexto: CompraService e TransacaoCheckoutService dividem responsabilidades do mesmo fluxo de compra, causando acoplamento desnecessário. CarrinhoController e CheckoutController também têm sobreposição.
Tarefa:
1. Fundir TransacaoCheckoutService em CompraService, mantendo CompraService como o único orquestrador do fluxo:
CompraService responsabilidades:
- validarCarrinho()
- calcularFrete()
- processarPagamento()  ← absorver de TransacaoCheckoutService
- gerarPedido()
- baixarEstoque()       ← apenas após pagamento APROVADO (RN0028)
- gerarCupomTroca()     ← quando aplicável
2. Manter CheckoutController apenas como controller HTTP — sem lógica de negócio nele.
Regras críticas que NÃO podem ser quebradas:

RN0028: Baixa de estoque apenas após pagamento APROVADO
RN0031/RN0032: Validar estoque no carrinho E antes da finalização
RN0033: Apenas um cupom promocional por compra
RN0034: Mínimo R$10 por cartão
RN0035: Consumir cupons antes do cartão
RN0065: 3 pagamentos REPROVADOS consecutivos bloqueiam carrinho
RN0064: Pedido mínimo R$20 sem frete

Validação: Fluxo completo de compra funciona — adicionar item, checkout, pagamento aprovado, estoque baixado, pedido criado com status EM_PROCESSAMENTO.