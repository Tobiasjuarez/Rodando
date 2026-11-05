# Plan de desarrollo de Rodando

Este documento cuenta cómo se construyó la app, etapa por etapa, qué decisiones tomamos y qué falta. Sirve para que cualquiera del equipo, al bajar el repo, sepa dónde estamos y cómo probar todo. El README explica cómo correr la app; acá está el recorrido completo.

**Equipo:** Tobías Juárez, Matías Fernández y Lucas Casas.

## Estado

| Etapa | Qué se hizo | PR | Estado |
| --- | --- | --- | --- |
| 0 | Preentrega: requisitos, diseño en Figma, arquitectura, logo, alcance | #1 a #4 | Hecha |
| 1 | Proyecto base, tema y navegación inferior | #5 | Hecha |
| 2 | Room y RF03: Historial y Detalle del viaje | #6 | Hecha |
| 3 | RF04: Ganancias e Inicio, con barras de objetivos | #8 | Hecha |
| 4a | RF01/RF02: iniciar y finalizar viaje (foto y ubicación simuladas) | #9 | Hecha |
| 4b | GPS real en servicio en primer plano, mapa y limpieza del recorrido | #10 | Hecha |
| — | Cambios por la devolución de la preentrega | #11 | Hecha |
| 4c | Foto real con CameraX | #12 | Hecha |
| 4d | Verificación del vinilo con ML Kit (QR, 3 intentos, en revisión) | #13 | Hecha |
| 5 | API simulada, login y envío de viajes con WorkManager | #14 | Hecha |
| 6 | Calidad: estados sin conexión, accesibilidad, tests, migraciones | — | En curso |

## Forma de trabajo

- Una rama por etapa (`feature/...`), commits chicos con prefijo (`feat:`, `fix:`, `test:`, `docs:`, `chore:`).
- Cada rama termina en un pull request con el formato de `.github/pull_request_template.md`: qué cambia, requisito, cómo se probó y uso de IA.
- Antes de mergear: compila, corre en el emulador y pasa `./gradlew test`.
- Usamos Claude (Anthropic) como asistente para escribir código y documentos; cada PR lo declara y el equipo lo revisa y prueba.

## Arquitectura en una línea

`Pantalla (Compose) → ViewModel (StateFlow de UiState) → caso de uso → repositorio → Room / API / sensores`. Room es la fuente de verdad: las pantallas leen solo de Room y la red sincroniza por detrás (Offline First). Inyección de dependencias manual en `AppContainer`.

## Qué se hizo en cada etapa

### Etapa 1 · Base y navegación (#5)

- Proyecto con Kotlin, Jetpack Compose y Material 3; paleta del diseño en claro y oscuro.
- Barra inferior con Inicio, Historial y Ganancias.
- Ajustes de Gradle para que KSP (Room) funcione con AGP 9: `android.builtInKotlin=false` y `android.newDsl=false` en `gradle.properties`. Las advertencias de "deprecated" que aparecen al compilar son esperables.

### Etapa 2 · Historial y Detalle, RF03 (#6)

- Room con viajes y puntos GPS; viajes de ejemplo para probar sin GPS ni cámara.
- Historial agrupado por día y Detalle con el dibujo del recorrido.

### Etapa 3 · Ganancias e Inicio, RF04 (#8)

- Campaña y vinilo en Room. Cálculo de la ganancia del mes con barras de avance hacia el fijo entero.

### Etapa 4a · Iniciar y finalizar un viaje (#9)

- Flujo completo: Inicio → foto inicial → viaje en curso (cronómetro, distancia, recorrido) → foto final → viaje guardado.
- Casos de uso `IniciarViajeUseCase` y `FinalizarViajeUseCase`.

### Etapa 4b · GPS real y mapa (#10)

- Fused Location dentro de un servicio en primer plano con notificación: el recorrido sigue con la pantalla apagada.
- Se piden ubicación y notificaciones; si la ubicación del teléfono está apagada, se ofrece activarla.
- Limpieza del recorrido: se descartan posiciones con más de 50 m de error, la última ubicación conocida si es de antes del viaje y los saltos de más de 200 km/h.
- Mapa de OpenStreetMap (osmdroid, sin clave de API) que sigue la posición.
- Recorridos GPX en `docs/emulador` para mover la ubicación del emulador.

### Cambios por la devolución de la preentrega (#11)

El profesor observó que reconocer el código no prueba que el auto circuló con el vinilo, que había que separar "sincronizado" de "aprobado", conservar la tarifa de cada viaje, revisar el mínimo de km y probar pérdida de GPS, interrupciones y envíos parciales. Cambiamos:

- **Dos estados por viaje:** envío (sin enviar / enviado) y verificación (por aprobar, en revisión, aprobado, rechazado, no computable).
- **Ganancia confirmada y estimada:** lo confirmado son solo los viajes aprobados.
- **Tarifa por viaje:** cada viaje guarda la tarifa vigente al iniciarse.
- **Sin mínimo de km:** el fijo entero se cobra con 12 días con viajes; así no conviene dar vueltas para llegar a un mínimo. Se mantiene el tope de 1.500 km pagos por mes.
- Tests de pérdida de GPS, interrupciones y viajes sin posiciones.
- Respuesta escrita a la devolución con los límites de la verificación, el plan de pruebas, las cuentas del modelo de negocio y una guía de entrevistas para anunciantes (documento aparte del equipo).

