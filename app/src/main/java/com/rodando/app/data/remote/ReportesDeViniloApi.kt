package com.rodando.app.data.remote

import com.rodando.app.domain.repository.ReportesDeVinilo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportesDeViniloApi(
    private val api: RodandoApi,
    private val codificarFoto: (String?) -> String? = CodificadorDeFotos::enBase64,
    private val ahora: () -> Long = System::currentTimeMillis
) : ReportesDeVinilo {

    override suspend fun reportarDanio(id: String, rutaFoto: String?, momento: String, viajeId: String?) {
        // Achicar la foto lleva unos cientos de milisegundos: fuera del hilo principal.
        val foto = withContext(Dispatchers.IO) { codificarFoto(rutaFoto) }
        api.reportarVinilo(ReporteDto(id = id, momento = momento, viajeId = viajeId, fecha = ahora(), foto = foto))
    }
}
