package com.rodando.app.presentation.login

import com.rodando.app.domain.usecase.IniciarSesionUseCase
import com.rodando.app.fakes.CampaniasFalsas
import com.rodando.app.fakes.ReglaDeHiloPrincipal
import com.rodando.app.fakes.SesionesFalsas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule val hiloPrincipal = ReglaDeHiloPrincipal()

    private val sesiones = SesionesFalsas()
    private val vm = LoginViewModel(IniciarSesionUseCase(sesiones, CampaniasFalsas()))

    private fun completar(email: String, password: String) {
        vm.cambiarEmail(email)
        vm.cambiarPassword(password)
    }

    @Test
    fun con_los_datos_correctos_guarda_la_sesion() {
        completar("lucia@rodando.com.ar", "rodando123")

        vm.ingresar()

        assertEquals("Lucía", sesiones.sesion.value?.nombre)
        assertNull(vm.estado.value.error)
        assertFalse(vm.estado.value.ingresando)
    }

    @Test
    fun con_la_contrasenia_incorrecta_muestra_el_error() {
        completar("lucia@rodando.com.ar", "otra")

        vm.ingresar()

        assertEquals("Email o contraseña incorrectos.", vm.estado.value.error)
        assertNull(sesiones.sesion.value)
    }

    @Test
    fun al_escribir_de_nuevo_se_borra_el_error() {
        completar("", "")
        vm.ingresar()
        assertEquals("Completá el email y la contraseña.", vm.estado.value.error)

        vm.cambiarEmail("lucia@rodando.com.ar")

        assertNull(vm.estado.value.error)
    }

    @Test
    fun sin_permiso_de_red_local_explica_como_activarlo() {
        vm.sinPermisoDeRedLocal()

        assertEquals(true, vm.estado.value.error?.contains("red local"))
    }
}
