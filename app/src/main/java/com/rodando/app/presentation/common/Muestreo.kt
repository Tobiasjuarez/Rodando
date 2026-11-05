package com.rodando.app.presentation.common

// Cuentas para mostrar las fotos del vinilo: Kotlin puro, para poder probarlas sin Android.

/** Ruta de una foto real; las de los viajes de ejemplo y las de la cámara simulada no tienen archivo. */
fun esFotoReal(ruta: String?): Boolean = !ruta.isNullOrBlank() && !ruta.startsWith("simulada://")

/**
 * Cuántas veces achicar la foto al leerla (potencia de 2) para que su lado más largo
 * no baje de [maximo] píxeles: así una foto de 12 MP no ocupa toda la memoria.
 */
fun muestreoPara(ancho: Int, alto: Int, maximo: Int): Int {
    var muestreo = 1
    while (maxOf(ancho, alto) / (muestreo * 2) >= maximo) muestreo *= 2
    return muestreo
}

/**
 * Grados que hay que girar la foto según la orientación EXIF que guarda la cámara
 * (6 = 90°, 3 = 180°, 8 = 270°; el resto, sin giro).
 */
fun gradosSegunExif(orientacion: Int): Float = when (orientacion) {
    6 -> 90f
    3 -> 180f
    8 -> 270f
    else -> 0f
}
