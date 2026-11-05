package com.rodando.app.data.local

import androidx.room.migration.Migration

/**
 * Migraciones de la base local, desde la versión 5 (la primera que conserva los datos).
 *
 * Cada cambio en una entidad sube `version` en [RodandoDatabase] y suma acá una migración que
 * lleva los datos de la versión anterior a la nueva sin borrarlos. Ejemplo, si se agregara
 * una nota a los viajes en la versión 6:
 *
 * ```
 * val DE_5_A_6 = object : Migration(5, 6) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE viajes ADD COLUMN nota TEXT")
 *     }
 * }
 * val TODAS = arrayOf(DE_5_A_6)
 * ```
 *
 * El esquema de cada versión queda en `app/schemas/` para comparar qué cambió.
 */
object Migraciones {
    val TODAS: Array<Migration> = emptyArray()
}
