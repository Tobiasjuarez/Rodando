package com.rodando.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Base de datos local de Rodando. Los enums (estado, tipo de vinilo) se guardan por nombre. */
@Database(
    entities = [ViajeEntity::class, PuntoGpsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RodandoDatabase : RoomDatabase() {

    abstract fun viajeDao(): ViajeDao

    companion object {
        fun crear(context: Context): RodandoDatabase =
            Room.databaseBuilder(context, RodandoDatabase::class.java, "rodando.db").build()
    }
}
