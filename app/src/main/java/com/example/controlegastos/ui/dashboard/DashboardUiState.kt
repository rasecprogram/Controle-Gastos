package com.example.controlegastos.ui.dashboard

import com.example.controlegastos.domain.model.Cartao
import com.example.controlegastos.domain.model.DespesaDetalhada
import com.example.controlegastos.domain.model.GastoPorCategoria
import com.example.controlegastos.domain.model.ResumoMensal
import java.time.YearMonth

data class DashboardUiState(
    val mesSelecionado: YearMonth = YearMonth.now(),

    val resumoMensal: ResumoMensal = ResumoMensal(
        totalGasto = 0L,
        totalPago = 0L,
        totalPendente = 0L
    ),

    val gastosPorCategoria: List<GastoPorCategoria> = emptyList(),
    val transacoesDoMes: List<DespesaDetalhada> = emptyList(),
    val cartoes: List<Cartao> = emptyList(),

    val totalSaldo: Long = 0L,
    val totalDespesas: Long = 0L,

    val numerosVisiveis: Boolean = true,
    val nomeUsuario: String = "Você",
    val carregando: Boolean = true

)