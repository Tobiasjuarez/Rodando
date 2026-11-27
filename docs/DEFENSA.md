# Guía para la demo y la defensa

Cómo mostrar Rodando en unos 10 minutos y qué contestar si preguntan. Cada integrante tiene que poder explicar cualquier parte, no solo la suya: por eso cada respuesta dice en qué archivo mirar.

## Antes de empezar (15 minutos antes)

1. `git pull` en `main` y *Run* una vez para que el emulador tenga la última versión.
2. En `api-mock/`, borrar `datos.json` y la carpeta `fotos/` para arrancar limpio, y levantar `node server.js`.
3. Abrir <http://localhost:3000> en el navegador de la computadora (página del equipo).
4. En el emulador: *Settings → Apps → Rodando → Storage → Clear storage*, abrir la app, iniciar sesión con `lucia@rodando.com.ar` / `rodando123` y aceptar el permiso de red local. Hacer un viaje corto de prueba para aceptar ubicación, notificaciones y cámara antes de la demo.
5. Dejar cargado en *Extended controls → Location* el recorrido corto `docs/emulador/ruta-corta-ramos-mejia-liniers.gpx` y la velocidad en 4x.
6. Tener a mano `./gradlew test` ya corrido, con el resultado en la terminal.

## Guion de la demo

Cada paso sigue el formato **Problema → Escenario → Acción → Respuesta → Valor**.

### 1. El problema (30 s)

- **Problema:** un conductor del AMBA hace los mismos recorridos todos los días y el auto no le deja nada. Un anunciante quiere visibilidad en la calle, pero no sabe si los autos circulan de verdad.
- **Valor:** Rodando le paga al conductor por llevar un vinilo y le da al anunciante viajes con foto y GPS.

### 2. Inicio y ganancias (1 min)

- **Escenario:** Lucía abre la app a la mañana.
- **Acción:** mostrar Inicio (saludo, campaña, vinilo, última sincronización) y Ganancias.
- **Respuesta:** ganancia confirmada (viajes aprobados) y estimada (más los que esperan aprobación); barras de los 12 días y del tope de 1.500 km.
- **Valor:** sabe cuánto lleva y qué le falta para cobrar el fijo entero, sin dar vueltas de más (no hay mínimo de km).

### 3. Iniciar un viaje con la foto del vinilo (2 min)

- **Escenario:** Lucía sale a trabajar.
- **Acción:** *Iniciar viaje* → mantener apretado *Sacar foto* (modo prueba con el QR) → "Vinilo verificado" → *Usar foto e iniciar*. Darle *Play* a la ruta GPX.
- **Respuesta:** ML Kit lee el QR en el teléfono, sin conexión. El mapa sigue al auto y la distancia sube. Mostrar la notificación fija: el GPS sigue con la pantalla bloqueada.
- **Valor:** el viaje queda respaldado aunque no haya señal.

### 4. Tres intentos fallidos y vinilo dañado (1 min)

- **Acción:** en otro viaje (o al finalizar), sacar 3 fotos a una pared sin QR.
- **Respuesta:** después del tercer intento se ofrece seguir "En revisión" o *Reportar vinilo dañado*. Mostrar el reporte en la página del equipo.
- **Valor:** un vinilo roto no deja al conductor sin cobrar ni al equipo sin enterarse.

### 5. Sin conexión (2 min)

- **Acción:** cortar la API (Ctrl + C) y finalizar el viaje con la foto final.
- **Respuesta:** aviso "Sin conexión" arriba; el viaje se guarda y en Historial queda "Sin enviar", con "Esperando conexión". Levantar la API: el viaje se envía solo.
- **Valor:** Offline First. Room es la fuente de verdad y WorkManager envía cuando puede, aunque la app esté cerrada.

### 6. Envíos parciales y aprobación (2 min)

- **Acción:** en la página del equipo, *Falla uno de cada dos*. Con dos o tres viajes pendientes, tocar *Actualizar* en Historial.
- **Respuesta:** llegan algunos, el resto queda "Reintentando" y se envía después, sin duplicarse. Aprobar un viaje en la página y *Actualizar*: pasa a "Aprobado" y sube lo confirmado en Ganancias.
- **Valor:** enviado no es aprobado. El conductor ve qué se cobra y qué falta revisar.

### 7. Calidad (1 min)

- Mostrar `./gradlew test` en verde (más de 100 tests).
- Opcional: TalkBack anunciando los títulos, o la letra al máximo sin cortes.

## Si algo falla en la demo

