package com.example.controlegastos.ui.transacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlegastos.domain.model.Cartao
import com.example.controlegastos.domain.model.ContaSaldo
import com.example.controlegastos.domain.model.DespesaDetalhada
import com.example.controlegastos.domain.model.FaturaCartao
import com.example.controlegastos.domain.model.TipoLancamento
import com.example.controlegastos.domain.repository.CartaoRepository
import com.example.controlegastos.domain.repository.ContaSaldoRepository
import com.example.controlegastos.domain.repository.DespesaRepository
import com.example.controlegastos.domain.util.calcularFimExclusivoCicloFatura
import com.example.controlegastos.domain.util.calcularInicioCicloFatura
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneOffset
import javax.inject.Inject
import com.example.controlegastos.domain.util.calcularDataVencimentoDaFatura
import com.example.controlegastos.domain.util.criarDataVencimento

@HiltViewModel
class TransacoesViewModel @Inject constructor(
    private val despesaRepository: DespesaRepository,
    private val cartaoRepository: CartaoRepository,
    private val contaSaldoRepository: ContaSaldoRepository
) : ViewModel() {

    private val mesSelecionado = MutableStateFlow(
        YearMonth.now()
    )

    private val valoresVisiveis = MutableStateFlow(true)

    private val abaSelecionada = MutableStateFlow(
        AbaFaturas.ABERTAS
    )

    private val cartoesExpandidos = MutableStateFlow<Set<Int>>(
        emptySet()
    )

    /*
     * Despesas cujo dataCompra pertence ao mês selecionado.
     *
     * Usadas para:
     * - despesas pagas diretamente por conta;
     * - despesas fixas daquele mês;
     * - valor "Despesas" do card de saldo;
     * - total de despesas do mês.
     */
    private val despesasDoMesCompra: Flow<List<DespesaDetalhada>> =
        mesSelecionado.flatMapLatest { mes ->
            despesaRepository.observarDespesasDetalhadasPorMes(
                mes = mes.monthValue,
                ano = mes.year
            )
        }

    /*
     * Busca ampla para formar a fatura do mês selecionado.
     *
     * A fatura de setembro pode conter compras do fim de julho,
     * agosto e setembro, conforme os dias de fechamento de cada cartão.
     *
     * Por isso são buscados três meses:
     * - dois meses antes;
     * - mês selecionado;
     * - até o primeiro dia do mês seguinte.
     *
     * Exemplo:
     * Para setembro/2026:
     * início da busca: 01/07/2026
     * fim da busca:    01/10/2026
     */
    private val despesasParaFaturas: Flow<List<DespesaDetalhada>> =
        mesSelecionado.flatMapLatest { mes ->
            val inicioEpoch = mes
                .minusMonths(2)
                .atDay(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()

            val fimEpoch = mes
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()

            despesaRepository.observarDetalhadasEntre(
                inicioEpoch = inicioEpoch,
                fimEpoch = fimEpoch
            )
        }

    private val contas: Flow<List<ContaSaldo>> =
        contaSaldoRepository.observarTodas()

    private val cartoes: Flow<List<Cartao>> =
        cartaoRepository.observarTodos()

    /*
     * Estados de interação/controladores da tela.
     */
    private val filtrosTela = combine(
        mesSelecionado,
        valoresVisiveis,
        abaSelecionada,
        cartoesExpandidos
    ) { mes, visiveis, aba, expandidos ->
        FiltrosTelaTransacoes(
            mes = mes,
            valoresVisiveis = visiveis,
            abaSelecionada = aba,
            cartoesExpandidos = expandidos
        )
    }

    /*
     * Dados persistidos necessários pela tela.
     */
    private val dadosTela = combine(
        despesasDoMesCompra,
        despesasParaFaturas,
        contas,
        cartoes
    ) {
            despesasMesCompra,
            despesasFaturas,
            contasAtuais,
            cartoesAtuais ->

        DadosTransacoes(
            despesasMesCompra = despesasMesCompra,
            despesasFaturas = despesasFaturas,
            contas = contasAtuais,
            cartoes = cartoesAtuais
        )
    }

    val uiState: StateFlow<TransacoesUiState> = combine(
        filtrosTela,
        dadosTela
    ) { filtros, dados ->

        val contasAtivas = dados.contas.filter { conta ->
            conta.ativo
        }

        /*
         * Despesas avulsas são despesas:
         * - sem cartão;
         * - não fixas.
         *
         * Elas continuam sendo usadas para calcular o saldo real
         * quando já foram pagas diretamente por uma conta.
         */
        val despesasAvulsas = dados.despesasMesCompra.filter { despesa ->
            despesa.cartaoId == null &&
                    despesa.tipoLancamento != TipoLancamento.FIXA
        }

        /*
         * Total exibido no card superior da tela de transações.
         *
         * Inclui:
         * - compras únicas no cartão;
         * - compras parceladas no cartão;
         * - despesas fixas daquele mês.
         *
         * Não inclui despesas avulsas pagas por conta/carteira,
         * pois essas já afetam diretamente o saldo das contas.
         */
        val despesasDoMesTotal = dados.despesasMesCompra
            .asSequence()
            .filter { despesa ->
                despesa.cartaoId != null ||
                        despesa.tipoLancamento == TipoLancamento.FIXA
            }
            .sumOf { despesa ->
                despesa.valor
            }

        val despesasFixas = dados.despesasFaturas
            .asSequence()
            .filter { despesa ->
                despesa.tipoLancamento == TipoLancamento.FIXA
            }
            .groupBy { despesa ->
                despesa.descricao
                    .trim()
                    .lowercase()
            }
            .map { (_, ocorrencias) ->
                ocorrencias.minByOrNull { despesa ->
                    despesa.dataCompra
                }!!
            }
            .sortedBy { despesa ->
                despesa.descricao.lowercase()
            }
            .toList()

        val saldoInicialTotal = contasAtivas.sumOf { conta ->
            conta.saldoCentavos
        }

        /*
         * O saldo disponível é reduzido somente por despesas avulsas
         * já pagas usando uma conta de saldo.
         *
         * Compras no cartão não reduzem o saldo aqui, porque só
         * reduzem a conta quando a fatura é efetivamente paga.
         */
        val despesasAvulsasTotal = despesasAvulsas
            .asSequence()
            .filter { despesa ->
                despesa.contaSaldoId != null &&
                        despesa.statusPago
            }
            .sumOf { despesa ->
                despesa.valor
            }

        /*
         * Cria uma fatura para cada cartão ativo, usando o ciclo
         * calculado pelo vencimento e pelos dias antes do vencimento.
         *
         * Compra no dia de fechamento pertence à próxima fatura,
         * pois o fim do ciclo é exclusivo:
         *
         * dataCompra < fimExclusivoCicloMillis
         */
        val faturas = dados.cartoes
            .filter { cartao ->
                cartao.ativo
            }
            .map { cartao ->


                val dataVencimentoFatura = criarDataVencimento(
                    anoMes = filtros.mes.plusMonths(1),
                    diaVencimento = cartao.diaVencimento
                )

                val inicioCiclo = calcularInicioCicloFatura(
                    mesFatura = filtros.mes,
                    diasAntesVencimento = cartao.diasAntesVencimento,
                    diaVencimento = cartao.diaVencimento
                )

                val fimExclusivoCiclo = calcularFimExclusivoCicloFatura(
                    mesFatura = filtros.mes,
                    diasAntesVencimento = cartao.diasAntesVencimento,
                    diaVencimento = cartao.diaVencimento
                )
                val dataVencimento = calcularDataVencimentoDaFatura(
                    mesFatura = filtros.mes,
                    diaVencimento = cartao.diaVencimento,
                    diasAntesVencimento = cartao.diasAntesVencimento
                )

                val inicioCicloMillis = inicioCiclo
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()

                val fimExclusivoCicloMillis = fimExclusivoCiclo
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()

                val despesasDoCartao = dados.despesasFaturas
                    .asSequence()
                    .filter { despesa ->
                        despesa.cartaoId == cartao.id &&
                                despesa.dataCompra >= inicioCicloMillis &&
                                despesa.dataCompra < fimExclusivoCicloMillis
                    }
                    .sortedBy { despesa ->
                        despesa.dataCompra
                    }
                    .toList()

                FaturaCartao(
                    cartao = cartao,
                    mesAno = filtros.mes,
                    dataVencimento = dataVencimentoFatura,
                    totalCentavos = despesasDoCartao.sumOf { despesa ->
                        despesa.valor
                    },
                    despesas = despesasDoCartao,
                    paga = despesasDoCartao.isNotEmpty() &&
                            despesasDoCartao.all { despesa ->
                                despesa.statusPago
                            }
                )
            }

        val faturasAbertas = faturas.filter { fatura ->
            fatura.despesas.isNotEmpty() &&
                    !fatura.paga
        }

        val faturasFechadas = faturas.filter { fatura ->
            fatura.despesas.isNotEmpty() &&
                    fatura.paga
        }

        TransacoesUiState(
            carregando = false,
            mesSelecionado = filtros.mes,
            valoresVisiveis = filtros.valoresVisiveis,
            abaSelecionada = filtros.abaSelecionada,
            cartoesExpandidos = filtros.cartoesExpandidos,
            saldoInicialTotal = saldoInicialTotal,
            despesasAvulsasTotal = despesasAvulsasTotal,
            despesasDoMesTotal = despesasDoMesTotal,
            saldoAtualTotal = saldoInicialTotal - despesasAvulsasTotal,
            contas = contasAtivas,
            faturasAbertas = faturasAbertas,
            faturasFechadas = faturasFechadas,
            despesasFixas = despesasFixas
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransacoesUiState()
    )

    fun mesAnterior() {
        mesSelecionado.value = mesSelecionado.value.minusMonths(1)
    }

    fun proximoMes() {
        mesSelecionado.value = mesSelecionado.value.plusMonths(1)
    }

    fun alternarValores() {
        valoresVisiveis.value = !valoresVisiveis.value
    }

    fun selecionarAbaFaturas(
        aba: AbaFaturas
    ) {
        abaSelecionada.value = aba
    }

    fun alternarCartao(
        cartaoId: Int
    ) {
        cartoesExpandidos.value = if (
            cartaoId in cartoesExpandidos.value
        ) {
            cartoesExpandidos.value - cartaoId
        } else {
            cartoesExpandidos.value + cartaoId
        }
    }

    fun pagarFatura(
        cartaoId: Int,
        contaId: Int,
        aoConcluir: (String?) -> Unit
    ) {
        val mes = mesSelecionado.value

        viewModelScope.launch {
            try {
                val pago = despesaRepository.pagarFatura(
                    cartaoId = cartaoId,
                    mes = mes.monthValue,
                    ano = mes.year,
                    contaId = contaId
                )

                if (pago) {
                    aoConcluir(null)
                } else {
                    aoConcluir(
                        "Não foi possível pagar esta fatura."
                    )
                }
            } catch (erro: IllegalArgumentException) {
                aoConcluir(
                    erro.message
                        ?: "Não foi possível pagar esta fatura."
                )
            } catch (_: Exception) {
                aoConcluir(
                    "Ocorreu um erro ao pagar a fatura."
                )
            }
        }
    }
}

private data class FiltrosTelaTransacoes(
    val mes: YearMonth,
    val valoresVisiveis: Boolean,
    val abaSelecionada: AbaFaturas,
    val cartoesExpandidos: Set<Int>
)

private data class DadosTransacoes(
    val despesasMesCompra: List<DespesaDetalhada>,
    val despesasFaturas: List<DespesaDetalhada>,
    val contas: List<ContaSaldo>,
    val cartoes: List<Cartao>
)