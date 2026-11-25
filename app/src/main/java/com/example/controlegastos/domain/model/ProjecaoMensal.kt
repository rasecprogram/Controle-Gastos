package com.example.controlegastos.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class ProjecaoMensal(
    val ano: Int,
    val mes: Int,
    val totalPendente: Long
)