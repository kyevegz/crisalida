# Crisálida 
Por Carrillo Castillo Jimena y Vázquez Canchola Maryjose
App móvil (Android) de acompañamiento y emprendimiento para mujeres en situación de vulnerabilidad en zonas rurales de Guanajuato. Combina un **radar de talentos**, **microrretos** remunerados, un **marketplace** de productos y servicios, **bienestar emocional** privado, y una mascota virtual (**Lilu**) que crece con las flores que la usuaria gana. Incluye una **galería de mariposas** como app de camuflaje para proteger su seguridad.

## Repositorios
- **App Android (Kotlin + Jetpack Compose):** https://github.com/kyevegz/crisalida
- **Backend (Python + FastAPI):** https://github.com/kyevegz/crisalidaBackend 

## Tecnología
| Parte | Tecnología |
|---|---|
| App | Kotlin, Jetpack Compose, Retrofit, minSdk 24 |
| Servidor | Python, FastAPI, Pydantic 2, JWT, bcrypt, AES-256-GCM |
| Base de datos | PostgreSQL en Neon |
| Hospedaje del servidor | Render |

## Cómo instalar y usar la app
1. Descarga el archivo **`crisalida.apk`** en un celular Android.
2. Ábrelo y acepta **"Instalar apps de origen desconocido"** si el teléfono lo pide.
3. Abre **Crisálida** e inicia sesión con las credenciales de prueba.
4. Necesitas **conexión a internet**: la app consulta un servidor en la nube.

> **Importante:** debido a que el servidor está en un plan gratuito de Render, este se "duerme" tras unos minutos sin uso. La primera vez puede tardar **de 30 a 60 segundos** en responder. Si el inicio de sesión falla, es recomendable que se abra en el navegador la url `https://URL-DE-RENDER/docs`, esperar a que cargue y volver a intentarlo en la app.

## Credenciales de prueba
| Modo | Seudónimo | PIN |
|---|---|---|
| Normal (app completa) | `camina5` | `231197` |
| Camuflaje (galería de mariposas) | `camina5` | `980236` |

También puedes crear una cuenta nueva con **Crear cuenta**: eliges seudónimo, un PIN de 6 dígitos y un PIN de camuflaje, y respondes el radar de talentos (unos minutos).

## Casos de prueba

### Caso 1 · Acceso seguro y modo camuflaje
**Objetivo:** comprobar el inicio de sesión y que un segundo PIN abre una app falsa de mariposas.
| Paso | Acción | Resultado esperado |
|---|---|---|
| 1 | Abre la app, escribe `camina5` y el PIN `231197` (puedes tocar el ojito para verlo) | Entra a **Inicio** con tu nivel, flores y a Lilu |
| 2 | Ve a **Perfil → cerrar sesión** (o reinicia la app) | Regresa al inicio de sesión |
| 3 | Inicia sesión con `camina5` y el **PIN de camuflaje** | Se abre la **Galería de mariposas**, sin ningún dato de la usuaria |
| 4 | Menú ⋮ → **Cerrar galería** | Regresa al inicio de sesión |

### Caso 2 · Microreto: aceptar, entregar y ganar flores
**Objetivo:** probar el flujo de un microrreto y cómo suma al nivel y a la mascota.
| Paso | Acción | Resultado esperado |
|---|---|---|
| 1 | Entra a **Retos** y toca un reto | Se abre su **detalle**: foto, qué se pide, pago, duración, fecha límite |
| 2 | Toca **Aceptar el reto** | Mensaje de éxito; el reto aparece en **Mis retos** |
| 3 | En **Mis retos**, toca **Marcar como entregado** | Estado: *Esperando validación* |
| 4 | Simula al contratista: abre `https://URL-DE-RENDER/docs` → **Authorize** con el token de `POST /auth/login` → ejecuta `POST /demo/validar-reto/{aceptacion_id}` | El reto queda *Completado ✓* |
| 5 | Vuelve a **Inicio/Perfil** | **+30 flores**, **+1 actividad** en la barra de nivel y en "Retos completados" |

> El contratista aún no tiene su propia app; por eso su validación se simula con el panel `/demo` del servidor (activo solo en modo demo).

### Caso 3 · Publicar un producto y verlo en el marketplace
**Objetivo:** probar la publicación de un producto con fotos y su visualización.
| Paso | Acción | Resultado esperado |
|---|---|---|
| 1 | En **Perfil** toca **Añadir nuevo producto** | Se abre el formulario de publicación |
| 2 | Elige categoría, escribe título, descripción y precio, agrega 1 foto y toca **Vista previa** | Se ve la tarjeta tal como la verán las compradoras (con tu alias, no tu nombre) |
| 3 | Toca **Publicar Producto** | Mensaje de éxito (y +20 flores, máx. 1 vez al día) |
| 4 | Entra a **Marketplace** y toca tu producto | Se abre el **detalle**: fotos, precio, vendedora, entrega y descripción |
| 5 | En **Perfil → Portafolio** toca **Editar detalles**, cambia el precio y guarda | Cambios guardados; vuelve a la pantalla anterior |

### Extra · Bienestar emocional
Entra a **Bienestar**, guarda una emoción varias veces en el día (solo la primera da flores) y toca **Ver mis registros** para releer y borrar tus entradas. Lo que escribes se guarda **cifrado** en el servidor.

## Seguridad y privacidad (resumen)
- Las usuarias se identifican con un **seudónimo** y un **PIN**; no se pide nombre real, correo ni teléfono.
- **PIN de camuflaje:** abre una galería de mariposas inofensiva y bloquea el acceso a los datos reales.
- Las compradoras solo ven la **zona pública** del producto. La dirección real se guarda **cifrada** y solo la ve quien publicó.
- Los textos públicos rechazan teléfonos, correos y ligas.
- Las reflexiones y la bitácora se guardan **cifradas (AES-256-GCM) en el servidor**. La llave vive en el servidor, por lo que no es cifrado de extremo a extremo.

## Limitaciones conocidas (versión de prueba)
- El **proceso formal de compra** (pago en custodia y confirmación de entrega) aún no está disponible: "Comprar" muestra un aviso.
- **Contratistas y administradores** todavía no tienen su propia app: validar retos y aprobar publicaciones se simula con el panel `/demo` o con comandos SQL.
- El borrador de una publicación se guarda solo en el teléfono y no incluye fotos ni la dirección privada.
- El servidor gratuito puede tardar en responder la primera vez (ver nota arriba).

## Correr el proyecto desde el código
**Backend**
```bash
pip install -r requirements.txt
cp env.example .env        # llena tus datos de Neon y las llaves secretas
uvicorn main:app --host 0.0.0.0 --port 8000
```
En Render: *Build* `pip install -r requirements.txt` · *Start* `uvicorn main:app --host 0.0.0.0 --port $PORT`.

**App:** abre la carpeta en Android Studio, en `Apicrisalida.kt` pon `BASE_URL` con la dirección del servidor (terminada en `/`) y ejecuta **Build → Build APK(s)**.
