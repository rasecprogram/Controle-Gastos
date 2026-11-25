package com.example.controlegastos.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class ResumoMensal(
    val totalGasto: Long,
    val totalPago: Long,
    val totalPendente: Long
)