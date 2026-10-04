package com.example.controlegastos.ui.transacoes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------
// Cores compartilhadas por todos os componentes de Transações.
// Usadas pelos domínios de Saldo/Contas, Fatura/Cartão e Despesas Fixas.
// ---------------------------------------------------------------------

internal val CorFundoApp = Color(0xFFECF0ED)
internal val CorPrincipal = Color(0xFF1B5B3A)
internal val CorTexto = Color(0xFF123C3A)
internal val CorFundoSaldo = Color(0xFFE1EBE7)
internal val CorCard = Color(0xFFE6EFEA)

// Cores do Card Principal de saldo
internal val CorCardSaldoDark = Color(0xFF0E3B36)
internal val CorCardSaldoAccent = Color(0xFF154C45)
internal val CorReceitaValor = Color(0xFF75E2A8)
internal val CorDespesaValor = Color(0xFFFF9E80)
internal val CorFundoIconeReceita = Color(0xFF1B4D3E)
internal val CorFundoIconeDespesa = Color(0xFF503431)

// ---------------------------------------------------------------------
// Pequenos componentes reutilizados por mais de um domínio da tela
// ---------------------------------------------------------------------

@Composable
internal fun TituloSecao(texto: String) {
    Text(
        text = texto.uppercase(),
        color = Color(0xFF8A9A9A),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp
    )
}

@Composable
internal fun TextoVazio(texto: String) {
    Text(
        text = texto,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        color = CorTexto.copy(alpha = 0.7f)
    )
}

/**
 * Ícone circular de categoria usado tanto pelo card de Despesas Fixas
 * quanto pela lista de lançamentos do diálogo "Ver fatura".
 */
@Composable
internal fun IconeCategoriaDoCardLocal(
    iconeChave: String,
    nomeCategoria: String,
    corHex: String?
) {
    val context = LocalContext.current
    val chave = iconeChave.ifBlank { "" }
    val resId = remember(chave) {
        context.resources.getIdentifier(chave.lowercase(), "drawable", context.packageName)
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF7F8F7)),
        contentAlignment = Alignment.Center
    ) {
        if (resId != 0) {
            Icon(
                painter = painterResource(id = resId),
                contentDescription = nomeCategoria,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        } else if (chave.ehEmojiLocal()) {
            Text(text = chave, fontSize = 18.sp)
        }
    }
}