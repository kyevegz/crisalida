# Crisálida 
Por Carrillo Castillo Jimena y Vázquez Canchola Maryjose

App móvil (Android) de acompañamiento y emprendimiento para mujeres en situación de vulnerabilidad en zonas rurales de Guanajuato. Combina un **radar de talentos**, **microrretos** remunerados, un **marketplace** de productos y servicios, **bienestar emocional** privado, y una mascota virtual (**Lilu**) que crece con las flores que la usuaria gana. Incluye una **galería de mariposas** como app de camuflaje para proteger su seguridad.

## Repositorios
- **App Android (Kotlin + Jetpack Compose):** https://github.com/kyevegz/crisalida
- **Backend (Python + FastAPI):** https://github.com/kyevegz/crisalidaBackend 

## Tecnología
| Parte | Tecnología |
|---|---|
| App | Kotlin, Jetpack Compose, Retrofit |
| Servidor | Python, FastAPI, Pydantic 2, JWT, bcrypt, AES-256-GCM |
| Base de datos | PostgreSQL en Neon |
| Hospedaje del servidor | Render |

## Cómo instalar y usar la app
1. Descarga el archivo **`crisalida.apk`** en un celular Android.
2. Ábrelo y acepta **"Instalar apps de origen desconocido"** si el teléfono lo pide.
3. Abre **Crisálida** e inicia sesión con las credenciales de prueba.
4. Necesitas **conexión a internet**: la app consulta un servidor en la nube.

> **Importante:** debido a que el servidor está en un plan gratuito de Render, este se "duerme" tras unos minutos sin uso. La primera vez puede tardar **de 30 a 60 segundos** en responder. Si el inicio de sesión falla, es recomendable que se abra en el navegador la url `https://crisalidabackend.onrender.com/docs`, esperar a que cargue y volver a intentarlo en la app.

## Credenciales de prueba
| Modo | Seudónimo | PIN |
|---|---|---|
| Normal (app completa) | `carmina6` | `231197` |
| Camuflaje (galería de mariposas) | `carmina6` | `980236` |

También puedes crear una cuenta nueva con **Crear cuenta**: eliges seudónimo, un PIN de 6 dígitos y un PIN de camuflaje, y respondes el radar de talentos (unos minutos).

## Seguridad y privacidad
- Las usuarias se identifican con un **seudónimo** y un **PIN**; no se pide nombre real, correo ni teléfono.
- **PIN de camuflaje:** abre una galería de mariposas inofensiva y bloquea el acceso a los datos reales.
- Las compradoras solo ven la **zona pública** del producto. La dirección real se guarda **cifrada** y solo la ve quien publicó.
- Los textos públicos rechazan teléfonos, correos y ligas.
- Las reflexiones y la bitácora se guardan **cifradas (AES-256-GCM) en el servidor**. La llave vive en el servidor.

## Limitaciones hasta la versión de prueba actual
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

**App:** abre la carpeta en Android Studio, en `Apicrisalida.kt` pon `[BASE_URL](https://crisalidabackend.onrender.com/)` con la dirección del servidor (terminada en `/`) y ejecuta **Build → Build APK(s)**.
