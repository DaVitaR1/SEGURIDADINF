# CLAUDE.md — Proyecto SEGURIDADINF
> Este archivo es el contexto completo del proyecto para Claude Code en VS Code.
> Lee todo antes de tocar cualquier archivo. No inventes clases ni cambies el estilo de código existente.

---

## ¿Qué es este proyecto?

Sistema de cuestionarios en Java/Swing con NetBeans + Ant + SQLite.
El usuario se loguea, elige un tema, responde preguntas y al terminar recibe un certificado.
**Objetivo académico:** añadir criptografía real sin romper lo que ya funciona.

---

## Arquitectura actual (NO cambiar la estructura de paquetes)

```
src/
├── datos/
│   └── DatabaseManager.java      ← acceso a SQLite, toda la lógica de BD
├── logica/
│   ├── HiloInicioRegistro.java   ← maneja login/registro en hilo separado
│   ├── HiloAlmacenarRespuestas.java
│   ├── HiloCalificacionNivel.java
│   ├── HiloControlTiempo.java
│   ├── HiloEvaluarRespuestas.java
│   ├── HiloMostrarPreguntas.java
│   ├── HiloSesion.java
│   └── GestorSugerencias.java
├── modelo/
│   ├── Usuario.java              ← tiene: id, nombreUsuario, password
│   ├── Pregunta.java
│   ├── Opcion.java
│   └── CursoSugerido.java
├── vistas/
│   ├── VistaLoginCodigo.java     ← pantalla de login/registro (main aquí)
│   ├── VistaCuestionario.java    ← pantalla del quiz con timer
│   ├── VistaCertificado.java     ← certificado visual + PrinterJob para PDF
│   └── SplashScreen.java
└── recursos/
    └── logo.png
```

**Flujo de la app:**
`SplashScreen` → `VistaLoginCodigo` → (selección de tema) → `VistaCuestionario` → `VistaCertificado`

---

## Base de datos (quiz.db — SQLite)

**Tablas existentes:**
```sql
usuarios        (id INTEGER PK, nombre_usuario TEXT, password TEXT)
preguntas       (id INTEGER PK, texto_pregunta TEXT, tema TEXT)
opciones        (id INTEGER PK, pregunta_id INTEGER FK, texto_opcion TEXT, es_correcta BOOLEAN)
respuestas_usuario (id INTEGER PK, usuario_id INTEGER FK, pregunta_id INTEGER FK, opcion_seleccionada_id INTEGER FK)
```

**Tabla nueva que hay que crear:**
```sql
certificados    (id INTEGER PK, usuario_id INTEGER FK, tema TEXT, calificacion REAL,
                 hash_cert TEXT, firma_rsa TEXT, fecha_emision TEXT)
```

---

## Problemas de seguridad que HAY QUE corregir (en orden de prioridad)

### PROBLEMA 1 — Contraseñas en texto plano ⚠️ CRÍTICO
**Archivo:** `datos/DatabaseManager.java`
**Línea problemática:** `WHERE password = ?` compara directo contra la BD
**Fix:** PBKDF2WithHmacSHA256 + salt aleatorio. Almacenar como `"PBKDF2:salt_hex:hash_hex"`

### PROBLEMA 2 — Ruta hardcodeada ⚠️
**Archivo:** `datos/DatabaseManager.java`
**Línea problemática:** `"jdbc:sqlite:C:/Users/berpy/Documents/NetBeansProjects/PP/quiz.db"`
**Fix:** usar ruta relativa: `"jdbc:sqlite:quiz.db"`

### PROBLEMA 3 — Certificado sin integridad
**Archivo:** `vistas/VistaCertificado.java`
**Problema:** el certificado se imprime como pantalla (PrinterJob), no tiene hash ni firma.
Alguien puede editar el PDF y cambiar el nombre o la nota sin que nadie lo detecte.
**Fix:** generar firma digital RSA del contenido del certificado + mostrar código de verificación

---

## Lo que hay que implementar — Plan exacto

### PASO 1: Crear `crypto/CryptoManager.java` (clase nueva)

Este es el único archivo de criptografía. Contiene SOLO métodos estáticos. No tiene estado.

