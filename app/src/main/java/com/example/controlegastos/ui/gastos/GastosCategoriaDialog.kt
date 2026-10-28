package com.example.controlegastos.ui.gastos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.controlegastos.domain.model.Cartao
import com.example.controlegastos.domain.model.DespesaDetalhada
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DetalheCategoriaUiState(
    val carregando: Boolean = true,
    val despesas: List<DespesaDetalhada> = emptyList(),
    val cartoes: List<Cartao> = emptyList()
)

@Composable
internal fun DialogoGastosCategoria(
    categoriaNome: String,
    mesSelecionado: YearMonth,
    estado: DetalheCategoriaUiState,
    onFechar: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val alturaMaxima = (configuration.screenHeightDp * 0.85f).dp

    val cartoesPorId = remember(estado.cartoes) {
        estado.cartoes.associateBy { cartao ->
            cartao.id
        }
    }

    val totalCentavos = estado.despesas.sumOf { despesa ->
        despesa.valor
    }

    val mesFormatado = remember(mesSelecionado) {
        mesSelecionado.format(
            DateTimeFormatter.ofPattern(
                "MMMM yyyy",
                Locale("pt", "BR")
            )
        )
    }

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = alturaMaxima),
                shape = RoundedCornerShape(
                    topStart = 26.dp,
                    topEnd = 26.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE1E5E8))
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 24.dp,
                                end = 16.dp,
                                bottom = 14.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Gastos — $categoriaNome",
                                color = Color(0xFF123C3A),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(3.dp))

                            Text(
                                text = mesFormatado,
                                color = Color(0xFF7D8B88),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton(
                            onClick = onFechar
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF4F6F8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar",
                                    tint = Color(0xFF7C8795),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFFE9ECEF)
                    )

                    if (estado.carregando) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF225E43)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            contentPadding = PaddingValues(
                                start = 24.dp,
                                end = 24.dp,
                                top = 20.dp,
                                bottom = 12.dp
                            ),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            if (estado.despesas.isEmpty()) {
                                item {
                                    Text(
                                        text = "Nenhum lançamento " +
                                            "nesta categoria no mês selecionado.",
                                        color = Color(0xFF7D8B88),
                                        modifier = Modifier.padding(
                                            vertical = 20.dp
                                        )
                                    )
                                }
                            }

                            items(
                                items = estado.despesas,
                                key = { despesa -> despesa.id }
                            ) { despesa ->
                                LancamentoCategoriaCard(
                                    despesa = despesa,
                                    cartaoNome = cartoesPorId[
                                        despesa.cartaoId
                                    ]?.nome
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 24.dp,
                                    vertical = 10.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (estado.despesas.size == 1) {
                                    "1 lançamento"
                                } else {
                                    "${estado.despesas.size} lançamentos"
                                },
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF7D8B88),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = formatarMoedaDetalheCategoria(
                                    totalCentavos
                                ),
                                color = Color(0xFF123C3A),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Button(
                        onClick = onFechar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 8.dp,
                                bottom = 20.dp
                            )
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF225E43),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Fechar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LancamentoCategoriaCard(
    despesa: DespesaDetalhada,
    cartaoNome: String?
) {
    val context = LocalContext.current
    val chave = despesa.categoriaIconeChave.orEmpty()

    val resId = remember(chave, context) {
        if (chave.isBlank()) {
            0
        } else {
            context.resources.getIdentifier(
                chave.lowercase(Locale.ROOT),
                "drawable",
                context.packageName
            )
        }
    }

    val formaPagamento = cartaoNome
        ?: despesa.cartaoId?.let { id ->
            "Cartão #$id"
        }
        ?: when (despesa.origemPagamento?.name) {
            "CARTEIRA" -> "Carteira"
            "COFRE" -> "Cofre"
            "CONTA" -> "Conta"
            else -> "Sem cartão"
        }

    val data = remember(despesa.dataCompra) {
        Instant.ofEpochMilli(despesa.dataCompra)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(
                DateTimeFormatter.ofPattern(
                    "dd/MM",
                    Locale("pt", "BR")
                )
            )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFFE4E7EA)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF7F8F7)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    resId != 0 -> {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = despesa.categoriaNome,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    chave.any { it.code > 255 } -> {
                        Text(
                            text = chave,
                            fontSize = 22.sp
                        )
                    }

                    else -> {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = despesa.categoriaNome,
                            tint = Color(0xFF2F6F62),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = despesa.descricao,
                    color = Color(0xFF123C3A),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    text = "${despesa.categoriaNome} · $formaPagamento",
                    color = Color(0xFF7D8B88),
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "$data · " +
                        if (despesa.statusPago) "Pago" else "Pendente",
                    color = Color(0xFF7D8B88),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.width(10.dp))

            Text(
                text = formatarMoedaDetalheCategoria(despesa.valor),
                color = Color(0xFF123C3A),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}

private fun formatarMoedaDetalheCategoria(
    centavos: Long
): String {
    return NumberFormat
        .getCurrencyInstance(Locale("pt", "BR"))
        .format(
            BigDecimal.valueOf(centavos, 2)
        )
}