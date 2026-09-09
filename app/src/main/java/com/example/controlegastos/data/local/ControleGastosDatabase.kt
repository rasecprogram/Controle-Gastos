package com.example.controlegastos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.controlegastos.data.local.converter.DatabaseConverters
import com.example.controlegastos.data.local.dao.CartaoDao
import com.example.controlegastos.data.local.dao.CategoriaDao
import com.example.controlegastos.data.local.dao.ContaSaldoDao
import com.example.controlegastos.data.local.dao.DespesaDao
import com.example.controlegastos.data.local.dao.GrupoParcelamentoDao
import com.example.controlegastos.data.local.entity.CartaoEntity
import com.example.controlegastos.data.local.entity.CategoriaEntity
import com.example.controlegastos.data.local.entity.ContaSaldoEntity
import com.example.controlegastos.data.local.entity.DespesaEntity
import com.example.controlegastos.data.local.entity.GrupoParcelamentoEntity

@Database(
    entities = [
        CategoriaEntity::class,
        GrupoParcelamentoEntity::class,
        DespesaEntity::class,
        CartaoEntity::class,
        ContaSaldoEntity::class
    ],
    version = 7, // 1. ALTERADO DE 5 PARA 6
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class ControleGastosDatabase : RoomDatabase() {

    abstract fun categoriaDao(): CategoriaDao
    abstract fun grupoParcelamentoDao(): GrupoParcelamentoDao
    abstract fun despesaDao(): DespesaDao
    abstract fun cartaoDao(): CartaoDao
    abstract fun contaSaldoDao(): ContaSaldoDao

    companion object {
        const val DATABASE_NAME = "controle_gastos.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            // ... seu código original da migration 1_2 (mantido igualzinho)
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tb_categorias ADD COLUMN icone_chave TEXT NOT NULL DEFAULT 'outros'")
                database.execSQL("ALTER TABLE tb_categorias ADD COLUMN ativa INTEGER NOT NULL DEFAULT 1")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_cartoes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nome TEXT NOT NULL,
                        marca_chave TEXT NOT NULL,
                        cor_hex TEXT NOT NULL,
                        ativo INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tb_cartoes_marca_chave ON tb_cartoes (marca_chave)")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_contas_saldo (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nome TEXT NOT NULL,
                        instituicao_chave TEXT NOT NULL,
                        tipo TEXT NOT NULL,
                        saldo_centavos INTEGER NOT NULL,
                        cor_hex TEXT NOT NULL,
                        ativo INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            // ... seu código original da migration 2_3
            override fun migrate(database: SupportSQLiteDatabase) {
                // ... mantido igualzinho
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            // ... seu código original da migration 3_4
            override fun migrate(database: SupportSQLiteDatabase) {
                // ... mantido igualzinho
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1) Cria nova tabela com o esquema esperado pelo Room (sem DEFAULT clauses)
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS tb_despesas_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                valor INTEGER NOT NULL,
                descricao TEXT NOT NULL,
                data_compra INTEGER NOT NULL,
                data_vencimento INTEGER NOT NULL,
                data_pagamento INTEGER,
                status_pago INTEGER NOT NULL,
                categoria_id INTEGER NOT NULL,
                grupo_parcelamento_id INTEGER,
                cartao_id INTEGER,
                conta_saldo_id INTEGER,
                tipo_lancamento TEXT NOT NULL,
                origem_pagamento TEXT,
                FOREIGN KEY(categoria_id) REFERENCES tb_categorias(id) ON DELETE RESTRICT ON UPDATE NO ACTION,
                FOREIGN KEY(grupo_parcelamento_id) REFERENCES tb_grupo_parcelamento(id) ON DELETE CASCADE ON UPDATE NO ACTION,
                FOREIGN KEY(cartao_id) REFERENCES tb_cartoes(id) ON DELETE SET NULL ON UPDATE NO ACTION,
                FOREIGN KEY(conta_saldo_id) REFERENCES tb_contas_saldo(id) ON DELETE SET NULL ON UPDATE NO ACTION
            )
            """.trimIndent()
                )

                // 2) Copia os dados existentes para a nova tabela.
                // Para as colunas novas, definimos valores neutros (NULL para conta_saldo_id e origem_pagamento; 'UNICA' para tipo_lancamento)
                database.execSQL(
                    """
            INSERT INTO tb_despesas_new (
                id, valor, descricao, data_compra, data_vencimento, data_pagamento,
                status_pago, categoria_id, grupo_parcelamento_id, cartao_id,
                conta_saldo_id, tipo_lancamento, origem_pagamento
            )
            SELECT
                id, valor, descricao, data_compra, data_vencimento, data_pagamento,
                status_pago, categoria_id, grupo_parcelamento_id, cartao_id,
                NULL as conta_saldo_id, 'UNICA' as tipo_lancamento, NULL as origem_pagamento
            FROM tb_despesas
            """.trimIndent()
                )

                // 3) Remove tabela antiga e renomeia a nova para o nome original
                database.execSQL("DROP TABLE IF EXISTS tb_despesas")
                database.execSQL("ALTER TABLE tb_despesas_new RENAME TO tb_despesas")

                // 4) Recria índices esperados
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tb_despesas_categoria_id ON tb_despesas(categoria_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tb_despesas_grupo_parcelamento_id ON tb_despesas(grupo_parcelamento_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tb_despesas_data_vencimento ON tb_despesas(data_vencimento)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tb_despesas_data_compra ON tb_despesas(data_compra)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tb_despesas_categoria_id_data_compra ON tb_despesas(categoria_id, data_compra)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tb_despesas_cartao_id ON tb_despesas(cartao_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tb_despesas_conta_saldo_id ON tb_despesas(conta_saldo_id)")
            }
        }

        // 2. NOVA MIGRATION ADICIONADA AQUI (5 PARA 6)
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE tb_cartoes ADD COLUMN limite_centavos INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            ALTER TABLE tb_cartoes
            ADD COLUMN dias_antes_vencimento INTEGER NOT NULL DEFAULT 8
            """.trimIndent()
                )
            }
        }
    }
}