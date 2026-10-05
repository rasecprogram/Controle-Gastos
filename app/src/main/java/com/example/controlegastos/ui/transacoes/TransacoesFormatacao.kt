package com.example.controlegastos.ui.transacoes

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.controlegastos.domain.model.Cartao
import com.example.controlegastos.domain.model.FaturaCartao

internal fun Long.formatarMoeda(visivel: Boolean): String {
    if (!visivel) return "R$ •••••"

    return NumberFormat
        .getCurrencyInstance(Locale("pt", "BR"))
        .format(this / 100.0)
}

internal fun Long.formatarDia(): String {
    return Instant
        .ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd/MM"))
}

internal fun String.toColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (_: IllegalArgumentException) {
    Color(0xFF5F8D84) // fallback
}

internal fun String.paraCentavos(): Long {
    return this
        .filter { caractere ->
            caractere.isDigit()
        }
        .toLongOrNull() ?: 0L
}

internal fun String.formatarValorTransferencia(): String {
    val centavos = this
        .filter { caractere ->
            caractere in '0'..'9'
        }
        .toLongOrNull() ?: 0L

    val valor = java.math.BigDecimal.valueOf(
        centavos,
        2
    )

    return java.text.NumberFormat
        .getNumberInstance(java.util.Locale("pt", "BR"))
        .apply {
            isGroupingUsed = true
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        .format(valor)
}

internal fun String.ehEmojiLocal(): Boolean {
    return this.any { caractere -> caractere.code > 255 }
}

internal fun ehPicPay(cartao: Cartao): Boolean {
    return cartao.nome.contains(
        "PicPay",
        ignoreCase = true
    ) || cartao.marcaChave.contains(
        "picpay",
        ignoreCase = true
    )
}

internal fun formatarVencimentoFatura(fatura: FaturaCartao): String {
    val data = fatura.dataVencimento

    val mes = data
        .format(
            DateTimeFormatter.ofPattern(
                "MMM",
                Locale("pt", "BR")
            )
        )
        .replace(".", "")
        .lowercase(Locale("pt", "BR"))

    return "Vence ${data.dayOfMonth} de $mes."
}

internal fun diasParaVencerTexto(fatura: FaturaCartao): String {
    val hoje = java.time.LocalDate.now()

    val vencimento = fatura.dataVencimento

    val dias = java.time.temporal.ChronoUnit.DAYS.between(
        hoje,
        vencimento
    )

    return when {
        dias < 0 -> "Vencida"
        dias == 0L -> "Vence Hoje"
        dias == 1L -> "Falta 1 dia para vencer"
        else -> "Faltam $dias dias para vencer"
    }
}

internal fun YearMonth.formatarMes(): String {
    return format(
        DateTimeFormatter.ofPattern(
            "MMMM yyyy",
            Locale("pt", "BR")
        )
    ).replaceFirstChar {
        it.titlecase(Locale("pt", "BR"))
    }
}