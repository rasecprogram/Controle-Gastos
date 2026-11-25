package com.example.controlegastos.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Categoria(
    val id: Int,
    val nome: String,
    val corHex: String,
    val tetoMensal: Long?,
    val iconeChave: String = "outros",
    val ativa: Boolean = true
)