package com.example.controlegastos.domain.model

import androidx.compose.runtime.Immutable
import java.time.YearMonth

@Immutable
data class GastoMensal(
    val mesAno: YearMonth,
    val totalCentavos: Long
)