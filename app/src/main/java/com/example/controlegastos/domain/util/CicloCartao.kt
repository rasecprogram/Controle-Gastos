package com.example.controlegastos.domain.util

import java.time.LocalDate
import java.time.YearMonth

fun criarDataVencimento(
    anoMes: YearMonth,
    diaVencimento: Int
): LocalDate {
    require(diaVencimento in 1..31) {
        "O dia de vencimento deve estar entre 1 e 31."
    }

    return anoMes.atDay(
        diaVencimento.coerceAtMost(anoMes.lengthOfMonth())
    )
}

fun calcularDataFechamento(
    dataVencimento: LocalDate,
    diasAntesVencimento: Int
): LocalDate {
    require(diasAntesVencimento in 1..31) {
        "Os dias antes do vencimento devem estar entre 1 e 31."
    }

    return dataVencimento.minusDays(
        (diasAntesVencimento - 1).toLong()
    )
}

fun calcularVencimentoDaCompra(
    dataCompra: LocalDate,
    diasAntesVencimento: Int,
    diaVencimento: Int
): LocalDate {
    val anoMesCompra = YearMonth.from(dataCompra)

    var vencimentoCandidato = criarDataVencimento(
        anoMes = anoMesCompra,
        diaVencimento = diaVencimento
    )

    if (!vencimentoCandidato.isAfter(dataCompra)) {
        vencimentoCandidato = criarDataVencimento(
            anoMes = anoMesCompra.plusMonths(1),
            diaVencimento = diaVencimento
        )
    }

    val fechamentoCandidato = calcularDataFechamento(
        dataVencimento = vencimentoCandidato,
        diasAntesVencimento = diasAntesVencimento
    )

    return if (dataCompra < fechamentoCandidato) {
        vencimentoCandidato
    } else {
        criarDataVencimento(
            anoMes = YearMonth.from(vencimentoCandidato).plusMonths(1),
            diaVencimento = diaVencimento
        )
    }
}

fun calcularInicioCicloFatura(
    mesFatura: YearMonth,
    diasAntesVencimento: Int,
    diaVencimento: Int
): LocalDate {
    val vencimentoFaturaAnterior = criarDataVencimento(
        anoMes = mesFatura.minusMonths(1),
        diaVencimento = diaVencimento
    )

    return calcularDataFechamento(
        dataVencimento = vencimentoFaturaAnterior,
        diasAntesVencimento = diasAntesVencimento
    )
}

fun calcularFimExclusivoCicloFatura(
    mesFatura: YearMonth,
    diasAntesVencimento: Int,
    diaVencimento: Int
): LocalDate {
    val vencimentoFatura = criarDataVencimento(
        anoMes = mesFatura,
        diaVencimento = diaVencimento
    )

    return calcularDataFechamento(
        dataVencimento = vencimentoFatura,
        diasAntesVencimento = diasAntesVencimento
    )
}