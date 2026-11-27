<p align="center"><img src="docs/logo_rodando.png" alt="Rodando — Ganá rodando" width="420"></p>

# Rodando

App Android para que conductores particulares del AMBA ganen un ingreso extra con su auto haciendo sus recorridos de siempre. El conductor lleva publicidad en vinilo, registra cada viaje con foto y GPS, y ve cuánto gana, aun sin conexión.

Trabajo Práctico Obligatorio — Desarrollo de Aplicaciones I (UADE), 2.º cuatrimestre 2026.

## Equipo

| Integrante | Rol principal |
| --- | --- |
| Tobías Juárez | Arquitectura, persistencia e integración |
| Matías Fernández | UI / UX |
| Lucas Casas | Sensores y testing |

## Requisitos funcionales (v1)

Los cuatro requisitos de la v1 están implementados. Lo que cambió respecto de la preentrega viene de la devolución del profesor y está explicado en [`docs/PLAN.md`](docs/PLAN.md#cambios-por-la-devolución-de-la-preentrega-11).

| RF | Qué hace | Estado |
| --- | --- | --- |
| **RF01** — Iniciar viaje | Foto del vinilo con CameraX, verificada en el teléfono con ML Kit (código QR). Hasta 3 intentos; después se inicia igual con el viaje "En revisión" o se reporta el vinilo dañado. El GPS registra el recorrido en un servicio en primer plano, con mapa. | Hecho |
| **RF02** — Finalizar viaje | Foto final con la misma verificación. Se calculan distancia (limpiando saltos y posiciones imprecisas) y duración. Menos de 0,5 km queda "No computable". | Hecho |
| **RF03** — Historial | Viajes agrupados por día con su estado de envío y de verificación, y detalle con el recorrido y las fotos. Funciona sin conexión. Muestra si se está enviando, si espera conexión o si reintenta. | Hecho |
| **RF04** — Ganancias | Ganancia del mes confirmada (viajes aprobados) y estimada (más los que esperan aprobación): fijo mensual según el vinilo, entero con 12 días con viajes y proporcional por debajo, + monto por km con la tarifa de cada viaje (tope de 1.500 km). No hay mínimo de km, para no incentivar recorridos extra. Inicio muestra la última sincronización. | Hecho |

Además: login contra la API simulada (si el servidor rechaza la sesión, vuelve al login con un aviso), envío automático de viajes con WorkManager y aprobación desde la página del equipo.

### Requisitos no funcionales

- **Offline First:** todas las pantallas leen de Room. Sin conexión se puede iniciar, finalizar y consultar viajes; un aviso arriba lo indica y los viajes se envían solos al volver la red.
- **Envíos confiables:** cada viaje tiene un id generado en el teléfono, así un reintento no lo duplica. Si fallan algunos, se envían los demás (envíos parciales).
- **Accesibilidad:** títulos marcados como encabezados para TalkBack, avisos que TalkBack anuncia solo, botones de al menos 48 dp que crecen con la letra grande. Revisado con TalkBack y con la letra al máximo.
- **Compatibilidad:** Android 8.0 (API 26) en adelante, probado en Android 17.
- **Batería y privacidad:** el GPS solo corre durante un viaje, con notificación fija. Las fotos quedan en la carpeta privada de la app. La red se anota en Logcat solo en desarrollo y el token no entra en el backup de Android.
- **Tests:** más de 100 tests unitarios (casos de uso, repositorio, sincronización, ViewModels y pantallas sin Android). `./gradlew test`.

### Estados de un viaje

Cada viaje tiene dos estados independientes:

| Envío (técnico) | Verificación (negocio) |
| --- | --- |
| **Sin enviar**: está solo en el teléfono. | **Por aprobar**: la foto se reconoció; falta que el equipo lo apruebe. |
| **Enviado**: llegó al servidor. | **En revisión**: la foto no se reconoció; el equipo la revisa a mano. |
| | **Aprobado**: se cobra. |
| | **Rechazado** / **No computable** (menos de 0,5 km): queda registrado pero no se paga. |

Un viaje enviado puede seguir sin aprobar. Cada viaje guarda la tarifa por km vigente al iniciarse, así un cambio de tarifa no altera lo ya recorrido.

## Tecnologías

Kotlin · Jetpack Compose + Material 3 · ViewModel + Coroutines + StateFlow · Navigation Compose · Room · DataStore · Retrofit · WorkManager · Fused Location · CameraX · ML Kit · osmdroid · JUnit.

Las dependencias se agregan a medida que se implementa cada requisito.

## Arquitectura

MVVM con principios de Clean Architecture y estrategia Offline First (Room es la fuente de verdad de la UI).

```
UI (Compose) → ViewModel → Caso de uso → Repositorio → Room / API / Sensores
```

```
app/src/main/java/com/rodando/app/
├── presentation/      # Pantallas Compose, ViewModels, UiState, tema
│   ├── common/        # UiState y componentes compartidos (estado vacío)
│   ├── navegacion/    # Barra inferior y NavHost (Inicio, Historial, Ganancias)
│   ├── login/         # Inicio de sesión contra la API simulada
│   ├── inicio/        # Campaña activa, vinilo y resumen del mes
│   ├── foto/          # RF01 / RF02: foto del vinilo al iniciar y al finalizar
│   ├── encurso/       # RF01: viaje en curso con tiempo, distancia y recorrido
│   ├── guardado/      # RF02: confirmación del viaje guardado
│   ├── historial/     # RF03: lista de viajes agrupada por día
│   ├── detalle/       # RF03: detalle del viaje con el recorrido
│   ├── ganancias/     # RF04: ganancia estimada y barras de objetivos
│   └── theme/         # Paleta de colores del diseño (Material 3)
├── domain/            # Kotlin puro: no depende de Android
│   ├── model/         # Viaje, Campania, PuntoGps, Vinilo, Sesion, ResumenGanancia, ObjetivoMensual
│   ├── repository/    # Interfaces de repositorio
│   └── usecase/       # Reglas de negocio (iniciar y finalizar viaje, distancia, ganancia, intentos de foto)
└── data/
    ├── local/         # Room (entidades, DAO, mappers), sesión en DataStore y viajes de ejemplo
    ├── remote/        # Retrofit (API simulada), DTOs y preparación de fotos para enviar
    ├── sync/          # WorkManager: envío de viajes y consulta de aprobaciones
    ├── sensors/       # GPS (Fused Location), servicio en primer plano del viaje, permisos y fotos
    └── repository/    # Implementaciones de los repositorios
```

## Cómo ejecutar

1. Levantar la API simulada: instalar [Node](https://nodejs.org) (una vez) y, en una terminal dentro de `api-mock/`, correr `node server.js`. Funciona igual en Mac y en Windows. Más detalles en [`api-mock/README.md`](api-mock/README.md).
2. Abrir la carpeta del proyecto en Android Studio y esperar el *Gradle Sync*.
3. Elegir un emulador (Android 8.0 / API 26 o superior) y tocar **Run**.
4. Iniciar sesión con `lucia@rodando.com.ar` / `rodando123`.
5. Tests unitarios: clic derecho en `app/src/test` → **Run Tests**, o `./gradlew test`.

La primera vez que se abre, la app carga viajes de ejemplo en Room para poder probar Historial y Detalle sin GPS ni cámara. También carga una campaña (Pampa Bebidas) y un vinilo Parcial ($50.000/mes + $50/km) para Inicio y Ganancias. Para empezar de cero: *Settings → Apps → Rodando → Storage → Clear storage*.

**Probar un viaje:** en Inicio, *Iniciar viaje*. La primera vez la app pide permiso de ubicación y de notificaciones. Después se saca la foto del vinilo con la cámara (CameraX), se revisa y se usa con *Usar foto e iniciar*, o se repite. Las fotos se guardan en la carpeta privada de la app y se ven en el detalle del viaje. ML Kit busca en la foto el código QR del vinilo, sin conexión: si no lo encuentra se repite (hasta 3 intentos) y después se puede seguir igual, con el viaje en revisión. El vinilo de ejemplo tiene el código `RODANDO-VINILO-0001`. El recorrido se registra con el GPS dentro de un servicio en primer plano, con una notificación fija, así que sigue aunque se bloquee el teléfono. Mientras dura el viaje, un mapa de OpenStreetMap (osmdroid, sin clave de API) sigue la posición y va dibujando el recorrido; sin conexión no se ven las calles, pero el recorrido se dibuja igual. *Finalizar viaje* pide la foto final y muestra el viaje guardado.

En el emulador la ubicación no cambia sola, así que un viaje queda en 0 km y "No computable". Para que se mueva hay dos formas:

- Con el viaje en curso, abrir *Extended controls (⋯) → Location*, importar uno de los recorridos de [`docs/emulador/`](docs/emulador/) con *Import GPX/KML* y tocar *Play*. La ruta corta (Ramos Mejía → Liniers, 3,7 km) tarda unos 6 minutos a velocidad 1x; la larga (hasta Plaza de Mayo, 18,4 km), unos 30. Se puede acelerar con *Playback speed*.
- Poner `USAR_UBICACION_SIMULADA = true` en `AppContainer`: el auto "avanza" solo unos 50 m cada 2 segundos (90 km/h) desde Ramos Mejía, sin tocar el emulador.

Las horas se muestran siempre en hora argentina, aunque el emulador esté en otra zona horaria. Los textos del sistema (diálogos de permisos, opciones de la notificación) salen en el idioma del teléfono: para verlos en español, cambiar el idioma del emulador en *Settings → System → Languages*.

En el emulador la cámara trasera muestra una sala virtual que se recorre manteniendo Option (Alt) y usando WASD y el mouse. Para probar la verificación:

1. En *Extended controls (⋯) → Camera → Virtual scene images*, cargar [`docs/emulador/qr-vinilo-demo.png`](docs/emulador/qr-vinilo-demo.png) como imagen de la pared (*Wall*).
2. Con la cámara abierta en la app, caminar hasta la pared y encuadrar el QR: la foto queda "Vinilo verificado".
3. Para probar los 3 intentos, sacarle fotos a cualquier otra parte de la sala.

**Modo prueba (solo en desarrollo):** mantener apretado *Sacar foto* usa el QR de ejemplo en lugar de la cámara, y la foto se verifica igual con ML Kit. Sirve para la defensa o para probar rápido; la imagen está en `app/src/debug/assets`, así que no existe en la versión final.

Otra forma: en la configuración del emulador (*Device Manager → Edit → Advanced → Camera*), usar la webcam de la Mac como cámara trasera y mostrarle el QR desde el celular.

**Envío y aprobación de viajes:** cada viaje se guarda primero en el teléfono (Room) y WorkManager lo envía a la API apenas hay conexión, aunque la app esté cerrada; si falla, reintenta con espera creciente y además repite cada 15 minutos. La página del equipo, en <http://localhost:3000>, muestra los viajes recibidos con sus fotos y permite aprobarlos o rechazarlos; *Actualizar* en Historial trae esos cambios. Para la defensa:

- **Sin conexión:** cortar el servidor (Ctrl + C). La app sigue funcionando y los viajes quedan "Sin enviar"; al levantarlo de nuevo se envían solos.
- **Envíos parciales:** en la página del equipo, *Simular fallas → Falla uno de cada dos*. De varios pendientes llegan algunos y el resto se reintenta, sin duplicarse.
- **Aprobación:** aprobar un viaje en la página y tocar *Actualizar*: pasa a "Aprobado" y suma a lo confirmado en Ganancias.
- **Vinilo dañado:** después de 3 fotos sin verificar, *Reportar vinilo dañado* manda la última foto al equipo; aparece abajo en la página, en "Vinilos reportados".

## Limitaciones conocidas

Lo que sabemos que la v1 no resuelve, y por qué:

- **La verificación no es una prueba total.** Reconocer el QR muestra que el vinilo estaba en la foto, no que siguió puesto todo el viaje. Por eso el viaje queda "Por aprobar" hasta que el equipo mira las fotos, y lo confirmado en Ganancias son solo los aprobados.
- **La API es simulada.** `api-mock/server.js` hace de servidor de la empresa en la computadora, con un solo conductor de ejemplo y sin registro de cuentas. El token no vence por tiempo; si se reinicia la API con datos nuevos, la app vuelve al login.
- **Diseñado pero no implementado en la v1:** alta del conductor con turno de instalación del vinilo y cambio de vinilo (el reporte de vinilo dañado avisa al equipo, pero la coordinación sigue por fuera de la app).
- **Ubicación de la foto:** la foto no guarda sus propias coordenadas; se usa el primer y el último punto del recorrido.
- **Fotos en el teléfono:** se conservan después de enviarse, para verlas en el detalle sin conexión. No hay limpieza automática.
- **Reporte de vinilo dañado:** necesita conexión en el momento; si no hay, la app avisa y se puede reintentar.
- **Datos de ejemplo:** la primera vez se cargan viajes, campaña y vinilo de ejemplo para poder probar sin GPS ni cámara.
- **Gradle:** `android.builtInKotlin=false` y `android.newDsl=false` en `gradle.properties` hacen falta para que KSP (Room) funcione con AGP 9. Las advertencias de "deprecated" al compilar son esperables.

## Forma de trabajo

- **Ramas:** `main` siempre compila. Cada tarea va en su rama: `feature/rf01-iniciar-viaje`, `fix/<problema>`, `docs/<tema>`.
- **Commits:** chicos y frecuentes, con prefijo: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`.
- **Pull requests:** al menos un compañero revisa antes de mergear. La descripción indica qué requisito cubre y cómo se probó.
- **Tareas:** GitHub Issues + Projects (Por hacer / En curso / Hecho).

## Documentación

- Plan de desarrollo con todas las etapas, decisiones y pendientes: [`docs/PLAN.md`](docs/PLAN.md).
- Guía para la demo y la defensa: [`docs/DEFENSA.md`](docs/DEFENSA.md).
- Preentrega, diagramas y diseño en Figma: ver [`docs/`](docs/).