```java
package crypto;

// Métodos que debe tener:

// --- Contraseñas ---
public static String hashPassword(String password)
// Genera salt random (16 bytes), deriva clave con PBKDF2WithHmacSHA256 (310.000 iteraciones, 256 bits)
// Devuelve: "PBKDF2:" + Hex(salt) + ":" + Hex(hash)

public static boolean verificarPassword(String passwordIngresado, String hashAlmacenado)
// Extrae salt del hash almacenado, rehashea el input, compara con constante de tiempo

// --- Hash SHA-256 ---
public static String sha256(String contenido)
// MessageDigest SHA-256 → devuelve Hex string (64 chars)

// --- RSA: Firma Digital ---
public static KeyPair generarParRSA()
// KeyPairGenerator RSA 2048 bits

public static String firmar(String contenido, PrivateKey privateKey)
// Signature SHA256withRSA → devuelve Base64

public static boolean verificarFirma(String contenido, String firmaBase64, PublicKey publicKey)
// Signature SHA256withRSA verify → true/false

// --- Gestión de claves del sistema ---
public static void guardarClaves(KeyPair kp, String rutaDir)
// Guarda public_key.b64 y private_key.b64 en el directorio indicado

public static KeyPair cargarClaves(String rutaDir)
// Carga las claves del disco. Si no existen, genera unas nuevas y las guarda.
```

**Librería:** solo `javax.crypto`, `java.security` — NATIVO DE JAVA, sin JARs externos.

---

### PASO 2: Modificar `datos/DatabaseManager.java`

**Cambio 1 — Fix ruta hardcodeada:**
```java
// ANTES:
private static final String URL = "jdbc:sqlite:C:/Users/berpy/Documents/NetBeansProjects/PP/quiz.db";
// DESPUÉS:
private static final String URL = "jdbc:sqlite:quiz.db";
```

**Cambio 2 — `registrarUsuario`:** antes de insertar, hashear el password:
```java
String hashSeguro = CryptoManager.hashPassword(password);
// guardar hashSeguro en lugar de password
```

**Cambio 3 — `validarUsuario`:** cambiar la query, ya NO compara en SQL:
```java
// Buscar solo por nombre_usuario, traer el hash, verificar en Java:
String sql = "SELECT password FROM usuarios WHERE nombre_usuario = ?";
// ... luego:
return CryptoManager.verificarPassword(passwordIngresado, hashAlmacenado);
```

**Cambio 4 — Nuevo método `guardarCertificado`:**
```java
public boolean guardarCertificado(int usuarioId, String tema, double calificacion,
                                   String hashCert, String firmaRsa)
// INSERT INTO certificados(usuario_id, tema, calificacion, hash_cert, firma_rsa, fecha_emision)
```

**Cambio 5 — Nuevo método `verificarCertificado`:**
```java
public String[] buscarCertificadoPorHash(String hashCert)
// SELECT * FROM certificados WHERE hash_cert = ?
// Devuelve array: [nombre_usuario, tema, calificacion, firma_rsa, fecha_emision] o null si no existe
```

---

### PASO 3: Modificar `vistas/VistaCertificado.java`

**Al construir el certificado:**
1. Armar un String con el contenido del certificado:
   ```java
   String contenidoCert = usuario.getNombreUsuario() + "|" + tema + "|" +
                          String.format("%.2f", calificacion) + "|" +
                          new java.util.Date().toString();
   ```
2. Calcular SHA-256: `String hashCert = CryptoManager.sha256(contenidoCert)`
3. Cargar claves RSA: `KeyPair kp = CryptoManager.cargarClaves(".")`
4. Firmar: `String firma = CryptoManager.firmar(contenidoCert, kp.getPrivate())`
5. Guardar en BD: `dbManager.guardarCertificado(usuario.getId(), tema, calificacion, hashCert, firma)`
6. Mostrar el hash en el certificado como "Código de verificación": primeros 16 chars del hash en un `JLabel` pequeño en el pie del certificado.

**Botón nuevo — "Verificar Certificado":**
- Abre `VistaVerificador` (clase nueva, ver PASO 4)

---

### PASO 4: Crear `vistas/VistaVerificador.java` (vista nueva)

Ventana simple (350x250) con:
- `JTextField` para pegar el código de verificación (hash)
- Botón "Verificar"
- `JLabel` de resultado: muestra ✅ "Certificado auténtico" en verde / ❌ "Certificado no encontrado o alterado" en rojo

