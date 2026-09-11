@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controlegastos.ui.transacoes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.controlegastos.domain.model.ContaSaldo
import com.example.controlegastos.domain.model.DespesaDetalhada
import com.example.controlegastos.domain.model.FaturaCartao
import com.example.controlegastos.domain.model.TipoContaSaldo
import com.example.controlegastos.domain.model.TipoLancamento
import com.example.controlegastos.ui.components.BarraNavegacaoInferior
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextAlign


private val CorFundoApp = Color(0xFFECF0ED)
private val CorPrincipal = Color(0xFF1B5B3A)
private val CorTexto = Color(0xFF123C3A)
private val CorFundoSaldo = Color(0xFFE1EBE7)
private val CorCard = Color(0xFFE6EFEA)

// Cores do Card Principal
private val CorCardSaldoDark = Color(0xFF0E3B36)
private val CorCardSaldoAccent = Color(0xFF154C45)
private val CorReceitaValor = Color(0xFF75E2A8)
private val CorDespesaValor = Color(0xFFFF9E80)
private val CorFundoIconeReceita = Color(0xFF1B4D3E)
private val CorFundoIconeDespesa = Color(0xFF503431)
val CorConfirmarPagamento = Color(0xFF225E43)

@Composable
fun TransacoesScreen(
    onVoltar: () -> Unit,
    onNavegarInicio: () -> Unit = {},
    onNavegarTransacoes: () -> Unit = {},
    onNavegarGastos: () -> Unit = {},
    onNavegarEdicao: () -> Unit = {},
    onAdicionarDespesa: () -> Unit = {},
    viewModel: TransacoesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var faturaParaPagar by remember { mutableStateOf<FaturaCartao?>(null) }
    var contaSelecionada by remember { mutableStateOf<ContaSaldo?>(null) }
    var processandoPagamento by remember { mutableStateOf(false) }
    var faturaParaVer by remember { mutableStateOf<FaturaCartao?>(null) }

    // Transações está na posição 1 da barra de navegação inferior
    var selectedIndex by remember { mutableStateOf(1) }

    val faturas = if (uiState.abaSelecionada == AbaFaturas.ABERTAS) {
        uiState.faturasAbertas
    } else {
        uiState.faturasFechadas
    }

    val totalFaturas = faturas.sumOf { it.totalCentavos }

    val contasDisponiveis = uiState.contas.filter {
        it.tipo != TipoContaSaldo.SALDO_RESERVADO
    }

    // 1. ROOT BOX PARA PERMITIR FIXAR A BARRA DE NAVEGAÇÃO NO RODAPÉ
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CorFundoApp)
    ) {
        var despesasFixasExpandidas by remember {
            mutableStateOf(false)
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .blur(
                    radius = if (
                        faturaParaPagar != null ||
                        faturaParaVer != null
                    ) {
                        10.dp
                    } else {
                        0.dp
                    }
                ),
            containerColor = CorFundoApp,
            topBar = {
                TopBarTransacoes(
                    onVoltar = onVoltar,
                    onToggleValores = viewModel::alternarValores,
                    valoresVisiveis = uiState.valoresVisiveis
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                // 3. PADDING INFERIOR ADICIONADO PARA O CONTEÚDO NÃO FICAR COBERTO PELA BARRA
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 110.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "mes") {
                    SeletorMes(
                        mes = uiState.mesSelecionado,
                        onAnterior = viewModel::mesAnterior,
                        onProximo = viewModel::proximoMes
                    )
                }

                item(key = "saldo") {
                    CardSaldoPrincipal(
                        saldoAtual = uiState.saldoAtualTotal,
                        saldoInicial = uiState.saldoInicialTotal,
                        despesas = uiState.despesasDoMesTotal,
                        visivel = uiState.valoresVisiveis
                    )
                }

                item(key = "titulo_contas") {
                    TituloSecao(texto = "Contas")
                }

                items(
                    items = uiState.contas,
                    key = { "conta_${it.id}" }
                ) { conta ->
                    CardConta(
                        conta = conta,
                        visivel = uiState.valoresVisiveis
                    )
                }

                val totalContas = uiState.contas.sumOf { it.saldoCentavos }

                item(key = "total_em_contas") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE9EFEA),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFFD4E0D8)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 14.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total em contas",
                                color = Color(0xFF0F5A4A),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 15.sp
                                ),
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = totalContas.formatarMoeda(
                                    uiState.valoresVisiveis
                                ),
                                color = Color(0xFF0F5A4A),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 15.sp
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item(key = "titulo_cartoes") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TituloSecao(texto = "Cartão de crédito")

                        Spacer(modifier = Modifier.weight(1f))

                        val abertosCount = uiState.faturasAbertas.size

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFFF4F2),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFFFFD6CF)
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$abertosCount fatura(s) em aberto",
                                    color = Color(0xFFB33A27),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                item(key = "abas_faturas") {
                    AbasFaturas(
                        selecionada = uiState.abaSelecionada,
                        onSelecionar = viewModel::selecionarAbaFaturas,
                        abertosCount = uiState.faturasAbertas.size,
                        fechadosCount = uiState.faturasFechadas.size
                    )
                }

                if (uiState.abaSelecionada == AbaFaturas.ABERTAS) {
                    item(key = "total_faturas") {
                        CardTotalFaturas(
                            total = totalFaturas,
                            quantidade = faturas.size,
                            visivel = uiState.valoresVisiveis,
                            aba = uiState.abaSelecionada
                        )
                    }
                }

                if (faturas.isEmpty()) {
                    item(key = "faturas_vazias") {
                        TextoVazio(
                            texto = if (
                                uiState.abaSelecionada == AbaFaturas.ABERTAS
                            ) {
                                "Nenhuma fatura aberta neste mês."
                            } else {
                                "Nenhuma fatura fechada neste mês."
                            }
                        )
                    }
                }

                items(
                    items = faturas,
                    key = {
                        "fatura_${uiState.abaSelecionada}_${it.cartao.id}_${it.mesAno}"
                    }
                ) { fatura ->
                    CardFaturaCompleta(
                        fatura = fatura,
                        expandida = fatura.cartao.id in uiState.cartoesExpandidos,
                        visivel = uiState.valoresVisiveis,
                        onExpandir = {
                            faturaParaVer = fatura
                        },
                        onPagar = {
                            faturaParaPagar = fatura
                            contaSelecionada = null
                        },
                        onVerFatura = {
                            faturaParaVer = fatura
                        }
                    )
                }

                item(key = "titulo_fixas") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                despesasFixasExpandidas =
                                    !despesasFixasExpandidas
                            }
                            .padding(
                                horizontal = 4.dp,
                                vertical = 0.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DESPESAS FIXAS",
                            color = Color(0xFF8A929B),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "${
                                uiState.despesasFixas
                                    .sumOf { it.valor }
                                    .formatarMoeda(uiState.valoresVisiveis)
                            }/mês",
                            color = Color(0xFF707983),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = if (despesasFixasExpandidas) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = if (despesasFixasExpandidas) {
                                "Recolher despesas fixas"
                            } else {
                                "Expandir despesas fixas"
                            },
                            tint = Color(0xFF9AA3A9),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                item(key = "card_fixas") {
                    CardDespesasFixas(
                        despesas = uiState.despesasFixas,
                        visivel = uiState.valoresVisiveis,
                        expandida = despesasFixasExpandidas
                    )
                }
            }
        }

        // 2. BARRA DE NAVEGAÇÃO INFERIOR FIXADA NA PARTE INFERIOR DO BOX
        BarraNavegacaoInferior(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedIndex = selectedIndex,
            onItemSelected = { index ->
                selectedIndex = index
                when (index) {
                    0 -> onNavegarInicio()
                    1 -> onNavegarTransacoes()
                    2 -> onNavegarGastos()
                    3 -> onNavegarEdicao()
                }
            },
            onAdicionarDespesa = onAdicionarDespesa
        )
    }

    faturaParaVer?.let { fatura ->
        DialogoVerFatura(
            fatura = fatura,
            despesasFixasDoMes = uiState.despesasFixas,
            visivel = true,
            onFechar = {
                faturaParaVer = null
            }
        )
    }

    faturaParaPagar?.let { fatura ->
        DialogoPagamento(
            fatura = fatura,
            contas = contasDisponiveis,
            selecionada = contaSelecionada,
            visivel = true,
            processando = processandoPagamento,
            onSelecionarConta = { conta ->
                contaSelecionada = conta
            },
            onCancelar = {
                if (!processandoPagamento) {
                    faturaParaPagar = null
                    contaSelecionada = null
                }
            },
            onConfirmar = {
                val conta = contaSelecionada ?: return@DialogoPagamento

                processandoPagamento = true

                viewModel.pagarFatura(
                    cartaoId = fatura.cartao.id,
                    contaId = conta.id
                ) { erro ->
                    processandoPagamento = false

                    if (erro == null) {
                        faturaParaPagar = null
                        contaSelecionada = null

                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Fatura paga com sucesso."
                            )
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(erro)
                        }
                    }
                }
            }
        )
    }

    faturaParaVer?.let { f ->
        DialogoVerFatura(
            fatura = f,
            despesasFixasDoMes = uiState.despesasFixas,
            visivel = uiState.valoresVisiveis,
            onFechar = { faturaParaVer = null }
        )
    }
}


