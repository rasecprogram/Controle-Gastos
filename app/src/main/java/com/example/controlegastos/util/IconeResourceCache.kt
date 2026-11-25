package com.example.controlegastos.util

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache global (processo) para resolução de recursos drawable dinâmicos
 * obtidos via [android.content.res.Resources.getIdentifier].
 *
 * `getIdentifier()` varre a tabela de recursos do APK, o que é uma operação
 * relativamente custosa (não é um lookup O(1)). Como essas chaves são usadas
 * repetidamente durante a composição de listas (ex.: ícones de categoria,
 * logos de cartão) e essas listas são recompostas com frequência (scroll,
 * troca de mês, troca de aba), cachear o resultado por chave elimina essa
 * sobrecarga de forma transparente, sem alterar nenhum comportamento visual
 * ou funcional do app.
 *
 * A chave combina o nome do recurso com o tipo de recurso (ex.: "drawable")
 * para evitar colisões entre tipos diferentes.
 */
object IconeResourceCache {

    private val cache = ConcurrentHashMap<String, Int>()

    /**
     * Retorna o resource id associado ao [nome] informado, resolvendo-o apenas
     * uma vez por processo. Retorna 0 caso o nome seja vazio/em branco ou o
     * recurso não exista (mesmo comportamento de [android.content.res.Resources.getIdentifier]).
     */
    fun resolverDrawable(context: Context, nome: String?): Int {
        val chave = nome?.trim().orEmpty()
        if (chave.isBlank()) return 0

        val chaveCache = "drawable:$chave"

        cache[chaveCache]?.let { return it }

        val resId = context.resources.getIdentifier(
            chave,
            "drawable",
            context.packageName
        )

        cache[chaveCache] = resId
        return resId
    }
}