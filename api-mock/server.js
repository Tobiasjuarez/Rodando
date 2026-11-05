// API simulada de Rodando. Hace de servidor de la empresa: login, campaña, recepción de viajes
// y una página para que "el equipo" apruebe o rechace los viajes.
//
// Solo usa módulos que vienen con Node (no hace falta npm install). Para levantarla:
//   node server.js
// La app la encuentra en http://10.0.2.2:3000 (así ve el emulador a la computadora).

const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const PUERTO = Number(process.env.PUERTO || 3000);
const ARCHIVO_DATOS = path.join(__dirname, 'datos.json');
const CARPETA_FOTOS = path.join(__dirname, 'fotos');

// ---------- Datos ----------

const CONDUCTORES = [
  { id: 'conductor-lucia', nombre: 'Lucía', email: 'lucia@rodando.com.ar', password: 'rodando123' },
];

function datosIniciales() {
  return { tokens: {}, viajes: {}, fallas: 'ninguna', envios: 0 };
}

function cargarDatos() {
  try {
    return JSON.parse(fs.readFileSync(ARCHIVO_DATOS, 'utf8'));
  } catch {
    return datosIniciales();
  }
}

let datos = cargarDatos();
const guardarDatos = () => fs.writeFileSync(ARCHIVO_DATOS, JSON.stringify(datos, null, 2));

fs.mkdirSync(CARPETA_FOTOS, { recursive: true });

/** Primer día del mes actual a las 00:00 en Buenos Aires (UTC-3), en milisegundos. */
function inicioDelMes(desplazamientoMeses = 0) {
  const ahora = new Date(Date.now() - 3 * 3600 * 1000);
  return Date.UTC(ahora.getUTCFullYear(), ahora.getUTCMonth() + desplazamientoMeses, 1, 3, 0, 0);
}

function campaniaVigente() {
  return {
    campania: {
      id: 'campania-demo',
      marca: 'Pampa Bebidas',
      fechaInicio: inicioDelMes(0),
      fechaFin: inicioDelMes(1) - 1,
    },
    vinilo: {
      tipo: 'PARCIAL',
      fijoMensual: 50000,
      tarifaPorKm: 50,
      fechaInstalacion: inicioDelMes(0),
      diasMinimos: 12,
      kmTope: 1500,
      codigo: 'RODANDO-VINILO-0001',
    },
  };
}

// ---------- Utilidades HTTP ----------

function responder(res, estado, cuerpo) {
  res.writeHead(estado, { 'Content-Type': 'application/json; charset=utf-8' });
  res.end(JSON.stringify(cuerpo));
}

function leerCuerpo(req) {
  return new Promise((resolver, rechazar) => {
    let partes = [];
    let tamanio = 0;
    req.on('data', (parte) => {
      tamanio += parte.length;
      if (tamanio > 30 * 1024 * 1024) rechazar(new Error('cuerpo demasiado grande'));
      partes.push(parte);
    });
    req.on('end', () => resolver(Buffer.concat(partes).toString('utf8')));
    req.on('error', rechazar);
  });
}

async function leerJson(req) {
  const texto = await leerCuerpo(req);
  return texto ? JSON.parse(texto) : {};
}

async function leerFormulario(req) {
  return Object.fromEntries(new URLSearchParams(await leerCuerpo(req)));
}

/** Devuelve el conductor del token "Bearer ..." o null. */
function conductorDe(req) {
  const [, token] = (req.headers.authorization || '').split(' ');
  const id = token && datos.tokens[token];
  return CONDUCTORES.find((c) => c.id === id) || null;
}

function redirigir(res, destino) {
  res.writeHead(303, { Location: destino });
  res.end();
}

const escapar = (texto) =>
  String(texto ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);

// ---------- API para la app ----------

async function login(req, res) {
  const { email, password } = await leerJson(req);
  const conductor = CONDUCTORES.find(
    (c) => c.email === String(email || '').trim().toLowerCase() && c.password === password
  );
  if (!conductor) return responder(res, 401, { error: 'Email o contraseña incorrectos' });
  const token = crypto.randomUUID();
  datos.tokens[token] = conductor.id;
  guardarDatos();
  responder(res, 200, { token, conductor: { id: conductor.id, nombre: conductor.nombre, email: conductor.email } });
}

/** Simula fallas de red para probar envíos parciales. Devuelve true si este envío tiene que fallar. */
function tocaFallar() {
  datos.envios += 1;
  if (datos.fallas === 'todas') return true;
  if (datos.fallas === 'cada-dos') return datos.envios % 2 === 0;
  return false;
}

