package com.example.controlegastos.ui.edicao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlegastos.domain.model.Cartao
import com.example.controlegastos.domain.model.Categoria
import com.example.controlegastos.domain.model.ContaSaldo
import com.example.controlegastos.domain.model.TipoContaSaldo
import com.example.controlegastos.domain.repository.CartaoRepository
import com.example.controlegastos.domain.repository.CategoriaRepository
import com.example.controlegastos.domain.repository.ContaSaldoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EdicaoViewModel @Inject constructor(
    private val categoriaRepository: CategoriaRepository,
    private val cartaoRepository: CartaoRepository,
    private val contaSaldoRepository: ContaSaldoRepository
) : ViewModel() {

    private val formulario = MutableStateFlow(EdicaoUiState())

    val uiState: StateFlow<EdicaoUiState> = combine(
        formulario,
        categoriaRepository.observarTodas(),
        cartaoRepository.observarTodos(),
        contaSaldoRepository.observarTodas()
    ) { formularioAtual, categorias, cartoes, contas ->
        formularioAtual.copy(
            carregando = false,
            categorias = categorias,
            cartoes = cartoes,
            contas = contas
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EdicaoUiState()
    )

    fun selecionarCategoriaSugerida(categoria: CategoriaSugerida) {
        formulario.value = formulario.value.copy(
            novaCategoriaNome = categoria.nome,
            novoIconeCategoria = categoria.iconeChave,
            novaCategoriaCorHex = categoria.corHex,
            mensagem = null
        )
    }

    fun atualizarNomeCategoria(nome: String) {
        formulario.value = formulario.value.copy(
            novaCategoriaNome = nome,
            mensagem = null
        )
    }

    fun atualizarTetoCategoria(texto: String) {
        formulario.value = formulario.value.copy(
            novaCategoriaTetoTexto = texto.filter(Char::isDigit),
            mensagem = null
        )
    }

    fun atualizarIconeCategoria(iconeChave: String) {
        formulario.value = formulario.value.copy(
            novoIconeCategoria = iconeChave,
            mensagem = null
        )
    }

    fun salvarCategoria() {
        val estado = formulario.value
        val nome = estado.novaCategoriaNome.trim()

        if (nome.isBlank()) {
            formulario.value = estado.copy(
                mensagem = "Informe o nome da categoria."
            )
            return
        }

        if (estado.categorias.any { it.nome.equals(nome, ignoreCase = true) }) {
            formulario.value = estado.copy(
                mensagem = "Essa categoria já está cadastrada."
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                categoriaRepository.salvar(
                    Categoria(
                        id = 0,
                        nome = nome,
                        corHex = estado.novaCategoriaCorHex,
                        tetoMensal = estado.novaCategoriaTetoTexto
                            .takeIf { it.isNotBlank() }
                            ?.toLong(),
                        iconeChave = estado.novoIconeCategoria,
                        ativa = true
                    )
                )
            }.onSuccess {
                formulario.value = EdicaoUiState(
                    instituicaoSelecionada = estado.instituicaoSelecionada,
                    tipoContaSelecionado = estado.tipoContaSelecionado,
                    mensagem = "Categoria adicionada."
                )
            }.onFailure { erro ->
                formulario.value = estado.copy(
                    mensagem = erro.message
                        ?: "Não foi possível salvar a categoria."
                )
            }
        }
    }

    fun excluirContaSaldo(contaId: Int) {
        viewModelScope.launch {
            val excluiu = contaSaldoRepository.excluir(contaId)

            formulario.value = formulario.value.copy(
                mensagem = if (excluiu) {
                    "Saldo excluído com sucesso."
                } else {
                    "Não foi possível excluir o saldo."
                }
            )
        }
    }

    fun alterarAtivacaoCategoria(categoria: Categoria, ativa: Boolean) {
        viewModelScope.launch {
            runCatching {
                categoriaRepository.atualizarAtivacao(categoria.id, ativa)
            }.onFailure { erro ->
                formulario.value = formulario.value.copy(
                    mensagem = erro.message
                        ?: "Não foi possível atualizar a categoria."
                )
            }
        }
    }

    fun excluirCategoria(categoriaId: Int) {
        viewModelScope.launch {
            runCatching {
                categoriaRepository.excluir(categoriaId)
            }.onSuccess {
                formulario.value = formulario.value.copy(
                    mensagem = "Categoria removida."
                )
            }.onFailure { erro ->
                formulario.value = formulario.value.copy(
                    mensagem = erro.message
                        ?: "Não foi possível remover a categoria."
                )
            }
        }
    }

    fun alterarAtivacaoCartao(
        instituicao: InstituicaoPredefinida,
        ativo: Boolean
    ) {
        val cartaoExistente = uiState.value.cartoes.firstOrNull {
            it.marcaChave == instituicao.chave
        }

        viewModelScope.launch {
            runCatching {
                if (cartaoExistente == null) {
                    cartaoRepository.salvarOuAtualizarPorMarca(
                        Cartao(
                            nome = instituicao.nome,
                            marcaChave = instituicao.chave,
                            corHex = instituicao.cor.toHex(),
                            ativo = ativo,
                            diasAntesVencimento = instituicao.diasAntesVencimentoPadrao,
                            diaVencimento = instituicao.diaVencimentoPadrao,
                            limiteCentavos = 0L // Valor padrão caso adicione via ativação rápida
                        )
                    )
                } else {
                    cartaoRepository.atualizarAtivacao(
                        cartaoId = cartaoExistente.id,
                        ativo = ativo
                    )
                }
            }.onFailure { erro ->
                formulario.value = formulario.value.copy(
                    mensagem = erro.message
                        ?: "Não foi possível atualizar o cartão."
                )
            }
        }
    }

    fun adicionarCartao(
        instituicaoChave: String,
        nome: String,
        diasAntesVencimento: Int,
        diaVencimento: Int,
        limiteCentavos: Long
    ) {
        val instituicao = instituicoesPredefinidas.firstOrNull {
            it.chave == instituicaoChave
        }

        if (instituicao == null) {
            formulario.value = formulario.value.copy(
                mensagem = "Instituição do cartão não encontrada."
            )
            return
        }

        // Caso especial: Pix — não possui ciclo de vencimento nem limite
        val isPix = instituicao.chave.equals("pix", ignoreCase = true)
        if (!isPix) {
            if (diasAntesVencimento !in 1..31 || diaVencimento !in 1..31) {
                formulario.value = formulario.value.copy(
                    mensagem = "Informe antecedência e vencimento entre 1 e 31."
                )
                return
            }

            if (limiteCentavos < 0L) {
                formulario.value = formulario.value.copy(
                    mensagem = "Informe um limite válido."
                )
                return
            }
        }

        viewModelScope.launch {
            runCatching {
                cartaoRepository.salvarOuAtualizarPorMarca(
                    Cartao(
                        nome = nome,
                        marcaChave = instituicao.chave,
                        corHex = instituicao.cor.toHex(),
                        ativo = true,
                        diasAntesVencimento = if (isPix) 8 else diasAntesVencimento,
                        diaVencimento = if (isPix) 1 else diaVencimento,
                        limiteCentavos = if (isPix) 0L else limiteCentavos
                    )
                )
            }.onSuccess {
                formulario.value = formulario.value.copy(
                    mensagem = "Cartão adicionado."
                )
            }.onFailure { erro ->
                formulario.value = formulario.value.copy(
                    mensagem = erro.message
                        ?: "Não foi possível adicionar o cartão."
                )
            }
        }
    }

    fun atualizarAtivacaoCartaoPorId(cartaoId: Int, ativo: Boolean) {
        viewModelScope.launch {
            runCatching {
                cartaoRepository.atualizarAtivacao(
                    cartaoId = cartaoId,
                    ativo = ativo
                )
            }.onFailure { erro ->
                formulario.value = formulario.value.copy(
                    mensagem = erro.message
                        ?: "Não foi possível atualizar o cartão."
                )
            }
        }
    }

    fun excluirCartao(cartaoId: Int) {
        viewModelScope.launch {
            runCatching {
                cartaoRepository.excluir(cartaoId)
            }.onSuccess {
                formulario.value = formulario.value.copy(
                    mensagem = "Cartão removido."
                )
            }.onFailure { erro ->
                formulario.value = formulario.value.copy(
                    mensagem = erro.message
                        ?: "Não foi possível remover o cartão."
                )
            }
        }
    }

    fun editarConfiguracaoCartao(cartao: Cartao) {
        formulario.value = formulario.value.copy(
            cartaoEmEdicao = cartao,
            diasAntesVencimentoTexto = cartao.diasAntesVencimento.toString(),
            diaVencimentoTexto = cartao.diaVencimento.toString(),
            mensagem = null
        )
    }

    fun atualizarDiasAntesVencimento(texto: String) {
        val apenasNumeros = texto.filter { it.isDigit() }
        formulario.value = formulario.value.copy(
            diasAntesVencimentoTexto = apenasNumeros,
            mensagem = null
        )
    }

    fun atualizarDiasCartao(diasAntesVencimento: String, vencimento: String) {
        formulario.value = formulario.value.copy(
            diasAntesVencimentoTexto = diasAntesVencimento.filter(Char::isDigit),
            diaVencimentoTexto = vencimento.filter(Char::isDigit),
            mensagem = null
        )
    }

    fun salvarConfiguracaoCartao() {
        val estado = formulario.value
        val cartao = estado.cartaoEmEdicao ?: return

        // Se for Pix, não permitimos salvar configuração de ciclo/limite
        if (cartao.marcaChave.equals("pix", ignoreCase = true)) {
            formulario.value = estado.copy(
                cartaoEmEdicao = null,
                diasAntesVencimentoTexto = "",
                diaVencimentoTexto = "",
                mensagem = "Configuração do Pix não aplicável."
            )
            return
        }

        val diasAntesVencimento = estado.diasAntesVencimentoTexto
            .toIntOrNull()
            ?.coerceIn(1, 31)
            ?: 8

        val diaVencimento = estado.diaVencimentoTexto
            .toIntOrNull()
            ?.coerceIn(1, 31)
            ?: 5

        viewModelScope.launch {
            runCatching {
                cartaoRepository.atualizarConfiguracao(
                    cartaoId = cartao.id,
                    ativo = cartao.ativo,
                    diasAntesVencimento = diasAntesVencimento,
                    diaVencimento = diaVencimento
                )
            }.onSuccess {
                formulario.value = formulario.value.copy(
                    cartaoEmEdicao = null,
                    diasAntesVencimentoTexto = "",
                    diaVencimentoTexto = "",
                    mensagem = "Configuração do cartão salva."
                )
            }.onFailure { erro ->
                formulario.value = estado.copy(
                    mensagem = erro.message
                        ?: "Não foi possível salvar o cartão."
                )
            }
        }
    }

    fun selecionarInstituicao(instituicao: InstituicaoPredefinida) {
        formulario.value = formulario.value.copy(
            instituicaoSelecionada = instituicao,
            mensagem = null
        )
    }

    fun selecionarTipoConta(tipo: TipoContaSaldo) {
        formulario.value = formulario.value.copy(
            tipoContaSelecionado = tipo,
            mensagem = null
        )
    }

    fun atualizarSaldoInicial(texto: String) {
        formulario.value = formulario.value.copy(
            saldoInicialTexto = texto.filter(Char::isDigit),
            mensagem = null
        )
    }

    fun salvarContaSaldo() {
        val estado = formulario.value
        val saldo = estado.saldoInicialTexto.toLongOrNull()

        if (saldo == null || saldo < 0L) {
            formulario.value = estado.copy(
                mensagem = "Informe um saldo inicial válido."
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                val instituicao = estado.instituicaoSelecionada
                contaSaldoRepository.salvar(
                    ContaSaldo(
                        nome = when (estado.tipoContaSelecionado) {
                            TipoContaSaldo.CONTA -> instituicao.nome
                            TipoContaSaldo.CARTEIRA -> "Carteira"
                            TipoContaSaldo.SALDO_RESERVADO -> {
                                "${instituicao.nome}"
                            }
                        },
                        instituicaoChave = instituicao.chave,
                        tipo = estado.tipoContaSelecionado,
                        saldoCentavos = saldo,
                        corHex = instituicao.cor.toHex(),
                        ativo = true
                    )
                )
            }.onSuccess {
                formulario.value = EdicaoUiState(
                    instituicaoSelecionada = estado.instituicaoSelecionada,
                    tipoContaSelecionado = estado.tipoContaSelecionado,
                    mensagem = "Conta adicionada."
                )
            }.onFailure { erro ->
                formulario.value = estado.copy(
                    mensagem = erro.message
                        ?: "Não foi possível salvar a conta."
                )
            }
        }
    }

    fun alterarAtivacaoConta(conta: ContaSaldo, ativo: Boolean) {
        viewModelScope.launch {
            runCatching {
                contaSaldoRepository.atualizarAtivacao(conta.id, ativo)
            }.onFailure { erro ->
                formulario.value = formulario.value.copy(
                    mensagem = erro.message
                        ?: "Não foi possível atualizar a conta."
                )
            }
        }
    }

    fun consumirMensagem() {
        formulario.value = formulario.value.copy(mensagem = null)
    }
}

private fun androidx.compose.ui.graphics.Color.toHex(): String {
    return "#%02X%02X%02X".format(
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}