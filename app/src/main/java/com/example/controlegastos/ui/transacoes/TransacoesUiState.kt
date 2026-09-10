package com.example.controlegastos.ui.transacoes

import com.example.controlegastos.domain.model.ContaSaldo
import com.example.controlegastos.domain.model.DespesaDetalhada
import com.example.controlegastos.domain.model.FaturaCartao
import java.time.YearMonth

data class TransacoesUiState(
    val carregando: Boolean = true,

    val mesSelecionado: YearMonth = YearMonth.now(),

    val valoresVisiveis: Boolean = true,

    val abaSelecionada: AbaFaturas = AbaFaturas.ABERTAS,

    val cartoesExpandidos: Set<Int> = emptySet(),

    /*
     * Soma dos saldos atuais das contas ativas.
     *
     * Não desconta automaticamente as faturas de cartão,
     * pois elas só saem do saldo quando a fatura é paga.
     */
    val saldoInicialTotal: Long = 0L,

    /*
     * Despesas avulsas pagas diretamente por uma conta.
     *
     * Este valor é usado para calcular o saldo real disponível.
     * Compras de cartão não entram aqui.
     */
    val despesasAvulsasTotal: Long = 0L,

    /*
     * Total de dívidas/despesas do mês selecionado.
     *
     * Inclui:
     * - compras únicas no cartão;
     * - compras parceladas no cartão;
     * - despesas fixas do mês.
     *
     * É o valor exibido como "Despesas" no card superior
     * da tela de transações.
     */
    val despesasDoMesTotal: Long = 0L,

    /*
     * Saldo real disponível:
     *
     * saldoInicialTotal - despesasAvulsasTotal
     *
     * Faturas de cartão não entram nessa subtração até que
     * o pagamento da fatura seja realizado.
     */
    val saldoAtualTotal: Long = 0L,

    val contas: List<ContaSaldo> = emptyList(),

    /*
     * Faturas do ciclo correspondente ao mês selecionado
     * que ainda possuem ao menos uma despesa não paga.
     */
    val faturasAbertas: List<FaturaCartao> = emptyList(),

    /*
     * Faturas do ciclo correspondente ao mês selecionado
     * cujas despesas já foram totalmente pagas.
     */
    val faturasFechadas: List<FaturaCartao> = emptyList(),

    /*
     * Despesas recorrentes/fixas do mês selecionado.
     */
    val despesasFixas: List<DespesaDetalhada> = emptyList(),

    val mensagemSucesso: String? = null,

    val mensagemErro: String? = null,

    val processandoPagamento: Boolean = false
)

enum class AbaFaturas {
    ABERTAS,
    FECHADAS
}