| Falla | Plan B |
| --- | --- |
| La app no conecta | Revisar que `node server.js` siga corriendo y abrir `http://10.0.2.2:3000` en el Chrome del emulador |
| El emulador no reproduce la ruta | Mostrar un viaje de ejemplo en Historial y Detalle, y explicar el GPS con `RegistradorDeViaje` |
| La cámara no encuentra el QR | Modo prueba: mantener apretado *Sacar foto* |
| El emulador se cuelga | *Cold Boot Now* desde Device Manager. Mientras, mostrar el código y los tests |

## Preguntas probables

### Arquitectura

**¿Qué arquitectura usaron y por qué?** MVVM con capas de Clean Architecture: pantalla Compose → ViewModel → caso de uso → repositorio → Room / API / sensores. El dominio (`domain/`) es Kotlin puro, sin Android, por eso se testea sin emulador. *Mirar:* `AppContainer.kt`, `domain/usecase/`.

**¿Cómo inyectan dependencias?** A mano, en `AppContainer`: crea una sola vez la base, la API y los repositorios, y los ViewModels los reciben por constructor con una `fabrica`. Para el tamaño de la app no hacía falta Hilt, y se ve claro qué depende de qué. *Mirar:* `AppContainer.kt`, `FotoViniloViewModel.fabrica`.

**¿Cómo se comunica el ViewModel con la pantalla?** Expone un `StateFlow<UiState<T>>` y la pantalla lo junta con `collectAsStateWithLifecycle()`, que deja de escuchar cuando la app pasa a segundo plano. `UiState` tiene Cargando, Contenido, Vacío, Error y Offline. Los eventos van al revés, como funciones del ViewModel. *Mirar:* `presentation/common/UiState.kt`.

**¿Qué pasa si se rota la pantalla o Android mata la app en pleno viaje?** La app está bloqueada en vertical, pero igual el estado de la pantalla vive en el ViewModel. Lo importante (el viaje, los puntos) está en Room desde el primer momento: un viaje sin `fin` es el viaje en curso, y al volver a abrir Inicio ofrece seguirlo. *Mirar:* `ViajeEntity.kt`, `InicioViewModel.kt`.

### Datos y sincronización

**¿Qué es Offline First acá?** Las pantallas leen solo de Room y la red sincroniza por detrás. Sin conexión se puede hacer todo menos el login. *Mirar:* `ViajeRepositoryImpl.kt`.

**¿Por qué WorkManager y no una corrutina?** Porque el envío tiene que sobrevivir a que se cierre la app o se reinicie el teléfono, esperar conexión (`NetworkType.CONNECTED`) y reintentar con espera creciente. Una corrutina muere con el proceso. *Mirar:* `ProgramadorWorkManager.kt`, `EnvioDeViajesWorker.kt`.

**¿Cómo evitan viajes duplicados?** El id del viaje se genera en el teléfono (UUID). Si se pierde la respuesta y la app reintenta, el servidor ve el mismo id y devuelve el mismo `remoteId`. *Mirar:* `api-mock/server.js` (`recibirViaje`), `SincronizacionTest.kt`.

**¿Qué pasa si de 3 viajes fallan 2?** `sincronizarPendientes` sigue con el siguiente aunque uno falle, marca como enviados los que llegaron y el Worker devuelve `retry()` para los demás. *Mirar:* `ViajeRepositoryImpl.sincronizarPendientes`.

**¿Enviado es lo mismo que aprobado?** No. Cada viaje tiene dos estados: envío (técnico) y verificación (negocio). Fue un cambio por la devolución. *Mirar:* `domain/model/Viaje.kt`.

**¿Qué pasa si cambian la base de datos?** Desde la versión 5, el esquema se exporta a `app/schemas/` y cada cambio necesita una migración en `Migraciones.kt`, así no se borran viajes sin enviar.

### Sensores

**¿Cómo registran el GPS con la pantalla apagada?** Con un servicio en primer plano de tipo `location` y una notificación fija. Android corta la ubicación de las apps en segundo plano; el servicio lo evita. *Mirar:* `ServicioViaje.kt`, `RegistradorDeViaje.kt`.

**¿Cómo limpian el recorrido?** Se descartan posiciones con más de 50 m de error, las anteriores al inicio del viaje y los saltos de más de 200 km/h. La distancia se calcula sobre lo que queda. *Mirar:* `LimpiarRecorridoUseCase.kt`, `CalcularDistanciaUseCase.kt`.

**¿Cómo verifican el vinilo?** ML Kit busca en la foto el QR del vinilo y lo compara con el código esperado, en el teléfono y sin conexión. 3 intentos; después, en revisión o reporte de vinilo dañado. *Mirar:* `VerificadorMlKit.kt`, `VerificarFotoUseCase.kt`.

