package com.example.controlegastos.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class ContaSaldo(
    val id: Int = 0,
    val nome: String,
    val instituicaoChave: String,
    val tipo: TipoContaSaldo,
    val saldoCentavos: Long,
    val corHex: String,
    val ativo: Boolean = true
)

enum class TipoContaSaldo {
    CONTA,
    CARTEIRA,
    SALDO_RESERVADO
}