@Composable
private fun TopBarTransacoes(
    onVoltar: () -> Unit,
    onToggleValores: () -> Unit,
    valoresVisiveis: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECF0ED)) // Cor de fundo no mesmo padrão
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Botão de Voltar
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, Color(0xFFE6EFEA)),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onVoltar() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color(0xFF2F6F62),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Título e Subtítulo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Transações",
                    color = Color(0xFF123C3A),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Acompanhe suas movimentações", // Subtítulo para manter a estrutura visual do design
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Botão de Visibilidade (Olho) na direita
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, Color(0xFFE6EFEA)),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onToggleValores() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (valoresVisiveis) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Ocultar ou mostrar valores",
                        tint = Color(0xFF2F6F62),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SeletorMes(
    mes: YearMonth,
    onAnterior: () -> Unit,
    onProximo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onAnterior,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Mês anterior",
                tint = CorTexto,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = mes.formatarMes(),
            color = CorTexto,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onProximo,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Próximo mês",
                tint = CorTexto,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CardSaldoPrincipal(
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
                    .zIndex(0f)
            )
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 64.dp, y = (-8).dp)
                    .background(Color.White.copy(alpha = 0.03f), shape = CircleShape)
                    .zIndex(0f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .zIndex(1f)
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

// Substituir a função existente TituloSecao por esta
@Composable
private fun TituloSecao(texto: String) {
    Text(
        text = texto.uppercase(), // imagem mostra texto em caixa alta pequena
        color = Color(0xFF8A9A9A), // tom de cinza
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun CardConta(
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
                        TipoContaSaldo.SALDO_RESERVADO -> "Saldo reservado"
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
private fun AbasFaturas(
    selecionada: AbaFaturas,
    onSelecionar: (AbaFaturas) -> Unit,
    abertosCount: Int,
    fechadosCount: Int
) {
    // Card externo que engloba as duas abas
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(30),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp), // Espaçamento interno para a aba verde não encostar na borda
            verticalAlignment = Alignment.CenterVertically
        ) {
            @Composable
            fun AbaItem(
                titulo: String,
                quantidade: Int,
                isSelecionada: Boolean,
                onClick: () -> Unit
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(30))
                        .clickable { onClick() },
                    color = if (isSelecionada) Color(0xFF225F44) else Color.Transparent, // Verde escuro se selecionada
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = titulo,
                            color = if (isSelecionada) Color.White else Color(0xFF7D8B99),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Badge com a quantidade
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = if (isSelecionada) Color.White.copy(alpha = 0.2f) else Color(
                                        0xFFF3F4F6
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = quantidade.toString(),
                                color = if (isSelecionada) Color.White else Color(0xFF7D8B99),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Aba Faturas Abertas
            AbaItem(
                titulo = "Faturas Abertas",
                quantidade = abertosCount,
                isSelecionada = selecionada == AbaFaturas.ABERTAS,
                onClick = { onSelecionar(AbaFaturas.ABERTAS) }
            )

            // Aba Faturas Fechadas
            AbaItem(
                titulo = "Faturas Fechadas",
                quantidade = fechadosCount,
                isSelecionada = selecionada == AbaFaturas.FECHADAS,
                onClick = { onSelecionar(AbaFaturas.FECHADAS) }
            )
        }
    }
}

@Composable
private fun CardTotalFaturas(
    total: Long,
    quantidade: Int,
    visivel: Boolean,
    aba: AbaFaturas
) {
    val isAberto = aba == AbaFaturas.ABERTAS

    // Cores baseadas na aba selecionada (quente/laranja para abertas, verde/frio para fechadas)
    val bgColor = if (isAberto) Color(0xFFFFF9E5) else Color(0xFFE9EFEA)
    val borderColor = if (isAberto) Color(0xFFFFE0B2) else Color(0xFFD4E0D8)
    val textColorMain = if (isAberto) Color(0xFFC25501) else Color(0xFF0F5A4A)
    val textColorSub = if (isAberto) Color(0xFFE58735) else Color(0xFF4A7D71)

    val titulo = if (isAberto) "Total em aberto" else "Total fechado"

    val subtitulo = if (isAberto) {
        if (quantidade == 1) "1 cartão pendente" else "$quantidade cartões pendentes"
    } else {
        if (quantidade == 1) "1 cartão" else "$quantidade cartões"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = titulo,
                    color = textColorMain,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitulo,
                    color = textColorSub,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = total.formatarMoeda(visivel),
                color = textColorMain,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
private fun CardFaturaCompleta(
    fatura: FaturaCartao,
    expandida: Boolean,
    visivel: Boolean,
    onExpandir: () -> Unit,
    onPagar: () -> Unit,
    onVerFatura: () -> Unit
) {
    val cartao = fatura.cartao
    val limite = cartao.limiteCentavos
    val usado = fatura.totalCentavos.coerceAtMost(limite)
    val percentual = if (limite > 0L) {
        (usado.toFloat() / limite.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val disponivel = (limite - usado).coerceAtLeast(0L)

    // Aumenta a altura mínima do card quando a fatura NÃO está paga, para evitar compressão.
    val cardModifier = if (!fatura.paga) {
        Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEAEFF0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Alterado somente para a fatura fechada/paga.
        // A fatura aberta continua com 16.dp, como estava.
        val basePadding = if (fatura.paga) 10.dp else 16.dp

        Column {
            Column(
                modifier = Modifier.padding(basePadding)
            ) {
                if (fatura.paga) {
                    // --- BLOCO DA FATURA PAGA (SEM MUDANÇAS RELEVANTES) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        val context = LocalContext.current

                        val logoRes = remember(cartao.marcaChave) {
                            context.resources.getIdentifier(
                                cartao.marcaChave,
                                "drawable",
                                context.packageName
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF0F4EF)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoRes != 0) {
                                Icon(
                                    painter = painterResource(id = logoRes),
                                    contentDescription = cartao.nome,
                                    tint = if (ehPicPay(cartao)) {
                                        Color(0xFF04C563)
                                    } else {
                                        Color.Unspecified
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = cartao.nome.take(2).uppercase(),
                                    color = cartao.corHex.toColor(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cartao.nome,
                                    color = CorTexto,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = Color(0xFFBBF7D0)
                                    )
                                ) {
                                    Text(
                                        text = "Pago",
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 6.dp
                                        ),
                                        color = Color(0xFF16A34A),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Fatura cartão: ${
                                    fatura.dataVencimento.format(
                                        DateTimeFormatter.ofPattern(
                                            "MMMM yyyy",
                                            Locale("pt", "BR")
                                        )
                                    )
                                }",
                                color = CorTexto.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.labelSmall
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val vencimentoData = fatura.dataVencimento

                            Text(
                                text = "Venceu: ${
                                    vencimentoData.format(
                                        DateTimeFormatter.ofPattern(
                                            "dd 'de' MMMM",
                                            Locale("pt", "BR")
                                        )
                                    )
                                }",
                                color = CorTexto.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = fatura.totalCentavos.formatarMoeda(visivel),
                                color = CorTexto,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(end = 10.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            androidx.compose.material3.OutlinedButton(
                                onClick = onExpandir,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Color(0xFFE5E7EB)
                                ),
                                modifier = Modifier.defaultMinSize(minWidth = 100.dp)
                            ) {
                                Text(
                                    text = "Ver fatura",
                                    color = Color(0xFF1F2937),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                } else {
                    // --- BLOCO DA FATURA ABERTA (onde aplicamos a mudança solicitada) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        val context = LocalContext.current
                        val logoRes = remember(cartao.marcaChave) {
                            context.resources.getIdentifier(
                                cartao.marcaChave,
                                "drawable",
                                context.packageName
                            )
                        }

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
                                    contentDescription = cartao.nome,
                                    tint = if (ehPicPay(cartao)) {
                                        Color(0xFF04C563)
                                    } else {
                                        Color.Unspecified
                                    },
                                    modifier = Modifier.size(28.dp)
                                )
                            } else {
                                Text(
                                    text = cartao.nome.take(2).uppercase(),
                                    color = cartao.corHex.toColor(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cartao.nome,
                                    color = CorTexto,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )


                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = formatarVencimentoFatura(fatura),
                                        color = Color(0xFF9CA3AF),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(72.dp)
                                    .height(28.dp)
                                    .padding(top = 1.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Color(0xFFFDE68A)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Pendente",
                                        color = Color(0xFFD97706),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        softWrap = false,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = fatura.totalCentavos.formatarMoeda(visivel),
                                    color = CorTexto,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "de ${limite.formatarMoeda(visivel)}",
                                    color = Color(0xFF9CA3AF),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                        }
                        
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val percentualInt = (percentual * 100).toInt()

                        Text(
                            text = "$percentualInt% do limite utilizado",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.labelSmall
                        )

                        Text(
                            text = "${disponivel.formatarMoeda(true)} disponível",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    ) {
                        drawRect(color = Color(0xFFF3F4F6))

                        drawRect(
                            color = CorPrincipal,
                            size = androidx.compose.ui.geometry.Size(
                                width = size.width * percentual.coerceIn(0f, 1f),
                                height = size.height
                            )
                        )
                    }
                }
            }

            // DIVIDER (mantido)
            if (!fatura.paga) {
                HorizontalDivider(
                    color = Color(0xFFF3F4F6),
                    thickness = 1.dp
                )

                // NOVA ESTRUTURA: texto de dias ACIMA dos botões, e abaixo uma linha com os botões alinhados à direita
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = diasParaVencerTexto(fatura),
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = onVerFatura,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(
                                horizontal = 12.dp,
                                vertical = 8.dp
                            ),
                            border = BorderStroke(
                                1.dp,
                                Color(0xFFE5E7EB)
                            ),
                            modifier = Modifier
                                .defaultMinSize(minWidth = 110.dp)
                        ) {
                            Text(
                                text = "Ver fatura",
                                color = Color(0xFF1F2937),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        androidx.compose.material3.Button(
                            onClick = onPagar,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(
                                horizontal = 12.dp,
                                vertical = 8.dp
                            ),
                            colors = androidx.compose.material3.ButtonDefaults
                                .buttonColors(
                                    containerColor = CorPrincipal
                                ),
                            modifier = Modifier
                                .defaultMinSize(minWidth = 110.dp)
                                .heightIn(min = 40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Pagar fatura",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// helper para calcular texto dias para vencer (simples)
private fun diasParaVencerTexto(fatura: FaturaCartao): String {
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

@Composable
private fun ItemDespesaCartao(
    despesa: DespesaDetalhada,
    visivel: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = despesa.dataCompra.formatarDia(),
            color = CorTexto,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(52.dp)
        )

        Column(Modifier.weight(1f)) {
            Text(
                text = despesa.descricao,
                color = CorTexto,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = despesa.categoriaNome,
                color = CorTexto.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Text(
            text = despesa.valor.formatarMoeda(visivel),
            color = CorTexto,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CardDespesasFixas(
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

@Composable
private fun IconeCategoriaDoCardLocal(
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


private fun String.ehEmojiLocal(): Boolean {
    return this.any { caractere -> caractere.code > 255 }
}


@Composable
private fun TextoVazio(texto: String) {
    Text(
        text = texto,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        color = CorTexto.copy(alpha = 0.7f)
    )
}

@Composable
private fun DialogoPagamento(
    fatura: FaturaCartao,
    contas: List<ContaSaldo>,
    selecionada: ContaSaldo?,
    visivel: Boolean,
    processando: Boolean,
    onSelecionarConta: (ContaSaldo) -> Unit,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit
) {
    if (!visivel) return

    val context = LocalContext.current

    val vencimentoData = fatura.dataVencimento

    val formatterData = remember {
        DateTimeFormatter.ofPattern(
            "dd 'de' MMM",
            Locale("pt", "BR")
        )
    }

    Dialog(
        onDismissRequest = onCancelar,
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
                    .wrapContentHeight(),
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
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 10.dp,
                            bottom = 20.dp
                        )
                ) {

                    // Pequeno handle no topo do bottom sheet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(width = 38.dp, height = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE1E5E8))
                        )
                    }

// Título e botão fechar centralizados na mesma linha
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pagar fatura",
                            color = CorTexto,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = onCancelar,
                            modifier = Modifier.size(40.dp)
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

// Linha divisória abaixo do cabeçalho
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = Color(0xFFE9ECEF)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Pagar fatura",
                        color = CorTexto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Card de resumo da fatura:
                    // Ele termina aqui e NÃO deve envolver a lista de contas.
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF7F8FA)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val logoFaturaRes = remember(fatura.cartao.marcaChave) {
                                context.resources.getIdentifier(
                                    fatura.cartao.marcaChave,
                                    "drawable",
                                    context.packageName
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF0EEF8)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoFaturaRes != 0) {
                                    Icon(
                                        painter = painterResource(logoFaturaRes),
                                        contentDescription = fatura.cartao.nome,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(28.dp)
                                    )
                                } else {
                                    Text(
                                        text = fatura.cartao.nome
                                            .take(2)
                                            .uppercase(),
                                        color = fatura.cartao.corHex.toColor(),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = fatura.cartao.nome,
                                    color = CorTexto,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Fatura - vence ${
                                        vencimentoData.format(formatterData)
                                    }",
                                    color = CorTexto.copy(alpha = 0.55f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = fatura.totalCentavos.formatarMoeda(true),
                                color = CorTexto,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "DEBITAR DE",
                        color = CorTexto.copy(alpha = 0.58f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lista de contas fora do Card de resumo
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        contas.forEach { conta ->
                            val isSelected = conta.id == selecionada?.id

                            val nomeArquivo = remember(conta.instituicaoChave) {
                                when {
                                    conta.instituicaoChave.contains(
                                        "caixa",
                                        ignoreCase = true
                                    ) -> "cef"

                                    conta.instituicaoChave.equals(
                                        "cx",
                                        ignoreCase = true
                                    ) -> "cef"

                                    else -> conta.instituicaoChave
                                }
                            }

                            val logoContaRes = remember(nomeArquivo) {
                                context.resources.getIdentifier(
                                    nomeArquivo,
                                    "drawable",
                                    context.packageName
                                )
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!processando) {
                                            onSelecionarConta(conta)
                                        }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        Color(0xFFF5FAF7)
                                    } else {
                                        Color.White
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 1.dp else 1.dp,
                                    color = if (isSelected) {
                                        CorPrincipal
                                    } else {
                                        Color(0xFFE4E7EA)
                                    }
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
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF1F3F3)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (logoContaRes != 0) {
                                            Icon(
                                                painter = painterResource(logoContaRes),
                                                contentDescription = conta.nome,
                                                tint = Color.Unspecified,
                                                modifier = Modifier.size(25.dp)
                                            )
                                        } else {
                                            Text(
                                                text = conta.nome
                                                    .take(2)
                                                    .uppercase(),
                                                color = conta.corHex.toColor(),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = conta.nome,
                                            color = if (isSelected) {
                                                CorPrincipal
                                            } else {
                                                CorTexto
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(1.dp))

                                        Text(
                                            text = "${conta.saldoCentavos.formatarMoeda(true)} disponível",
                                            color = CorTexto.copy(alpha = 0.52f),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    // Radio igual ao visual da imagem 1:
                                    // círculo verde externo e ponto branco interno.
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (isSelected) {
                                                    Modifier.background(CorPrincipal)
                                                } else {
                                                    Modifier.border(
                                                        width = 1.5.dp,
                                                        color = Color(0xFFD5DBE0),
                                                        shape = CircleShape
                                                    )
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (selecionada != null) {
                        val novoSaldo =
                            selecionada.saldoCentavos - fatura.totalCentavos

                        Spacer(modifier = Modifier.height(18.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF3F7F5)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFFD2DDD6)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 15.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Saldo após pagamento",
                                    color = CorTexto.copy(alpha = 0.65f),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    text = novoSaldo.formatarMoeda(true),
                                    color = CorPrincipal,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onConfirmar,
                        enabled = selecionada != null && !processando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF225E43),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFB8CECA),
                            disabledContentColor = Color.White.copy(alpha = 0.85f)
                        )
                    ) {
                        Text(
                            text = if (processando) {
                                "Pagando..."
                            } else {
                                "Confirmar pagamento de ${
                                    fatura.totalCentavos.formatarMoeda(true)
                                }"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoVerFatura(
    fatura: FaturaCartao,
    despesasFixasDoMes: List<DespesaDetalhada>,
    visivel: Boolean,
    onFechar: () -> Unit
) {
    if (!visivel) return

    val despesas = remember(fatura.despesas) {
        fatura.despesas.sortedByDescending { it.dataCompra }
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
                    .wrapContentHeight(),
                shape = RoundedCornerShape(
                    topStart = 26.dp,
                    topEnd = 26.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Área superior com padding lateral:
                    // handle, título e botão de fechar.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 10.dp
                            )
                    ) {
                        // Handle superior do bottom sheet.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(
                                        width = 38.dp,
                                        height = 4.dp
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE1E5E8))
                            )
                        }

                        // Cabeçalho: título original + botão fechar.
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fatura — ${fatura.cartao.nome}",
                                color = CorTexto,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = onFechar,
                                modifier = Modifier.size(40.dp)
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
                    }

                    // Divider sem padding lateral, igual ao diálogo de pagamento.
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = Color(0xFFE9ECEF)
                    )

                    // Conteúdo recebe o padding lateral padrão do modal.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 20.dp,
                                bottom = 20.dp
                            )
                    ) {
                        // Área rolável de lançamentos.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(
                                    min = 80.dp,
                                    max = 420.dp
                                )
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(
                                    bottom = 8.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = despesas,
                                    key = { it.id }
                                ) { despesa ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = Color(0xFFE4E7EA)
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
                                            IconeCategoriaDoCardLocal(
                                                iconeChave = despesa
                                                    .categoriaIconeChave
                                                    ?: "",
                                                nomeCategoria = despesa.categoriaNome,
                                                corHex = despesa.categoriaCorHex
                                            )

                                            Spacer(
                                                modifier = Modifier.width(12.dp)
                                            )

                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = despesa.descricao,
                                                    color = CorTexto,
                                                    fontWeight = FontWeight.SemiBold,
                                                    style = MaterialTheme
                                                        .typography
                                                        .bodyMedium
                                                )

                                                Row(
                                                    verticalAlignment = Alignment
                                                        .CenterVertically
                                                ) {
                                                    Text(
                                                        text = despesa.categoriaNome,
                                                        color = CorTexto.copy(
                                                            alpha = 0.6f
                                                        ),
                                                        style = MaterialTheme
                                                            .typography
                                                            .bodySmall
                                                    )

                                                    // Mantém exatamente o texto original
                                                    // e só exibe quando houver cartão.
                                                    despesa.cartaoId?.let {
                                                        Spacer(
                                                            modifier = Modifier
                                                                .width(8.dp)
                                                        )

                                                        Text(
                                                            text = "• ${
                                                                fatura.cartao.nome
                                                            }",
                                                            color = CorTexto.copy(
                                                                alpha = 0.6f
                                                            ),
                                                            style = MaterialTheme
                                                                .typography
                                                                .bodySmall
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(
                                                modifier = Modifier.width(8.dp)
                                            )

                                            Text(
                                                text = despesa.valor
                                                    .formatarMoeda(visivel),
                                                color = CorTexto,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme
                                                    .typography
                                                    .bodyMedium,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                item(key = "espaco_resumo_fatura") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                item(key = "resumo_fatura") {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFF7F8FA)
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = Color(0xFFE4E7EA)
                                        ),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 0.dp
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment
                                                    .CenterVertically
                                            ) {
                                                Text(
                                                    text = "Total da fatura",
                                                    color = CorTexto.copy(
                                                        alpha = 0.8f
                                                    ),
                                                    style = MaterialTheme
                                                        .typography
                                                        .bodyMedium
                                                )

                                                Spacer(
                                                    modifier = Modifier.weight(1f)
                                                )

                                                Text(
                                                    text = fatura.totalCentavos
                                                        .formatarMoeda(visivel),
                                                    fontWeight = FontWeight.Bold,
                                                    color = CorTexto,
                                                    style = MaterialTheme
                                                        .typography
                                                        .bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onFechar,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF225E43),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Fechar",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Long.formatarMoeda(visivel: Boolean): String {
    if (!visivel) return "R$ •••••"

    return NumberFormat
        .getCurrencyInstance(Locale("pt", "BR"))
        .format(this / 100.0)
}

private fun Long.formatarDia(): String {
    return Instant
        .ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd/MM"))
}


private fun String.toColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (_: IllegalArgumentException) {
    Color(0xFF5F8D84) // fallback
}

private fun ehPicPay(
    cartao: com.example.controlegastos.domain.model.Cartao
): Boolean {
    return cartao.nome.contains(
        "PicPay",
        ignoreCase = true
    ) || cartao.marcaChave.contains(
        "picpay",
        ignoreCase = true
    )
}
private fun formatarVencimentoFatura(
    fatura: FaturaCartao
): String {
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

private fun YearMonth.formatarMes(): String {
    return format(
        DateTimeFormatter.ofPattern(
            "MMMM yyyy",
            Locale("pt", "BR")
        )
    ).replaceFirstChar {
        it.titlecase(Locale("pt", "BR"))
    }
}