**¿Eso prueba que el auto circuló con el vinilo?** No del todo, y lo decimos: prueba que el vinilo estaba en la foto al principio y al final. Por eso el equipo aprueba cada viaje mirando las fotos, y Ganancias separa lo confirmado de lo estimado.

**¿Qué permisos piden y cuándo?** Red local (Android 17) al tocar Ingresar, ubicación y notificaciones al iniciar el primer viaje, y cámara al abrir la foto. Se piden en el momento en que hacen falta, no al abrir la app. *Mirar:* `data/sensors/Permisos.kt`, `AndroidManifest.xml`.

### Negocio

**¿Cómo se calcula la ganancia?** Fijo mensual × días con viajes / 12 (entero desde 12 días) + km pagados × tarifa de cada viaje, con tope de 1.500 km por mes en orden cronológico. *Mirar:* `CalcularGananciaUseCase.kt`, `CalcularGananciaUseCaseTest.kt`.

**¿Por qué cada viaje guarda su tarifa?** Si la empresa cambia la tarifa a mitad de mes, lo ya recorrido se paga como se acordó.

**¿Por qué no hay mínimo de km?** Un mínimo incentiva dar vueltas solo para llegar. Premiamos la constancia (días con viajes).

### Calidad y seguridad

**¿Qué testearon?** Casos de uso (ganancia, distancia, limpieza, fotos), repositorio y sincronización (envíos parciales, duplicados), ViewModels con `kotlinx-coroutines-test` y la lógica de las pantallas. Los repositorios se reemplazan por falsos en memoria. *Mirar:* `app/src/test/`, `fakes/Falsos.kt`.

**¿Qué hicieron de accesibilidad?** Títulos como encabezados (`Modifier.encabezado()`), avisos con `liveRegion` para que TalkBack los lea solos, botones grandes que crecen con la letra y descripciones en los íconos. Probado con TalkBack y la letra al máximo.

**¿Es segura la conexión?** La API simulada usa http, así que `network_security_config.xml` permite http solo hacia `10.0.2.2` y `localhost`; el resto exige https. El token va en el header `Authorization`, no se anota en Logcat y no entra en el backup. Si el servidor lo rechaza (401), la app vuelve al login.

## Qué explica cada uno

Todos tienen que poder contestar todo, pero cada uno lleva la parte que más conoce:

| Integrante | Parte | Archivos |
| --- | --- | --- |
| Tobías Juárez | Arquitectura, Room, sincronización, API | `AppContainer.kt`, `data/local/`, `data/sync/`, `ViajeRepositoryImpl.kt`, `api-mock/` |
| Matías Fernández | Pantallas, navegación, estados de UI, accesibilidad | `presentation/`, `RodandoApp.kt`, `UiState.kt`, `theme/` |
| Lucas Casas | GPS, cámara, ML Kit, tests | `data/sensors/`, `LimpiarRecorridoUseCase.kt`, `VerificarFotoUseCase.kt`, `app/src/test/` |

Ejercicio útil: que cada uno abra un archivo de otro y lo explique en voz alta en 2 minutos.

## Uso de IA

Usamos Claude (Anthropic) como asistente para escribir código y documentos. Cada PR lo declara en "Uso de IA". El equipo revisó, probó en el emulador y corrió los tests antes de cada merge.

Si piden ejemplos, contarlos así: **problema → qué le pedimos → qué explicó → qué decidimos → cómo lo verificamos**.

| Problema | Qué pasó | Cómo lo verificamos |
| --- | --- | --- |
| La app no llegaba a la API, pero Chrome del emulador sí | La explicación fue que Android 17 pide el permiso `ACCESS_LOCAL_NETWORK` para conectarse a la red local; sin él la conexión se corta sin error claro | Logcat con `SocketTimeoutException`; después de pedir el permiso, el login funcionó |
| El mapa con estilo CARTO no cargaba | Esos mapas piden clave de API; volvimos a OpenStreetMap (MAPNIK), que no la necesita | El mapa se ve en el emulador |
| La verificación de "versión de desarrollo" daba mal | `flags and FLAG_DEBUGGABLE != 0` se evaluaba en el orden equivocado; se corrigió con paréntesis | El modo prueba aparece solo en debug |
| Reintentos para siempre con un token viejo | Se agregó la vuelta al login cuando el servidor responde 401 | Reiniciar la API con datos nuevos: la app vuelve al login con el aviso |

Qué no delegamos: el modelo de negocio, los cambios por la devolución, las decisiones de alcance y las pruebas en el emulador.
