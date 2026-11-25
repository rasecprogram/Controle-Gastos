package com.example.controlegastos.ui.gastos

import androidx.compose.runtime.Immutable
import com.example.controlegastos.domain.model.DespesaDetalhada
import com.example.controlegastos.domain.model.GastoMensal
import com.example.controlegastos.domain.model.GastoPorCategoria
import java.time.YearMonth
import com.example.controlegastos.domain.model.Cartao

@Immutable
data class GastosUiState(
    val carregando: Boolean = true,
    val mesSelecionado: YearMonth = YearMonth.now(),
    val gastosMensais: List<GastoMensal> = emptyList(),
    val totalMesSelecionado: Long = 0L,
    val despesasDoMes: List<DespesaDetalhada> = emptyList(),
    val cartoes: List<Cartao> = emptyList(),
    val gastosPorCategoria: List<GastoPorCategoria> = emptyList()
)