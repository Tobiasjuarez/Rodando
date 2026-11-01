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

- **RF01** — Iniciar un viaje con foto del vinilo, verificada en el teléfono (hasta 3 intentos; si no, viaje "En revisión").
- **RF02** — Finalizar un viaje con foto; se calcula distancia y duración.
- **RF03** — Consultar el historial de viajes, aun sin conexión.
- **RF04** — Ver la ganancia estimada del mes: fijo mensual según el vinilo + monto por km (mínimos de 400 km y 12 días, tope de 1.500 km).

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
│   ├── inicio/        # Campaña activa, vinilo y resumen del mes
│   ├── foto/          # RF01 / RF02: foto del vinilo al iniciar y al finalizar
│   ├── encurso/       # RF01: viaje en curso con tiempo, distancia y recorrido
│   ├── guardado/      # RF02: confirmación del viaje guardado
│   ├── historial/     # RF03: lista de viajes agrupada por día
│   ├── detalle/       # RF03: detalle del viaje con el recorrido
│   ├── ganancias/     # RF04: ganancia estimada y barras de objetivos
│   └── theme/         # Paleta de colores del diseño (Material 3)
├── domain/            # Kotlin puro: no depende de Android
│   ├── model/         # Viaje, Campania, PuntoGps, Vinilo, ResumenGanancia, ObjetivoMensual
│   ├── repository/    # Interfaces de repositorio
│   └── usecase/       # Reglas de negocio (iniciar y finalizar viaje, distancia, ganancia, intentos de foto)
└── data/
    ├── local/         # Room (entidades, DAO, mappers) y viajes de ejemplo
    ├── remote/        # Retrofit (API simulada) y DTOs
    ├── sensors/       # GPS (Fused Location), servicio en primer plano del viaje, permisos y cámara
    └── repository/    # Implementaciones de los repositorios
```

## Cómo ejecutar

1. Abrir la carpeta del proyecto en Android Studio y esperar el *Gradle Sync*.
2. Elegir un emulador (Android 8.0 / API 26 o superior) y tocar **Run**.
3. Tests unitarios: clic derecho en `app/src/test` → **Run Tests**, o `./gradlew test`.

La primera vez que se abre, la app carga viajes de ejemplo en Room para poder probar Historial y Detalle sin GPS ni cámara. También carga una campaña (Pampa Bebidas) y un vinilo Parcial ($50.000/mes + $50/km) para Inicio y Ganancias. Para empezar de cero: *Settings → Apps → Rodando → Storage → Clear storage*.

**Probar un viaje:** en Inicio, *Iniciar viaje*. La primera vez la app pide permiso de ubicación y de notificaciones. Después viene *Sacar foto e iniciar* (la cámara todavía es simulada y la foto siempre se acepta). El recorrido se registra con el GPS dentro de un servicio en primer plano, con una notificación fija, así que sigue aunque se bloquee el teléfono. Mientras dura el viaje, un mapa (osmdroid con mosaicos de CARTO sobre datos de OpenStreetMap, sin clave de API) sigue la posición y va dibujando el recorrido; sin conexión no se ven las calles, pero el recorrido se dibuja igual. *Finalizar viaje* pide la foto final y muestra el viaje guardado.

En el emulador la ubicación no cambia sola, así que un viaje queda en 0 km y "No computable". Para que se mueva hay dos formas:

- Con el viaje en curso, abrir *Extended controls (⋯) → Location*, importar uno de los recorridos de [`docs/emulador/`](docs/emulador/) con *Import GPX/KML* y tocar *Play*. La ruta corta (Ramos Mejía → Liniers, 3,7 km) tarda unos 6 minutos a velocidad 1x; la larga (hasta Plaza de Mayo, 18,4 km), unos 30. Se puede acelerar con *Playback speed*.
- Poner `USAR_UBICACION_SIMULADA = true` en `AppContainer`: el auto "avanza" solo unos 50 m cada 2 segundos (90 km/h) desde Ramos Mejía, sin tocar el emulador.

Las horas se muestran siempre en hora argentina, aunque el emulador esté en otra zona horaria. Los textos del sistema (diálogos de permisos, opciones de la notificación) salen en el idioma del teléfono: para verlos en español, cambiar el idioma del emulador en *Settings → System → Languages*.

CameraX y la verificación con ML Kit se suman en las próximas ramas.

La API simulada y sus instrucciones se agregan en la carpeta `api-mock/` cuando se implemente la sincronización.

## Forma de trabajo

- **Ramas:** `main` siempre compila. Cada tarea va en su rama: `feature/rf01-iniciar-viaje`, `fix/<problema>`, `docs/<tema>`.
- **Commits:** chicos y frecuentes, con prefijo: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`.
- **Pull requests:** al menos un compañero revisa antes de mergear. La descripción indica qué requisito cubre y cómo se probó.
- **Tareas:** GitHub Issues + Projects (Por hacer / En curso / Hecho).

## Documentación

Preentrega, diagramas y diseño en Figma: ver [`docs/`](docs/).