**Lógica del botón verificar:**
1. Tomar el hash ingresado
2. Llamar `dbManager.buscarCertificadoPorHash(hash)`
3. Si null → mostrar ❌
4. Si encontró → cargar `public_key.b64`, verificar firma RSA con `CryptoManager.verificarFirma()`
5. Si firma válida → mostrar ✅ con nombre, tema, calificación y fecha
6. Si firma inválida → mostrar ❌ "El certificado fue alterado"

---

### PASO 5: Crear tabla `certificados` en quiz.db

Ejecutar este SQL en DB Browser for SQLite antes de probar:
```sql
CREATE TABLE IF NOT EXISTS certificados (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL,
    tema TEXT NOT NULL,
    calificacion REAL NOT NULL,
    hash_cert TEXT UNIQUE NOT NULL,
    firma_rsa TEXT NOT NULL,
    fecha_emision TEXT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
```

---

### PASO 6: Inicializar claves RSA al arrancar (una sola vez)

En `VistaLoginCodigo.main()`, antes de mostrar el splash, añadir:
```java
// Generar claves RSA si no existen todavía
crypto.CryptoManager.cargarClaves(".");
// Este método ya maneja el caso de "no existen" → las genera y guarda
```

---

## ⚠️ Convenciones de código — RESPETAR siempre

- Estilo NetBeans: llaves en línea nueva, espacios entre bloques
- Todo en ESPAÑOL: nombres de variables, comentarios, mensajes al usuario
- No usar lambdas complejas donde ya se usan clases anónimas (mantener consistencia)
- Swing: todas las actualizaciones de UI dentro de `SwingUtilities.invokeLater()`
- No usar librerías externas: todo lo criptográfico con APIs nativas de Java (`javax.crypto`, `java.security`)
- Manejo de excepciones: `try-catch` con `System.out.println("Error: " + e.getMessage())` (mantener el estilo actual)

---

## Archivos a CREAR (nuevos)
- `src/crypto/CryptoManager.java`
- `src/vistas/VistaVerificador.java`

## Archivos a MODIFICAR
- `src/datos/DatabaseManager.java` (pasos 1-5 descritos arriba)
- `src/vistas/VistaCertificado.java` (paso 3)
- `src/vistas/VistaLoginCodigo.java` (añadir inicialización de claves en main)

## Archivos que NO se tocan
- Todo en `logica/` excepto si el compilador exige ajustes menores
- `modelo/` — sin cambios
- `vistas/VistaCuestionario.java` — sin cambios
- `vistas/SplashScreen.java` — sin cambios
- `nbproject/`, `build.xml`, `manifest.mf` — sin cambios

---

## Demo de presentación (secuencia para mostrar al profesor)

1. Abrir `quiz.db` con DB Browser → mostrar que `password` está hasheada (no en texto plano)
2. Registrar usuario nuevo → verificar que en BD aparece `PBKDF2:abc123...:def456...`
3. Loguearse → hacer cuestionario → ver certificado con código de verificación al pie
4. Usar el Verificador con el código correcto → ✅ auténtico
5. Abrir el PDF, editar el nombre con un editor de texto → volver a verificar → ❌ alterado
6. Mostrar las claves RSA (`public_key.b64`, `private_key.b64`) y explicar para qué sirven

---

## Algoritmos utilizados y justificación (para la documentación)

| Algoritmo | Uso | Justificación |
|-----------|-----|---------------|
| PBKDF2WithHmacSHA256 | Hash de contraseñas | Estándar NIST, lento por diseño, con salt evita rainbow tables |
| AES-256-GCM | (opcional: cifrar preguntas en BD) | Cifrado autenticado: confidencialidad + integridad en una sola operación |
| RSA-2048 | Firma digital de certificados | Permite verificar autenticidad sin compartir clave privada |
| SHA-256 | Huella digital del certificado | Detecta cualquier alteración del contenido |

---

## Estado actual del proyecto al comenzar

- [x] Login/Registro funcional (pero contraseñas en texto plano)
- [x] Cuestionario con timer funcional
- [x] Certificado visual con PrinterJob funcional
- [x] Selector de temas funcional
- [ ] Contraseñas hasheadas
- [ ] Ruta de BD relativa
- [ ] CryptoManager implementado
- [ ] Certificado firmado digitalmente
- [ ] VistaVerificador implementada
- [ ] Tabla certificados creada en quiz.db
