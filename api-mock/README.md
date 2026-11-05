# API simulada de Rodando

Hace de servidor de la empresa: login, campaña vigente, recepción de viajes y una página para que el equipo los apruebe o rechace. Usa solo Node, sin dependencias.

## Cómo levantarla (Mac o Windows)

1. Instalar Node LTS desde [nodejs.org](https://nodejs.org) (una sola vez).
2. Abrir una terminal en esta carpeta (`api-mock`) y correr:

   ```
   node server.js
   ```

3. Dejar la terminal abierta. La página del equipo queda en <http://localhost:3000>.

La app se conecta a `http://10.0.2.2:3000`, que es como el emulador de Android ve a la computadora, en Mac y en Windows. En un teléfono real hay que cambiar `URL_API` en `app/src/main/java/com/rodando/app/data/remote/ClienteApi.kt` por la IP de la computadora en la red Wi-Fi.

**Usuario de prueba:** `lucia@rodando.com.ar` / `rodando123`

En Android 17 o más nuevo, la app pide el permiso de **red local** (o "Dispositivos cercanos") antes de iniciar sesión: sin él Android corta las conexiones a la computadora y el login da "No hay conexión con el servidor".

## Rutas

| Método y ruta | Para qué |
| --- | --- |
| `POST /login` | Email y contraseña → token |
| `GET /campania` | Campaña y vinilo vigentes (tarifas y código QR) |
| `POST /viajes` | Recibe un viaje con su recorrido y fotos. Si el id ya llegó, no lo duplica |
| `GET /viajes/estados` | Qué viajes aprobó o rechazó el equipo |
| `GET /` | Página del equipo: ver viajes y fotos, aprobar, rechazar, simular fallas |

Todas menos `/login` y la página del equipo piden `Authorization: Bearer <token>`.

## Para la defensa

- **Sin conexión:** cortar el servidor (Ctrl + C). La app sigue andando y los viajes quedan "Sin enviar"; al volver a levantarlo se envían solos.
- **Envíos parciales:** en la página, "Simular fallas → Falla uno de cada dos". De varios viajes pendientes llegan algunos y el resto se reintenta.
- **Aprobación:** aprobar un viaje en la página y tocar "Actualizar" en Historial: pasa a "Aprobado" y suma a lo confirmado en Ganancias.

Los datos se guardan en `datos.json` y las fotos en `fotos/` (no se suben a git). Para empezar de cero, borrar los dos.
