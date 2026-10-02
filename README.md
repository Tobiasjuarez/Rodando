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
│   ├── common/        # UiState (carga, contenido, vacío, error, offline)
│   ├── inicio/
│   └── theme/
├── domain/            # Kotlin puro: no depende de Android
│   ├── model/         # Viaje, Campania, PuntoGps, Vinilo, ResumenGanancia, ObjetivoMensual
│   ├── repository/    # Interfaces de repositorio
│   └── usecase/       # Reglas de negocio (distancia, ganancia)
└── data/
    ├── local/         # Room (entidades, DAO) y archivos de fotos
    ├── remote/        # Retrofit (API simulada) y DTOs
    ├── sensors/       # GPS y cámara
    └── repository/    # Implementaciones de los repositorios
```

## Cómo ejecutar

1. Abrir la carpeta del proyecto en Android Studio y esperar el *Gradle Sync*.
2. Elegir un emulador (Android 8.0 / API 26 o superior) y tocar **Run**.
3. Tests unitarios: clic derecho en `app/src/test` → **Run Tests**, o `./gradlew test`.

La API simulada y sus instrucciones se agregan en la carpeta `api-mock/` cuando se implemente la sincronización.

## Forma de trabajo

- **Ramas:** `main` siempre compila. Cada tarea va en su rama: `feature/rf01-iniciar-viaje`, `fix/<problema>`, `docs/<tema>`.
- **Commits:** chicos y frecuentes, con prefijo: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`.
- **Pull requests:** al menos un compañero revisa antes de mergear. La descripción indica qué requisito cubre y cómo se probó.
- **Tareas:** GitHub Issues + Projects (Por hacer / En curso / Hecho).

## Documentación

Preentrega, diagramas y diseño en Figma: ver [`docs/`](docs/).
