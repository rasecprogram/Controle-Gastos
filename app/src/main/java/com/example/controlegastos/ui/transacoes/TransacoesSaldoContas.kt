package com.example.controlegastos.ui.transacoes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.controlegastos.domain.model.ContaSaldo
import com.example.controlegastos.domain.model.TipoContaSaldo

// =======================================================================
// Domínio: Saldo & Contas
// Agrupa o card de saldo principal, o card de cada conta e o fluxo
// completo de transferência entre contas (diálogo + item de seleção).
// =======================================================================

@Composable
internal fun CardSaldoPrincipal(
    saldoAtual: Long,
    saldoInicial: Long,
    despesas: Long,
    visivel: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CorCardSaldoDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Círculos decorativos
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 24.dp, y = (-40).dp)
                    .background(CorCardSaldoAccent.copy(alpha = 0.18f), shape = CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 64.dp, y = (-8).dp)
                    .background(Color.White.copy(alpha = 0.03f), shape = CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "Saldo disponível",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = saldoAtual.formatarMoeda(visivel),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bloco Receitas
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CorFundoIconeReceita
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = CorReceitaValor,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Receitas",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = saldoInicial.formatarMoeda(visivel),
                            color = CorReceitaValor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Divisor Vertical
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .width(1.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    // Bloco Despesas
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CorFundoIconeDespesa
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = CorDespesaValor,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Despesas",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = despesas.formatarMoeda(visivel),
                            color = CorDespesaValor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CardConta(
    conta: ContaSaldo,
    visivel: Boolean
) {
    // obter contexto para buscar drawable pelo nome
    val context = LocalContext.current

    val logoRes = remember(conta.instituicaoChave) {
        val nomeArquivo = if (
            conta.instituicaoChave.contains("caixa", ignoreCase = true) ||
            conta.instituicaoChave.equals("cx", ignoreCase = true)
        ) {
            "cef"
        } else {
            conta.instituicaoChave
        }

        context.resources.getIdentifier(nomeArquivo, "drawable", context.packageName)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge circular com ícone ou iniciais
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0F4EF)),
                contentAlignment = Alignment.Center
            ) {
                if (logoRes != 0) {
                    Icon(
                        painter = painterResource(id = logoRes),
                        contentDescription = conta.nome,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = conta.nome.take(2).uppercase(),
                        color = conta.corHex.toColor(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conta.nome,
                    color = Color(0xFF123C3A),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (conta.tipo) {
                        TipoContaSaldo.CONTA -> "Conta bancária"
                        TipoContaSaldo.CARTEIRA -> "Carteira"
                        TipoContaSaldo.SALDO_RESERVADO -> "Cofre"
                    },
                    color = Color(0xFF7D8B88),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = conta.saldoCentavos.formatarMoeda(visivel),
                color = Color(0xFF123C3A),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
internal fun DialogTransferenciaSaldo(
    contas: List<ContaSaldo>,
    onDismiss: () -> Unit,
    onConfirmarTransferencia: (
        contaOrigemId: Int,
        contaDestinoId: Int,
        valorCentavos: Long
    ) -> Unit
) {
    var contaOrigem by remember {
        mutableStateOf<ContaSaldo?>(null)
    }

    var contaDestino by remember {
        mutableStateOf<ContaSaldo?>(null)
    }

    var textoValor by remember {
        mutableStateOf("")
    }

    val valorCentavos = textoValor.paraCentavos()

    val origemValida = contaOrigem != null
    val destinoValido = contaDestino != null
    val contasDiferentes = contaOrigem?.id != contaDestino?.id
    val possuiSaldoSuficiente =
        contaOrigem?.saldoCentavos?.let { saldoOrigem ->
            valorCentavos > 0L && valorCentavos <= saldoOrigem
        } ?: false

    val podeTransferir =
        origemValida &&
                destinoValido &&
                contasDiferentes &&
                possuiSaldoSuficiente

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Transferência",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CorTexto
                        )

                        Spacer(
                            modifier = Modifier.height(3.dp)
                        )

                        Text(
                            text = "Mova valores entre contas, carteira e cofre",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7D8B88)
                        )
                    }

                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color(0xFF65707A)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Text(
                    text = "DE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFF7D8B88)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                contas.forEach { conta ->
                    OpcaoContaTransferencia(
                        conta = conta,
                        selecionada = conta.id == contaOrigem?.id,
                        onClick = {
                            contaOrigem = conta

                            if (contaDestino?.id == conta.id) {
                                contaDestino = null
                            }
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "PARA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFF7D8B88)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                contas.forEach { conta ->
                    OpcaoContaTransferencia(
                        conta = conta,
                        selecionada = conta.id == contaDestino?.id,
                        desabilitada = conta.id == contaOrigem?.id,
                        onClick = {
                            contaDestino = conta
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                OutlinedTextField(
                    value = textoValor.formatarValorTransferencia(),
                    onValueChange = { novoTexto ->
                        textoValor = novoTexto.filter { caractere ->
                            caractere.isDigit()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Valor da transferência")
                    },
                    placeholder = {
                        Text("Ex.: 500,00")
                    },
                    prefix = {
                        Text("R$ ")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CorPrincipal,
                        focusedLabelColor = CorPrincipal,
                        cursorColor = CorPrincipal
                    )
                )

                if (contaOrigem != null) {
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Disponível em ${contaOrigem?.nome}: " +
                                "${contaOrigem?.saldoCentavos?.formatarMoeda(true)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7D8B88)
                    )
                }

                if (
                    contaOrigem != null &&
                    contaDestino != null &&
                    !contasDiferentes
                ) {
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Selecione contas diferentes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD84315)
                    )
                }

                if (
                    contaOrigem != null &&
                    valorCentavos > contaOrigem!!.saldoCentavos
                ) {
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "O valor é maior que o saldo disponível na origem.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD84315)
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFFE1E7E3)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val origem = contaOrigem ?: return@Button
                            val destino = contaDestino ?: return@Button

                            onConfirmarTransferencia(
                                origem.id,
                                destino.id,
                                valorCentavos
                            )
                        },
                        enabled = podeTransferir,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CorPrincipal,
                            disabledContainerColor = Color(0xFFB8CECA),
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Transferir",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun OpcaoContaTransferencia(
    conta: ContaSaldo,
    selecionada: Boolean,
    desabilitada: Boolean = false,
    onClick: () -> Unit
) {
    val corBorda = when {
        desabilitada -> Color(0xFFE8ECEA)
        selecionada -> CorPrincipal
        else -> Color(0xFFE1E7E3)
    }

    val corFundo = when {
        desabilitada -> Color(0xFFF7F8F7)
        selecionada -> Color(0xFFEAF4EF)
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(
                enabled = !desabilitada,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = corFundo
        ),
        border = BorderStroke(
            width = if (selecionada) 2.dp else 1.dp,
            color = corBorda
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0F4EF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (conta.tipo) {
                        TipoContaSaldo.CONTA -> "🏦"
                        TipoContaSaldo.CARTEIRA -> "👛"
                        TipoContaSaldo.SALDO_RESERVADO -> "💰"
                    },
                    fontSize = 19.sp
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = conta.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (desabilitada) {
                        Color(0xFFB2BCB7)
                    } else {
                        CorTexto
                    }
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = when (conta.tipo) {
                        TipoContaSaldo.CONTA -> "Conta"
                        TipoContaSaldo.CARTEIRA -> "Carteira"
                        TipoContaSaldo.SALDO_RESERVADO -> "Cofre"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7D8B88)
                )
            }

            Text(
                text = conta.saldoCentavos.formatarMoeda(true),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (desabilitada) {
                    Color(0xFFB2BCB7)
                } else {
                    CorTexto
                }
            )
        }
    }
}