function guardarFoto(viajeId, momento, base64) {
  if (!base64) return null;
  const nombre = `${viajeId}-${momento}.jpg`;
  fs.writeFileSync(path.join(CARPETA_FOTOS, nombre), Buffer.from(base64, 'base64'));
  return `/fotos/${nombre}`;
}

async function recibirViaje(req, res, conductor) {
  const viaje = await leerJson(req);
  if (!viaje.id) return responder(res, 400, { error: 'Falta el id del viaje' });

  // El mismo id ya llegó (por ejemplo, se perdió la respuesta y la app reintentó): no se duplica.
  const existente = datos.viajes[viaje.id];
  if (existente) {
    return responder(res, 200, { remoteId: existente.remoteId, verificacion: existente.verificacion, duplicado: true });
  }
  if (tocaFallar()) {
    guardarDatos();
    return responder(res, 503, { error: 'Falla simulada: el servidor no está disponible' });
  }

  const remoteId = `R-${String(Object.keys(datos.viajes).length + 1).padStart(4, '0')}`;
  datos.viajes[viaje.id] = {
    id: viaje.id,
    remoteId,
    conductorId: conductor.id,
    campaniaId: viaje.campaniaId,
    tipoVinilo: viaje.tipoVinilo,
    tarifaPorKm: viaje.tarifaPorKm,
    inicio: viaje.inicio,
    fin: viaje.fin,
    distanciaKm: viaje.distanciaKm,
    intentosFoto: viaje.intentosFoto,
    // La app propone por aprobar, en revisión o no computable; aprobar o rechazar lo decide el equipo.
    verificacion: viaje.verificacion || 'POR_APROBAR',
    puntos: (viaje.puntos || []).length,
    fotoInicio: guardarFoto(viaje.id, 'inicio', viaje.fotoInicio),
    fotoFin: guardarFoto(viaje.id, 'fin', viaje.fotoFin),
    recibido: Date.now(),
  };
  guardarDatos();
  responder(res, 201, { remoteId, verificacion: datos.viajes[viaje.id].verificacion, duplicado: false });
}

function estados(res, conductor) {
  const lista = Object.values(datos.viajes)
    .filter((v) => v.conductorId === conductor.id)
    .map((v) => ({ id: v.id, verificacion: v.verificacion }));
  responder(res, 200, lista);
}

// ---------- Página del equipo de Rodando ----------

const ETIQUETAS = {
  POR_APROBAR: 'Por aprobar',
  EN_REVISION: 'En revisión',
  APROBADO: 'Aprobado',
  RECHAZADO: 'Rechazado',
  NO_COMPUTABLE: 'No computable',
};

const fecha = (ms) =>
  ms ? new Date(ms).toLocaleString('es-AR', { timeZone: 'America/Argentina/Buenos_Aires' }) : '—';

