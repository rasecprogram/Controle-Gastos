
package com.example.controlegastos.ui.transacoes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.controlegastos.domain.model.DespesaDetalhada
import com.example.controlegastos.domain.model.TipoLancamento

// =======================================================================
// Domínio: Despesas Fixas
// Agrupa o card de resumo/lista de despesas recorrentes do mês, que é
// uma seção independente da tela e muda de forma isolada das demais.
// =======================================================================

@Composable
internal fun CardDespesasFixas(
    despesas: List<DespesaDetalhada>,
    visivel: Boolean,
    expandida: Boolean
) {
    val total = despesas.sumOf { it.valor }
    val formatoCard = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = formatoCard
            ),
        shape = formatoCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column {
            // Este resumo fica sempre visível, mesmo com a lista fechada.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 14.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = LocalContext.current

                val calendarRecRes = remember {
                    context.resources.getIdentifier(
                        "calendar_rec",
                        "drawable",
                        context.packageName
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0F4EF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (calendarRecRes != 0) {
                        Image(
                            painter = painterResource(
                                id = calendarRecRes
                            ),
                            contentDescription = "Total recorrente",
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Total recorrente",
                            tint = CorPrincipal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Total recorrente",
                        color = CorTexto,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${despesas.size} despesas ativas",
                        color = CorTexto.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = total.formatarMoeda(visivel),
                    color = CorTexto,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Este divisor também fica sempre visível.
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF0F2F1),
                thickness = 1.dp
            )

            // Somente a lista de despesas é ocultada/revelada.
            if (expandida) {
                despesas.forEachIndexed { index, despesa ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconeCategoriaDoCardLocal(
                            iconeChave = despesa.categoriaIconeChave ?: "",
                            nomeCategoria = despesa.categoriaNome,
                            corHex = despesa.categoriaCorHex
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = despesa.descricao,
                                color = CorTexto,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )

                            val tipoTexto = when (
                                despesa.tipoLancamento
                            ) {
                                TipoLancamento.FIXA -> "Mensal"
                                TipoLancamento.PARCELADA -> "Parcelada"
                                TipoLancamento.UNICA -> "Única"
                            }

                            Text(
                                text = tipoTexto,
                                color = CorTexto.copy(alpha = 0.55f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = despesa.valor.formatarMoeda(visivel),
                            color = Color(0xFF65707A),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (index != despesas.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF0F2F1),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}