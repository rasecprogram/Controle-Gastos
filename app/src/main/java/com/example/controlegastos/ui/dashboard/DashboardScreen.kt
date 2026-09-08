@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controlegastos.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.controlegastos.domain.model.DespesaDetalhada
import com.example.controlegastos.domain.model.GastoPorCategoria
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.*
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.YearMonth
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.delay
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.text.NumberFormat
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import com.example.controlegastos.ui.components.BarraNavegacaoInferior


private val CorCardSaldoAccent = Color(0xFF1B6B4A)
@Composable
fun DashboardScreen(
    onGerenciarCategorias: () -> Unit,
    onAdicionarDespesa: () -> Unit,
    onVerTodasTransacoes: () -> Unit,
    onVerProjecoes: () -> Unit,
    onVerPendencias: () -> Unit,
    onAbrirConfiguracoes: () -> Unit,
    onAbrirCartoes: () -> Unit,
    onNavegarEdicao: () -> Unit = {},       // novo callback (default para retrocompatibilidade)
    onNavegarGastos: () -> Unit = {},       // mantive como antes
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Dashboard é a tela inicial, logo o índice padrão é 0
    var selectedIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.carregarNomeUsuario()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.material3.Scaffold(
            topBar = {
                CardSaldoPrincipalNovo(
                    nomeUsuario = uiState.nomeUsuario,
                    saldoAtual = uiState.saldoPositivo,
                    receitas = uiState.totalReceitas,
                    gastos = uiState.resumoMensal.totalGasto,
                    totalFaturas = uiState.totalFaturas,
                    visivel = uiState.numerosVisiveis,
                    onAlternarVisibilidade = viewModel::alternarVisibilidadeValores,
                    onAbrirConfiguracoes = onAbrirConfiguracoes
                )
            }
        ) { innerPadding ->

            if (uiState.carregando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    ConteudoDashboard(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 12.dp),
                        uiState = uiState
                    )

                    FaturasProximas(
                        cartoes = uiState.cartoes,
                        visivel = uiState.numerosVisiveis,
                        onVerTodas = {
                            selectedIndex = 1
                            onVerTodasTransacoes()
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    PainelTransacoes(
                        transacoes = uiState.transacoesDoMes,
                        mesSelecionado = uiState.mesSelecionado.formatarMesAno(),
                        numerosVisiveis = uiState.numerosVisiveis,
                        cartoes = uiState.cartoes,
                        onVerTodas = {
                            selectedIndex = 1 // Transações corresponde ao índice 1
                            onVerTodasTransacoes()
                        }
                    )

                    Spacer(modifier = Modifier.height(110.dp))
                }
            }
        }

        BarraNavegacaoInferior(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedIndex = selectedIndex,
            onItemSelected = { index ->
                selectedIndex = index
                when (index) {
                    0 -> { /* Início - já na Dashboard */ }
                    1 -> onVerTodasTransacoes()
                    2 -> onVerPendencias()   // ou onNavegarGastos se preferir
                    3 -> onNavegarEdicao()    // chama explicitamente o novo callback
                }
            },
            onAdicionarDespesa = onAdicionarDespesa
        )
    }
}