### Etapa 4c · Cámara real (#12)

- CameraX con recuadro de encuadre; la foto se revisa antes de usarla. Fotos guardadas en la carpeta privada de la app y mostradas en el Detalle.

### Etapa 4d · Verificación con ML Kit (#13)

- ML Kit lee el código QR del vinilo en la foto, en el teléfono y sin conexión. Si no lo encuentra se repite (hasta 3 intentos); después se puede seguir y el viaje queda en revisión.
- QR de ejemplo en `docs/emulador/qr-vinilo-demo.png` (código `RODANDO-VINILO-0001`).

### Etapa 5 · API simulada, login y envío (#14)

- `api-mock/server.js`: servidor Node sin dependencias con login, campaña, recepción de viajes (sin duplicar por id) y una página del equipo para ver viajes con fotos, aprobarlos o rechazarlos y simular fallas.
- Login con Retrofit y sesión en DataStore. Al entrar se baja la campaña.
- WorkManager envía los viajes pendientes cuando hay conexión, reintenta con espera creciente y repite cada 15 minutos; también trae las aprobaciones. Botón Actualizar en Historial.
- Permiso de red local de Android 17 (sin él, la app no llega a la computadora).
- App bloqueada en vertical y modo prueba en desarrollo (mantener apretado "Sacar foto" usa el QR de ejemplo).

### Etapa 6 · Calidad (en curso)

- [ ] Aviso sin conexión y hora de la última sincronización.
- [ ] Accesibilidad: títulos como encabezados para TalkBack, descripciones y tamaños táctiles.
- [ ] Más tests: ViewModels (foto y login) y pantallas.
- [ ] Migraciones de Room reales en lugar de borrar la base al cambiar de versión.

## Decisiones importantes

| Decisión | Por qué |
| --- | --- |
| Room como fuente de verdad (Offline First) | El conductor maneja con mala señal: nada se pierde y todo se ve sin conexión |
| Viaje con id generado en el teléfono | Si un envío se reintenta, el servidor reconoce el mismo viaje y no lo duplica |
| Servicio en primer plano para el GPS | Android corta el GPS de las apps en segundo plano; con el servicio el recorrido sigue |
| QR en el vinilo verificado con ML Kit | Funciona sin conexión; el límite (no prueba que el vinilo siguió puesto) se cubre con revisión del equipo |
| Fijo por días, sin mínimo de km | Respuesta a la devolución: premia la constancia y no incentiva recorridos extra |
| API simulada en Node | Corre igual en Mac y Windows sin instalar librerías, y permite mostrar en la defensa la aprobación, el modo sin conexión y los envíos parciales |
| Bloqueo en vertical | El diseño es vertical y la app se usa con el teléfono en un soporte |

## Cómo probar todo

1. Levantar la API: `cd api-mock` y `node server.js` (detalles en `api-mock/README.md`).
2. Run desde Android Studio e iniciar sesión con `lucia@rodando.com.ar` / `rodando123`. En Android 17 aceptar el permiso de red local.
3. Viaje completo: Iniciar viaje → foto (mantener apretado "Sacar foto" usa el QR de ejemplo) → reproducir una ruta GPX en *Extended controls → Location* → Finalizar → foto final.
4. Equipo: abrir <http://localhost:3000>, aprobar el viaje y tocar Actualizar en Historial. Ganancias muestra lo confirmado.
5. Sin conexión: cortar el servidor; los viajes quedan "Sin enviar" y se envían solos al volver.
6. Envíos parciales: en la página del equipo, "Falla uno de cada dos".
7. Tests: `./gradlew test`.

## Problemas conocidos y soluciones

| Problema | Solución |
| --- | --- |
| "No hay conexión con el servidor" en el login | Revisar que la API esté levantada y aceptar el permiso de red local. Probar `http://10.0.2.2:3000` en el Chrome del emulador |
| El emulador no tiene internet | *Device Manager → ⋮ → Cold Boot Now*; apagar VPN o filtros de red de la computadora |
| `./gradlew` pide un Java 25 | Borrar `gradle/gradle-daemon-jvm.properties` (lo crea Android Studio) |
| El viaje queda en 0 km en el emulador | Reproducir una ruta de `docs/emulador` o usar `USAR_UBICACION_SIMULADA = true` en `AppContainer` |
| El QR no aparece en la sala virtual | Cargarlo en *Extended controls → Camera → Wall*; está en una pared, no en la TV. O usar el modo prueba |
| Cambió la base de datos y la app falla al abrir | *Settings → Apps → Rodando → Storage → Clear storage* (hasta que estén las migraciones de la etapa 6) |

## Pendientes del equipo (fuera del código)

- Entrevistas con 5 a 8 anunciantes para validar el precio.
- Presupuestos de ploteo para 10 autos por tipo de vinilo.
- Requisito de ingreso de conductores (recorrido habitual).
- Pasar los cambios de la devolución al documento de la entrega final.
