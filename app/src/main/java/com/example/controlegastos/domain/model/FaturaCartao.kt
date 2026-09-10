package com.example.controlegastos.domain.model

import java.time.LocalDate
import java.time.YearMonth

data class FaturaCartao(
    val cartao: Cartao,
    val mesAno: YearMonth,
    val dataVencimento: LocalDate,
    val totalCentavos: Long,
    val despesas: List<DespesaDetalhada>,
    val paga: Boolean
)