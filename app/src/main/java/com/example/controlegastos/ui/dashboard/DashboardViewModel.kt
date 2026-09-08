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
                            conta.tipo == TipoContaSaldo.SALDO_RESERVADO
                }
                .filter { conta ->
                    conta.saldoCentavos > 0L
                }
                .sumOf { conta ->
                    conta.saldoCentavos
                }
        }

    private val totalDespesasCartaoFlow = despesaRepository.observarTotalDespesasCartao()

    val uiState: StateFlow<DashboardUiState> = combine(
        mesSelecionado,
        numerosVisiveis,
        nomeUsuarioFlow
    ) { mesAno, valoresVisiveis, nome ->
        Triple(mesAno, valoresVisiveis, nome)
    }
        .flatMapLatest { (mesAno, valoresVisiveis, nomeUsuario) ->
            val resumoFlow = getResumoMensalUseCase(mes = mesAno.monthValue, ano = mesAno.year)
            val gastosPorCategoriaFlow = getGastosPorCategoriaUseCase(mes = mesAno.monthValue, ano = mesAno.year)
            val despesasDoMesFlow = despesaRepository.observarDespesasDetalhadasPorMes(mes = mesAno.monthValue, ano = mesAno.year)

            combine(resumoFlow, gastosPorCategoriaFlow, despesasDoMesFlow) { resumo, gastos, despesas ->
                Triple(resumo, gastos, despesas)
            }.flatMapLatest { (resumo, gastosPorCategoria, despesas) ->
                combine(
                    cartoesAtivosFlow,
                    totalSaldoFlow,
                    totalDespesasCartaoFlow
                ) { cartoes, totalSaldo, totalDespesas ->
                    DashboardUiState(
                        mesSelecionado = mesAno,
                        resumoMensal = resumo,
                        gastosPorCategoria = gastosPorCategoria,
                        transacoesDoMes = despesas.sortedByDescending { it.dataVencimento },
                        cartoes = cartoes,
                        totalSaldo = totalSaldo,
                        totalDespesas = totalDespesas,
                        numerosVisiveis = valoresVisiveis,
                        nomeUsuario = nomeUsuario,
                        carregando = false
                    )
                }.catch { e ->
                    emit(
                        DashboardUiState(
                            mesSelecionado = mesAno,
                            resumoMensal = ResumoMensal(0L, 0L, 0L),
                            gastosPorCategoria = emptyList(),
                            transacoesDoMes = emptyList(),
                            cartoes = emptyList(),
                            totalSaldo = 0L,
                            totalDespesas = 0L,
                            numerosVisiveis = valoresVisiveis,
                            nomeUsuario = nomeUsuario,
                            carregando = false
                        )
                    )
                }
            }
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