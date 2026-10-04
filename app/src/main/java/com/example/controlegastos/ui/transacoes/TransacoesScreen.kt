@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controlegastos.ui.transacoes

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.controlegastos.domain.model.ContaSaldo
import com.example.controlegastos.domain.model.FaturaCartao
import com.example.controlegastos.domain.model.TipoContaSaldo
import com.example.controlegastos.ui.components.BarraNavegacaoInferior
import kotlinx.coroutines.launch
import java.time.YearMonth

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
    var mostrarTransferencias by remember {
        mutableStateOf(false)
    }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TituloSecao(
                            texto = "Contas"
                        )

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Transferências",
                            color = Color(0xFF0F5A4A),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    mostrarTransferencias = true
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
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

    if (mostrarTransferencias) {
        DialogTransferenciaSaldo(
            contas = uiState.contas,
            onDismiss = {
                mostrarTransferencias = false
            },
            onConfirmarTransferencia = { origemId, destinoId, valorCentavos ->
                viewModel.transferirSaldo(
                    contaOrigemId = origemId,
                    contaDestinoId = destinoId,
                    valorCentavos = valorCentavos
                )

                mostrarTransferencias = false
            }
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
            Surface(
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
            Surface(
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