package com.example.controlegastos.domain.usecase

import com.example.controlegastos.domain.util.calcularVencimentoDaCompra
import java.time.LocalDate
import javax.inject.Inject

class CalcularVencimentoCartaoUseCase @Inject constructor() {

    operator fun invoke(
        dataCompra: LocalDate,
        diasAntesVencimento: Int,
        diaVencimento: Int
    ): LocalDate {
        return calcularVencimentoDaCompra(
            dataCompra = dataCompra,
            diasAntesVencimento = diasAntesVencimento,
            diaVencimento = diaVencimento
        )
    }
}