@Composable
fun CardSaldoPrincipalNovo(
    nomeUsuario: String,
    saldoAtual: Long,
    receitas: Long,
    gastos: Long,
    totalFaturas: Long,
    visivel: Boolean,
    onAlternarVisibilidade: () -> Unit,
    onAbrirConfiguracoes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val corFundo = Color(0xFF135A3D)
    val textoClaro = Color.White
    val textoCinza = Color.White.copy(alpha = 0.55f)
    val miniCardBg = Color.White.copy(alpha = 0.08f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 40.dp, bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = corFundo),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-80).dp, y = 60.dp)
                    .background(Color.White.copy(alpha = 0.04f), shape = CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 100.dp, y = (-60).dp)
                    .background(Color.White.copy(alpha = 0.04f), shape = CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "INÍCIO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = textoCinza
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Olá, $nomeUsuario",
                                fontSize = 18.sp,
                                color = textoClaro,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "👋", fontSize = 18.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable { onAlternarVisibilidade() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (visivel) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = "Alternar visibilidade",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable { onAbrirConfiguracoes() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Configurações",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "SALDO DISPONÍVEL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = textoCinza
                )

                Text(
                    text = saldoAtual.formatarMoeda(visivel),
                    fontSize = 32.sp,
                    color = textoClaro,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )

                val aposPagar = (saldoAtual - totalFaturas).coerceAtLeast(0L)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "→ Após pagar faturas:",
                        color = textoCinza,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = aposPagar.formatarMoeda(visivel), // <--- USANDO `visivel` AQUI
                        color = textoClaro,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(miniCardBg)
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // RECEITAS
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(1f).padding(start = 14.dp, end = 2.dp)
                        ) {
                            Text(text = "RECEITAS", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = textoCinza)
                            Text(
                                text = receitas.formatarMoeda(visivel), // <--- USANDO `visivel` AQUI
                                fontSize = 13.sp,
                                color = Color(0xFF55D4A3),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.offset(y = (-3).dp)
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.15f)))

                        // GASTOS
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(1f).padding(start = 10.dp, end = 2.dp)
                        ) {
                            Text(text = "GASTOS", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = textoCinza)
                            Text(
                                text = gastos.formatarMoeda(visivel), // <--- USANDO `visivel` AQUI
                                fontSize = 13.sp,
                                color = Color(0xFFFF9A9A),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.offset(y = (-3).dp)
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.15f)))

                        // FATURAS
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(1f).padding(start = 10.dp, end = 2.dp)
                        ) {
                            Text(text = "FATURAS", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = textoCinza)
                            Text(
                                text = totalFaturas.formatarMoeda(visivel), // <--- USANDO `visivel` AQUI
                                fontSize = 13.sp,
                                color = Color(0xFFFFD166),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.offset(y = (-3).dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConteudoDashboard(
    modifier: Modifier,
    uiState: DashboardUiState
) {
    val tetoSoma: Long = uiState.gastosPorCategoria.mapNotNull { it.tetoMensal }.sum()

    val totalBudget: Long = when {
        tetoSoma > 0L -> tetoSoma
        uiState.resumoMensal.totalGasto > 0L -> uiState.resumoMensal.totalGasto * 2L
        else -> 0L
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        EstruturaGastosCard(
            gastosPorCategoria = uiState.gastosPorCategoria,
            totalGasto = uiState.resumoMensal.totalGasto,
            totalBudget = totalBudget,
            numerosVisiveis = uiState.numerosVisiveis,
            saldoDisponivel = uiState.saldoPositivo,  // ✅ Seu saldo de R$ 4.000,00
            totalDividas = uiState.totalFaturas,      // ✅ Suas faturas de R$ 954,82
            modifier = Modifier.fillMaxWidth()
        )

        // ---------- DESTAQUE FORA DO CARD PRINCIPAL ----------
        val maiorCategoria = uiState.gastosPorCategoria.maxByOrNull { it.totalGasto }
        if (maiorCategoria != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFFCCE9DE), // cor da borda — ajuste para ficar mais forte se necessário
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF8F3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💡", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sua maior despesa foi ${maiorCategoria.nomeCategoria} com ${maiorCategoria.totalGasto.formatarMoeda(uiState.numerosVisiveis)}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF136451),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EstruturaGastosCard(
    gastosPorCategoria: List<GastoPorCategoria>,
    totalGasto: Long,
    totalBudget: Long,
    numerosVisiveis: Boolean,
    modifier: Modifier = Modifier,
    saldoDisponivel: Long = 0L,      // ✅ NOVO: seu saldo disponível
    totalDividas: Long = 0L          // ✅ NOVO: faturas do mês
) {
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(gastosPorCategoria, totalGasto, totalBudget) {
        delay(120)
        animate = true
    }

    val totalCategorias = gastosPorCategoria.sumOf { it.totalGasto }

    val rawPercents = gastosPorCategoria.map { gasto ->
        if (totalCategorias > 0L) gasto.totalGasto.toFloat() / totalCategorias.toFloat() * 100f else 0f
    }
    val floorInts = rawPercents.map { kotlin.math.floor(it).toInt() }.toMutableList()
    var diff = 100 - floorInts.sum()
    if (diff > 0) {
        val remainders = rawPercents.mapIndexed { idx, v -> idx to (v - kotlin.math.floor(v)) }
            .sortedByDescending { it.second }
        var i = 0
        while (diff > 0 && i < remainders.size) {
            floorInts[remainders[i].first] = floorInts[remainders[i].first] + 1
            diff--
            i++
        }
    }
    val adjustedPercents = floorInts.toList()

    val mainDonutProgress by animateFloatAsState(
        targetValue = if (animate) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    val usedFraction = if (totalBudget > 0L) (totalGasto.toFloat() / totalBudget.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedUsed by animateFloatAsState(
        targetValue = if (animate) usedFraction else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESTRUTURA DE GASTOS · ${java.time.YearMonth.now().format(DateTimeFormatter.ofPattern("MMM", Locale("pt","BR"))).uppercase(Locale("pt","BR"))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8A929B),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(150.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val thickness = 22.dp.toPx()
                        var startAngle = -90f

                        drawArc(
                            color = Color(0xFFEEF0F2),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = thickness, cap = StrokeCap.Butt)
                        )

                        gastosPorCategoria.forEach { gasto ->
                            val sweep = if (totalCategorias > 0L) (gasto.totalGasto.toFloat() / totalCategorias.toFloat() * 360f) else 0f
                            if (sweep > 0f) {
                                val gap = if (sweep > 4f) 4f else 0f
                                drawArc(
                                    color = gasto.corHex.toComposeColor(),
                                    startAngle = startAngle + (gap / 2f),
                                    sweepAngle = (sweep * mainDonutProgress) - gap,
                                    useCenter = false,
                                    style = Stroke(width = thickness, cap = StrokeCap.Butt)
                                )
                                startAngle += sweep  // ✅ MOVIDO PARA DENTRO DO IF - ESTAVA FORA!
                            }
                        }
                    }

                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "ESTE MÊS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = totalGasto.formatarMoeda(numerosVisiveis), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "de ${totalBudget.formatarMoeda(numerosVisiveis)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(82.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 10.dp.toPx()
                            drawArc(
                                color = Color(0xFFEEF0F2),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )

                            // ✅ CORRIGIDO: Percentual baseado em FATURAS / SALDO
                            val percentualUsado = if (saldoDisponivel > 0L)
                                (totalDividas.toFloat() / saldoDisponivel.toFloat() * 100f).toInt().coerceIn(0, 100)
                            else 0

                            val sweep = 360f * (percentualUsado / 100f)
                            drawArc(
                                color = Color(0xFF1B6B4A),
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(start = 12.dp)) {
                            // ✅ CORRIGIDO: Mostra percentual de FATURAS / SALDO
                            val percentualUsado = if (saldoDisponivel > 0L)
                                (totalDividas.toFloat() / saldoDisponivel.toFloat() * 100f).toInt().coerceIn(0, 100)
                            else 0

                            Text(text = "$percentualUsado%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF143045))
                            Text(text = "usado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // ✅ CORRIGIDO: ORÇAMENTO = SALDO DISPONÍVEL
                    val restante = (saldoDisponivel - totalDividas).coerceAtLeast(0L)

                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(start = 12.dp)) {
                        Text(text = "ORÇAMENTO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = saldoDisponivel.formatarMoeda(numerosVisiveis), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF143045))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "de ${totalDividas.formatarMoeda(numerosVisiveis)} em faturas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = restante.formatarMoeda(numerosVisiveis), style = MaterialTheme.typography.bodySmall, color = Color(0xFF1B6B4A), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                val totalForBars = if (totalCategorias > 0L) totalCategorias.toFloat() else 1f

                gastosPorCategoria.forEachIndexed { idx, gasto ->
                    val fraction = if (totalForBars > 0f) gasto.totalGasto.toFloat() / totalForBars else 0f

                    val animatedFraction by animateFloatAsState(
                        targetValue = if (animate) fraction else 0f,
                        animationSpec = tween(durationMillis = 700 + idx * 80)
                    )

                    val displayPercent = adjustedPercents.getOrNull(idx) ?: 0

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        IconeCategoriaSimples(iconeChave = gasto.iconeChave, corHex = gasto.corHex)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(text = gasto.nomeCategoria, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = gasto.totalGasto.formatarMoeda(numerosVisiveis), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "($displayPercent%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(modifier = Modifier
                                    .fillMaxWidth(animatedFraction)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(gasto.corHex.toComposeColor())
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
// NOVO: Função para desenhar o ícone sem o quadrado de fundo, idêntico à Imagem 2
@Composable
private fun IconeCategoriaSimples(
    iconeChave: String?,
    corHex: String
) {
    val chave = iconeChave ?: ""
    val context = LocalContext.current
    val resId = remember(chave) {
        if (chave.isBlank()) 0 else context.resources.getIdentifier(chave, "drawable", context.packageName)
    }

    val cor = try {
        Color(android.graphics.Color.parseColor(corHex))
    } catch (_: Exception) {
        CorCategoriaFallback
    }

    if (resId != 0) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    } else if (chave.isNotBlank() && chave.any { it.code > 255 }) {
        Text(text = chave, fontSize = 18.sp)
    } else {
        val chaveDerivada = if (chave.isNotBlank()) chave else chaveDaCategoriaAPartirDoNome("")
        Icon(
            imageVector = iconeCategoria(chaveDerivada),
            contentDescription = null,
            tint = cor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun FaturasProximas(
    cartoes: List<com.example.controlegastos.domain.model.Cartao>,
    visivel: Boolean, // <--- ADICIONADO AQUI
    onVerTodas: () -> Unit,
    modifier: Modifier = Modifier,
    diasAvisoEmBreve: Int = 10
) {
    val ativos = cartoes.filter { it.ativo }
    if (ativos.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FATURAS PRÓXIMAS",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF9098A3),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Ver todas",
                modifier = Modifier
                    .clickable(onClick = onVerTodas)
                    .padding(vertical = 4.dp),
                color = Color(0xFF0F5141),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            ativos.forEach { cartao ->
                val (daysUntil, _) = calcularProximoVencimento(cartao.diaVencimento)
                val estaEmBreve = daysUntil <= diasAvisoEmBreve

                val borderColor = if (estaEmBreve) Color(0xFFFFE0B2) else Color(0xFFCFE2D8)
                val backgroundColor = if (estaEmBreve) Color(0xFFFFFBF2) else Color(0xFFEEF6F1)
                val subtitleColor = if (estaEmBreve) Color(0xFFD97706) else Color(0xFF136451)
                val badgeBgColor = if (estaEmBreve) Color(0xFFFFE8CC) else Color(0xFFD9E9DF)
                val badgeTextColor = if (estaEmBreve) Color(0xFFB45309) else Color(0xFF136451)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(
                            width = 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val context = LocalContext.current
                        val marcaKey = cartao.marcaChave.orEmpty().lowercase(Locale("pt","BR"))
                        val resId = remember(marcaKey) {
                            if (marcaKey.isBlank()) 0 else context.resources.getIdentifier(marcaKey, "drawable", context.packageName)
                        }

                        if (resId != 0) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = cartao.nome,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                )
                            }
                        } else {
                            val corBg = try {
                                val parsed = android.graphics.Color.parseColor(cartao.corHex)
                                Color(parsed)
                            } catch (_: Exception) {
                                when {
                                    cartao.nome.contains("C6", ignoreCase = true) -> Color(0xFF263238)
                                    cartao.nome.contains("Nu", ignoreCase = true) -> Color(0xFF8B3DFF)
                                    else -> Color(0xFF5F8D84)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(corBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cartao.marcaChave.takeIf { it.isNotBlank() }?.uppercase()?.take(2)
                                        ?: cartao.nome.firstOrNull()?.uppercase()?.toString() ?: "?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cartao.nome,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF143045)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            val textoVencimento = buildString {
                                append("Vence em ${daysUntil} dias")
                                append(" · ")
                                append(cartao.limiteCentavos.formatarMoeda(visivel)) // <--- USANDO `visivel` AQUI
                            }
                            Text(
                                text = textoVencimento,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = subtitleColor
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        val badgeText = if (estaEmBreve) "Em breve" else "${daysUntil}d"

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBgColor)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeTextColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
// Helper: calcula (daysUntil, dueDate)
private fun calcularProximoVencimento(diaVencimento: Int): Pair<Int, LocalDate> {
    val hoje = LocalDate.now()
    val day = diaVencimento.coerceAtLeast(1)
    // candidate no mês atual:
    val ultimoDiaMesAtual = YearMonth.from(hoje).lengthOfMonth()
    val diaAtualAjustado = day.coerceAtMost(ultimoDiaMesAtual)
    var candidate = hoje.withDayOfMonth(diaAtualAjustado)
    if (!candidate.isAfter(hoje)) {
        // pega próximo mês
        val proximoMes = hoje.plusMonths(1)
        val ultimoDiaProx = YearMonth.from(proximoMes).lengthOfMonth()
        val diaProxAjustado = day.coerceAtMost(ultimoDiaProx)
        candidate = proximoMes.withDayOfMonth(diaProxAjustado)
    }
    val daysUntil = ChronoUnit.DAYS.between(hoje, candidate).toInt()
    return Pair(daysUntil, candidate)
}

@Composable
private fun PainelTransacoes(
    transacoes: List<DespesaDetalhada>,
    mesSelecionado: String,
    numerosVisiveis: Boolean,
    cartoes: List<com.example.controlegastos.domain.model.Cartao>,
    onVerTodas: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp, max = 680.dp)
            .padding(horizontal = 16.dp)
    ) {
        // Cabeçalho ajustado
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = "ÚLTIMAS TRANSAÇÕES",
                style = MaterialTheme.typography.labelMedium, // Reduzido um pouco
                color = Color(0xFF8A929B),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = mesSelecionado,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB0B4BA),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Card com últimos 5 lançamentos
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val ultimos = transacoes
                    .sortedByDescending { it.dataCompra }
                    .take(5)

                if (ultimos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhuma transação recente", color = Color(0xFF8A929B))
                    }
                } else {
                    ultimos.forEachIndexed { index, desp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp), // Espaçamento vertical igual ao alvo
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ícone categoria
                            IconeCategoriaPill(
                                iconeChave = desp.categoriaIconeChave,
                                corHex = desp.categoriaCorHex
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Descrição + subtítulo
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = desp.descricao,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF143045), // Cor escura idêntica
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = desp.categoriaNome,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF9CA0A9) // Cinza claro
                                    )

                                    Text(
                                        text = "  ·  ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF9CA0A9),
                                        fontWeight = FontWeight.Bold
                                    )

                                    // RESTAURADO: Sua lógica original para buscar a logo no drawable
                                    val cartao = cartoes.firstOrNull { it.id == desp.cartaoId }
                                    if (cartao != null) {
                                        val context = LocalContext.current
                                        val marcaChaveLower = cartao.marcaChave.orEmpty().lowercase(Locale("pt", "BR"))
                                        val resIdCard = remember(marcaChaveLower) {
                                            if (marcaChaveLower.isBlank()) 0 else context.resources.getIdentifier(marcaChaveLower, "drawable", context.packageName)
                                        }
                                        if (resIdCard != 0) {
                                            // Carrega a imagem real se existir
                                            androidx.compose.foundation.Image(
                                                painter = painterResource(id = resIdCard),
                                                contentDescription = cartao.nome,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            // Fallback para a bolinha com a letra (estilo imagem 1)
                                            val initial = cartao.nome.firstOrNull()?.uppercase() ?: "C"
                                            val corBadge = when (initial) {
                                                "N" -> Color(0xFF6A1B9A)
                                                "I" -> Color(0xFFEF6C00)
                                                "C" -> Color(0xFF263238)
                                                else -> Color(0xFF5F8D84)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(corBadge),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = initial,
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    // Data sem o ponto final
                                    val dataCurta = try {
                                        Instant.ofEpochMilli(desp.dataCompra)
                                            .atZone(ZoneOffset.UTC)
                                            .format(DateTimeFormatter.ofPattern("d/MMM", Locale("pt", "BR")))
                                            .lowercase(Locale("pt", "BR"))
                                            .replace(".", "") // REMOVE O PONTO (ex: set. vira set)
                                    } catch (_: Exception) {
                                        ""
                                    }
                                    Text(
                                        text = dataCurta,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF9CA0A9)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Valor
                            Text(
                                text = desp.valor.formatarMoeda(numerosVisiveis),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF143045)
                            )
                        }

                        // Divisor APENAS DAQUI PARA FRENTE (alinhado com o texto, não de ponta a ponta)
                        if (index != ultimos.lastIndex) {
                            HorizontalDivider(
                                color = Color(0xFFF0F2F4),
                                thickness = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Divisor da área do botão (esse vai de ponta a ponta)
                    HorizontalDivider(
                        color = Color(0xFFF0F2F4),
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Ver todas as transações →",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onVerTodas)
                            .padding(vertical = 16.dp),
                        color = Color(0xFF1B6B4A),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private val CorCategoriaFallback = Color(0xFF5F8D84)

@Composable
private fun IconeCategoriaPill(
    iconeChave: String?,
    corHex: String
) {
    val chave = iconeChave ?: ""
    val context = LocalContext.current
    val resId = remember(chave) {
        if (chave.isBlank()) 0 else context.resources.getIdentifier(chave, "drawable", context.packageName)
    }

    val cor = try {
        Color(android.graphics.Color.parseColor(corHex))
    } catch (_: Exception) {
        CorCategoriaFallback
    }

    if (resId != 0) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cor.copy(alpha = 0.12f))
                .padding(6.dp)
        )
    } else if (chave.isNotBlank() && chave.any { it.code > 255 }) {
        // emoji
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = chave, fontSize = 18.sp)
        }
    } else {
        // fallback para ícone vetorial mapeado a partir da chave (ou name derivado)
        val chaveDerivada = if (chave.isNotBlank()) chave else chaveDaCategoriaAPartirDoNome("")
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconeCategoria(chaveDerivada),
                contentDescription = null,
                tint = cor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun String?.ehEmoji(): Boolean {
    return this?.any { caractere -> caractere.code > 255 } ?: false
}

private fun chaveDaCategoriaAPartirDoNome(nome: String): String {
    val m = nome.lowercase(Locale("pt", "BR"))
        .replace("ç", "c")
        .replace("ã", "a")
        .replace("õ", "o")
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .replace(Regex("[^a-z0-9]"), "_")
        .replace(Regex("_+"), "_")
        .trim('_')

    val mapFallback = mapOf(
        "viagem" to "viagem",
        "alimentacao" to "alimentacao",
        "alimentação" to "alimentacao",
        "fast_food" to "fastfood",
        "streaming" to "streaming",
        "academia" to "academia",
        "transporte" to "transporte",
        "contas_da_casa" to "contas",
        "contas" to "contas",
        "saude" to "saude",
        "lazer" to "lazer",
        "assinaturas" to "assinaturas",
        "pets" to "pets",
        "presentes" to "presentes",
        "moradia" to "moradia"
    )

    return mapFallback[m] ?: m
}

private fun iconeCategoria(chave: String): ImageVector = when (chave) {
    "alimentacao", "alimentação", "fastfood" -> Icons.Default.Fastfood
    "loja_online" -> Icons.Default.ShoppingBag
    "streaming" -> Icons.Default.Videocam
    "academia" -> Icons.Default.FitnessCenter
    "transporte" -> Icons.Default.DirectionsCar
    "moradia" -> Icons.Default.Home
    "saude" -> Icons.Default.Favorite
    "educacao" -> Icons.Default.School
    "lazer" -> Icons.Default.Celebration
    "assinaturas" -> Icons.Default.Subscriptions
    "pets" -> Icons.Default.Pets
    "presentes" -> Icons.Default.CardGiftcard
    "viagem" -> Icons.Default.Flight
    "contas", "contas_da_casa" -> Icons.Default.ReceiptLong
    else -> Icons.Default.Category
}

@Composable
private fun EstadoVazio(titulo: String, descricao: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(descricao, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


private fun Long.formatarMoeda(numerosVisiveis: Boolean): String {
    if (!numerosVisiveis) return "R$ •••••"
    val nf = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    // nf.format(…) pode inserir um NBSP (non-breaking space) entre o símbolo e o número.
    // Substituímos por espaço normal para evitar quebras inesperadas no layout.
    return nf.format(this / 100.0).replace('\u00A0', ' ')
}

private fun Long.formatarData(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneOffset.UTC)
    .toLocalDate()
    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR")))

private fun java.time.YearMonth.formatarMesAno(): String = format(
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))
).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }

private fun String.toComposeColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (_: IllegalArgumentException) {
    Color.Gray
}