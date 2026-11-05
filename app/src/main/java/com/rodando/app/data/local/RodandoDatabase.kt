package com.rodando.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local de Rodando. Los enums (estados, tipo de vinilo) se guardan por nombre.
 *
 * Room exporta el esquema de cada versión a `app/schemas/` (se sube a git): así se ve qué cambió
 * entre versiones y se pueden probar las migraciones.
 */
@Database(
    entities = [ViajeEntity::class, PuntoGpsEntity::class, CampaniaEntity::class, ViniloEntity::class],
    version = 5,
    exportSchema = true
)
abstract class RodandoDatabase : RoomDatabase() {

    abstract fun viajeDao(): ViajeDao
    abstract fun campaniaDao(): CampaniaDao
    abstract fun viniloDao(): ViniloDao

    companion object {
        fun crear(context: Context): RodandoDatabase =
            Room.databaseBuilder(context, RodandoDatabase::class.java, "rodando.db")
                .addMigrations(*Migraciones.TODAS)
                // Las versiones 1 a 4 fueron de desarrollo, antes de que la app se usara: si alguien
                // tiene una, se borra y se vuelve a bajar todo. Desde la 5, un cambio de versión sin
                // migración hace fallar la app al abrir, a propósito: los viajes sin enviar no se pierden.
                .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1, 2, 3, 4)
                .build()
    }
}
