package com.example.controlegastos.ui.dashboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlegastos.domain.model.ResumoMensal
import com.example.controlegastos.domain.model.TipoContaSaldo
import com.example.controlegastos.domain.repository.ContaSaldoRepository
import com.example.controlegastos.domain.repository.DespesaRepository
import com.example.controlegastos.domain.repository.CartaoRepository
import com.example.controlegastos.domain.usecase.GetGastosPorCategoriaUseCase
import com.example.controlegastos.domain.usecase.GetResumoMensalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    private val getResumoMensalUseCase: GetResumoMensalUseCase,
    private val getGastosPorCategoriaUseCase: GetGastosPorCategoriaUseCase,
    private val despesaRepository: DespesaRepository,
    private val cartaoRepository: CartaoRepository,
    private val contaSaldoRepository: ContaSaldoRepository
) : AndroidViewModel(application) {

    private val preferences = application.getSharedPreferences(
        "backup_preferences",
        Context.MODE_PRIVATE
    )

    private val mesSelecionado = MutableStateFlow(YearMonth.now())
    private val numerosVisiveis = MutableStateFlow(true)

    private val nomeUsuarioFlow = MutableStateFlow(
        preferences.getString("chave_nome_usuario", "Você") ?: "Você"
    )

    private val cartoesAtivosFlow = cartaoRepository.observarAtivos()

    private val totalSaldoFlow = contaSaldoRepository
        .observarTodas()
        .map { contas ->
            contas
                .asSequence()
                .filter { conta ->
                    conta.tipo == TipoContaSaldo.CONTA ||
                            conta.tipo == TipoContaSaldo.CARTEIRA
                }
                .filter { conta ->
                    conta.saldoCentavos > 0L
                }
                .sumOf { conta ->
                    conta.saldoCentavos
                }
        }

    private val totalDespesasCartaoFlow = despesaRepository.observarTotalDespesasCartao()

    /**
     * Dados que dependem do mês selecionado (resumo, gastos por categoria e
     * despesas do mês). Apenas esta porção do estado é re-observada via
     * [flatMapLatest] quando o mês muda — os três flows internos são
     * combinados com um único [combine] (sem aninhamento adicional).
     */
    private data class DadosDoMes(
        val resumo: ResumoMensal,
        val gastosPorCategoria: List<com.example.controlegastos.domain.model.GastoPorCategoria>,
        val transacoesDoMes: List<com.example.controlegastos.domain.model.DespesaDetalhada>
    )

    private val dadosDoMesFlow = mesSelecionado
        .flatMapLatest { mesAno ->
            val resumoFlow = getResumoMensalUseCase(mes = mesAno.monthValue, ano = mesAno.year)
            val gastosPorCategoriaFlow = getGastosPorCategoriaUseCase(mes = mesAno.monthValue, ano = mesAno.year)
            val despesasDoMesFlow = despesaRepository.observarDespesasDetalhadasPorMes(mes = mesAno.monthValue, ano = mesAno.year)

            combine(resumoFlow, gastosPorCategoriaFlow, despesasDoMesFlow) { resumo, gastos, despesas ->
                DadosDoMes(
                    resumo = resumo,
                    gastosPorCategoria = gastos,
                    transacoesDoMes = despesas.sortedByDescending { it.dataVencimento }
                )
            }.catch { e ->
                emit(
                    DadosDoMes(
                        resumo = ResumoMensal(0L, 0L, 0L),
                        gastosPorCategoria = emptyList(),
                        transacoesDoMes = emptyList()
                    )
                )
            }
        }

    /**
     * Dados independentes do mês selecionado (cartões ativos, saldo total e
     * despesas de cartão). Combinados uma única vez, sem depender da cadeia
     * de [flatMapLatest] do mês — evitando que uma mudança nesses flows
     * reinicie a observação dos dados mensais, e vice-versa.
     */
    private val dadosIndependentesFlow = combine(
        cartoesAtivosFlow,
        totalSaldoFlow,
        totalDespesasCartaoFlow
    ) { cartoes, totalSaldo, totalDespesas ->
        Triple(cartoes, totalSaldo, totalDespesas)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        mesSelecionado,
        dadosDoMesFlow,
        dadosIndependentesFlow,
        numerosVisiveis,
        nomeUsuarioFlow
    ) { mesAno, dadosDoMes, dadosIndependentes, valoresVisiveis, nomeUsuario ->
        val (cartoes, totalSaldo, totalDespesas) = dadosIndependentes

        DashboardUiState(
            mesSelecionado = mesAno,
            resumoMensal = dadosDoMes.resumo,
            gastosPorCategoria = dadosDoMes.gastosPorCategoria,
            transacoesDoMes = dadosDoMes.transacoesDoMes,
            cartoes = cartoes,
            totalSaldo = totalSaldo,
            totalDespesas = totalDespesas,
            numerosVisiveis = valoresVisiveis,
            nomeUsuario = nomeUsuario,
            carregando = false
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(carregando = false)
        )

    fun carregarNomeUsuario() {
        val nomeSalvo = preferences.getString("chave_nome_usuario", "Você") ?: "Você"
        nomeUsuarioFlow.value = nomeSalvo
    }

    fun irParaMesAnterior() {
        mesSelecionado.value = mesSelecionado.value.minusMonths(1)
    }

    fun irParaProximoMes() {
        mesSelecionado.value = mesSelecionado.value.plusMonths(1)
    }

    fun alternarVisibilidadeValores() {
        numerosVisiveis.value = !numerosVisiveis.value
    }
}