package com.example.controlegastos.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class GastoPorCategoria(
    val categoriaId: Int,
    val nomeCategoria: String,
    val corHex: String,
    val tetoMensal: Long?,
    val totalGasto: Long,
    val percentualDoTotal: Float,
    val iconeChave: String? = null // novo campo
)