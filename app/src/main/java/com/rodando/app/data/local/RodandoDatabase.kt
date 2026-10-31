package com.rodando.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Base de datos local de Rodando. Los enums (estado, tipo de vinilo) se guardan por nombre. */
@Database(
    entities = [ViajeEntity::class, PuntoGpsEntity::class, CampaniaEntity::class, ViniloEntity::class],
    version = 2,
    exportSchema = false
)
abstract class RodandoDatabase : RoomDatabase() {

    abstract fun viajeDao(): ViajeDao
    abstract fun campaniaDao(): CampaniaDao
    abstract fun viniloDao(): ViniloDao

    companion object {
        fun crear(context: Context): RodandoDatabase =
            Room.databaseBuilder(context, RodandoDatabase::class.java, "rodando.db")
                // Mientras se desarrolla, un cambio de versión borra la base y se vuelven a cargar
                // los datos de ejemplo. Antes de la entrega final se reemplaza por migraciones.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