function paginaEquipo(res) {
  const viajes = Object.values(datos.viajes).sort((a, b) => b.recibido - a.recibido);
  const filas = viajes
    .map(
      (v) => `
      <tr>
        <td><b>${escapar(v.remoteId)}</b><br><small>${escapar(v.id)}</small></td>
        <td>${fecha(v.inicio)}<br>${fecha(v.fin)}</td>
        <td>${Number(v.distanciaKm || 0).toFixed(1).replace('.', ',')} km<br><small>${v.puntos} puntos · ${v.intentosFoto} intento(s) de foto · $${escapar(v.tarifaPorKm)}/km</small></td>
        <td class="fotos">${v.fotoInicio ? `<a href="${v.fotoInicio}" target="_blank"><img src="${v.fotoInicio}" alt="Foto de inicio"></a>` : '<span>Sin foto</span>'}
            ${v.fotoFin ? `<a href="${v.fotoFin}" target="_blank"><img src="${v.fotoFin}" alt="Foto de fin"></a>` : '<span>Sin foto</span>'}</td>
        <td><span class="estado ${escapar(v.verificacion)}">${escapar(ETIQUETAS[v.verificacion] || v.verificacion)}</span></td>
        <td>
          <form method="post" action="/admin/viajes/${encodeURIComponent(v.id)}/aprobar"><button>Aprobar</button></form>
          <form method="post" action="/admin/viajes/${encodeURIComponent(v.id)}/rechazar"><button class="secundario">Rechazar</button></form>
        </td>
      </tr>`
    )
    .join('');
  const opcion = (valor, texto) => `<option value="${valor}" ${datos.fallas === valor ? 'selected' : ''}>${texto}</option>`;

  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(`<!doctype html>
<html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Rodando · Equipo</title>
<style>
  body { font-family: system-ui, sans-serif; margin: 24px; color: #1a1c1a; background: #f6faf6; }
  h1 { margin: 0 0 4px; } p { color: #555; margin: 0 0 16px; }
  table { border-collapse: collapse; width: 100%; background: white; }
  th, td { text-align: left; padding: 10px; border-bottom: 1px solid #dde5dd; vertical-align: top; }
  .fotos img { width: 96px; height: 72px; object-fit: cover; border-radius: 6px; margin-right: 4px; }
  .fotos span { color: #888; margin-right: 8px; }
  .estado { padding: 3px 8px; border-radius: 6px; background: #eee; white-space: nowrap; }
  .APROBADO { background: #c8ecd6; } .POR_APROBAR { background: #fde9b5; }
  .EN_REVISION, .RECHAZADO { background: #f9d4cf; }
  form { display: inline; } button { padding: 6px 12px; margin: 2px; border: 0; border-radius: 6px; background: #006c4c; color: white; cursor: pointer; }
  button.secundario { background: #e3e8e3; color: #1a1c1a; }
  .barra { display: flex; gap: 16px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
</style></head>
<body>
  <h1>Rodando · Equipo</h1>
  <p>Viajes recibidos desde la app. Aprobar o rechazar cambia lo que el conductor ve en Historial y Ganancias.</p>
  <div class="barra">
    <form method="post" action="/admin/fallas">
      <label>Simular fallas de envío:
        <select name="modo" onchange="this.form.submit()">
          ${opcion('ninguna', 'Ninguna')}${opcion('cada-dos', 'Falla uno de cada dos (envíos parciales)')}${opcion('todas', 'Fallan todos (servidor caído)')}
        </select>
      </label>
    </form>
    <a href="/">Actualizar</a>
  </div>
  <table>
    <thead><tr><th>Viaje</th><th>Inicio / fin</th><th>Recorrido</th><th>Fotos</th><th>Estado</th><th></th></tr></thead>
    <tbody>${filas || '<tr><td colspan="6">Todavía no llegó ningún viaje.</td></tr>'}</tbody>
  </table>
</body></html>`);
}

async function decidir(req, res, id, decision) {
  const viaje = datos.viajes[id];
  if (viaje) {
    viaje.verificacion = decision;
    guardarDatos();
  }
  redirigir(res, '/');
}

async function cambiarFallas(req, res) {
  const { modo } = await leerFormulario(req);
  if (['ninguna', 'cada-dos', 'todas'].includes(modo)) datos.fallas = modo;
  datos.envios = 0;
  guardarDatos();
  redirigir(res, '/');
}

function servirFoto(res, nombre) {
  const archivo = path.join(CARPETA_FOTOS, path.basename(nombre));
  if (!fs.existsSync(archivo)) return responder(res, 404, { error: 'No existe la foto' });
  res.writeHead(200, { 'Content-Type': 'image/jpeg' });
  fs.createReadStream(archivo).pipe(res);
}

// ---------- Rutas ----------

const servidor = http.createServer(async (req, res) => {
  const { pathname } = new URL(req.url, `http://${req.headers.host}`);
  console.log(new Date().toLocaleTimeString('es-AR'), req.method, pathname);
  try {
    if (req.method === 'GET' && pathname === '/') return paginaEquipo(res);
    if (req.method === 'GET' && pathname.startsWith('/fotos/')) return servirFoto(res, pathname.slice('/fotos/'.length));
    if (req.method === 'POST' && pathname === '/admin/fallas') return cambiarFallas(req, res);
    const decision = pathname.match(/^\/admin\/viajes\/([^/]+)\/(aprobar|rechazar)$/);
    if (req.method === 'POST' && decision) {
      return decidir(req, res, decodeURIComponent(decision[1]), decision[2] === 'aprobar' ? 'APROBADO' : 'RECHAZADO');
    }

    if (req.method === 'POST' && pathname === '/login') return login(req, res);

    const conductor = conductorDe(req);
    if (!conductor) return responder(res, 401, { error: 'Sesión vencida: volvé a iniciar sesión' });
    if (req.method === 'GET' && pathname === '/campania') return responder(res, 200, campaniaVigente());
    if (req.method === 'POST' && pathname === '/viajes') return recibirViaje(req, res, conductor);
    if (req.method === 'GET' && pathname === '/viajes/estados') return estados(res, conductor);

    responder(res, 404, { error: 'No existe esa ruta' });
  } catch (error) {
    console.error(error);
    responder(res, 500, { error: 'Error del servidor' });
  }
});

servidor.listen(PUERTO, () => {
  console.log(`API simulada de Rodando en http://localhost:${PUERTO}`);
  console.log('La app la usa en http://10.0.2.2:' + PUERTO + ' (emulador). Usuario: lucia@rodando.com.ar / rodando123');
});
