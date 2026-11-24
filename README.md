# Crisálida 
Por Carrillo Castillo Jimena y Vázquez Canchola Maryjose

App móvil (Android) de acompañamiento y emprendimiento para mujeres en situación de vulnerabilidad en zonas rurales de Guanajuato. Combina un **radar de talentos**, **microrretos** remunerados, un **marketplace** de productos y servicios, **bienestar emocional** privado, y una mascota virtual (**Lilu**) que crece con las flores que la usuaria gana. Incluye una **galería de mariposas** como app de camuflaje para proteger su seguridad.

## Tecnología
| Parte | Tecnología |
|---|---|
| App | Kotlin, Jetpack Compose, Retrofit |
| Servidor | Python, FastAPI, Pydantic 2, JWT, bcrypt, AES-256-GCM |
| Base de datos | PostgreSQL en Neon |
| Hospedaje del servidor | Render |

## Cómo instalar y usar la app
1. Descarga el archivo **`crisalidaV1.apk`** desde la [última Release](https://github.com/kyevegz/crisalida/releases/latest) en un celular Android.
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

## Mediciones de rendimiento |
| Medición |	Resultado |
|---|---|
| Fecha y teléfono |	23/11/2026, motorola moto g(20) |
| Versión	| 1.1.0 (código 2), release firmado |
| Tamaño del APK	| 18.4 MB (descarga de 17.8 MB); el APK que revisó el profesor pesaba unos 26 MB |
| Arranque en frío (5 pruebas)	| 1077, 626, 599, 673 y 603 ms; promedio 716 ms y mediana 626 ms |
| Memoria (PSS total)	| 77.5 MB al inicio y 115.3 MB después de recorrer la app |
| Respuesta del servidor (/docs, 5 pruebas)	| 0.51, 0.35, 0.31, 0.31 y 0.38 s; promedio 0.37 s |

Interpretación:
- Arranque: Android vitals considera excesivo un arranque en frío de 5 segundos o más.
- Memoria: La primera lectura fue en Inicio y la última después de abrir las pantallas (Inicio, retos, marketplace, bienestar, perfil y configuración). Subió unos 38 MB al navegar, y ahí se estabilizó cerca de 118 MB.
- Servidor: el servidor estaba despierto así que no tomó en cuenta el arranque en frío de Render.

**App:** abre la carpeta en Android Studio, en `Apicrisalida.kt` pon `[BASE_URL](https://crisalidabackend.onrender.com/)` con la dirección del servidor (terminada en `/`) y ejecuta **Build → Build APK(s)**.
