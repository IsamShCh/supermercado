# Cliente gRPC para grpcurl

Script de Python que permite hacer peticiones gRPC mediante grpcurl desde la línea de comandos, con soporte completo para autenticación JWT y gestión automática de tokens.

## Requisitos

- Python 3.6+
- grpcurl instalado y disponible en el PATH
- Archivos .proto del proyecto

## Uso Básico

### Sintaxis

```bash
python grpcurl_client.py --json <archivo.json> --host <host:puerto> \
    --proto-path <ruta> --proto <archivo.proto> \
    --service <servicio> --method <método> \
    [--token <token.txt>] [--save-token] [--verbose]
```

### Parámetros

- `--json`: Archivo JSON con los datos de la petición (obligatorio)
- `--host`: Host y puerto del servicio gRPC (obligatorio)
- `--proto-path`: Ruta al directorio con archivos .proto (obligatorio)
- `--proto`: Nombre del archivo .proto (obligatorio)
- `--service`: Nombre completo del servicio gRPC (obligatorio)
- `--method`: Método del servicio a invocar (obligatorio)
- `--token`: Archivo de texto con el token JWT (opcional)
- `--save-token`: Extraer y guardar automáticamente el token JWT de la respuesta (opcional)
- `--token-output`: Archivo donde guardar el token extraído (default: token.txt) (opcional)
- `--verbose, -v`: Mostrar información detallada de depuración (opcional)

## Ejemplos de Uso

### 1. Crear Categoría (sin autenticación)

```bash
python grpcurl_client.py \
    --json ejemplos_json/crear_categoria.json \
    --host localhost:9090 \
    --proto-path ./api/src/main/proto \
    --proto catalogo.proto \
    --service isam.catalogo.CatalogoService \
    --method CrearCategoria
```

### 2. Iniciar Sesión y guardar token automáticamente

```bash
python grpcurl_client.py \
    --json ejemplos_json/iniciar_sesion.json \
    --host localhost:9093 \
    --proto-path ./api/src/main/proto \
    --proto usuarios.proto \
    --service isam.usuarios.UsuarioService \
    --method IniciarSesion \
    --save-token
```

El token se guardará automáticamente en `token.txt`

### 3. Listar Permisos (con autenticación)

```bash
python grpcurl_client.py \
    --json ejemplos_json/listar_permisos.json \
    --token token.txt \
    --host localhost:9093 \
    --proto-path ./api/src/main/proto \
    --proto usuarios.proto \
    --service isam.usuarios.UsuarioService \
    --method ListarPermisos
```

### 4. Crear Usuario (con autenticación)

```bash
python grpcurl_client.py \
    --json ejemplos_json/crear_usuario.json \
    --token token.txt \
    --host localhost:9093 \
    --proto-path ./api/src/main/proto \
    --proto usuarios.proto \
    --service isam.usuarios.UsuarioService \
    --method CrearUsuario
```

### 5. Crear Producto

```bash
python grpcurl_client.py \
    --json ejemplos_json/crear_producto.json \
    --host localhost:9090 \
    --proto-path ./api/src/main/proto \
    --proto catalogo.proto \
    --service isam.catalogo.CatalogoService \
    --method CrearProducto
```

## Flujo Completo con Autenticación

### Paso 1: Iniciar sesión y guardar token automáticamente

```bash
# Iniciar sesión y guardar token automáticamente
python grpcurl_client.py \
    --json ejemplos_json/iniciar_sesion.json \
    --host localhost:9093 \
    --proto-path ./api/src/main/proto \
    --proto usuarios.proto \
    --service isam.usuarios.UsuarioService \
    --method IniciarSesion \
    --save-token
```

El token se extrae automáticamente y se guarda en `token.txt`

### Paso 2: Usar token para operaciones protegidas

```bash
# Listar permisos con token
python grpcurl_client.py \
    --json ejemplos_json/listar_permisos.json \
    --token token.txt \
    --host localhost:9093 \
    --proto-path ./api/src/main/proto \
    --proto usuarios.proto \
    --service isam.usuarios.UsuarioService \
    --method ListarPermisos
```

## Archivos de Ejemplo

### ejemplos_json/crear_categoria.json
```json
{
  "nombre_categoria": "Electrónica",
  "descripcion": "Productos electrónicos y gadgets"
}
```

### ejemplos_json/iniciar_sesion.json
```json
{
  "nombre_usuario": "admin",
  "password": "admin123"
}
```

### ejemplos_json/listar_permisos.json
```json
{}
```

### ejemplos_json/crear_usuario.json
```json
{
  "nombre_usuario": "jdoe",
  "nombre_completo": "John Doe",
  "email": "john.doe@example.com",
  "password": "TempPassword123!",
  "id_roles": ["USER"]
}
```

### ejemplos_json/crear_producto.json
```json
{
  "sku": "ELEC-001",
  "ean": "1234567890123",
  "nombre": "Smartphone Android",
  "descripcion": "Teléfono inteligente con sistema operativo Android",
  "precio_venta": 299.99,
  "caduca": false,
  "es_granel": false,
  "id_categoria": 1,
  "politica_rotacion": "FIFO",
  "unidad_medida": "UNIDAD",
  "etiquetas": ["electrónica", "smartphone", "android"]
}
```

## Servicios y Puertos Típicos

- **Catálogo**: `localhost:9090` - `isam.catalogo.CatalogoService`
- **Inventario**: `localhost:9091` - `isam.inventario.InventarioService`
- **Usuarios**: `localhost:9093` - `isam.usuarios.UsuarioService`
- **Ventas**: `localhost:9092` - `isam.ventas.VentasService`

## Métodos Comunes

### Servicio Catálogo
- `CrearCategoria`
- `CrearProducto`
- `BuscarProductos`
- `ConsultarProducto`

### Servicio Inventario
- `CrearInventario`
- `RegistrarNuevasExistencias`
- `ConsultarInventario`
- `AjustarInventarioManual`

### Servicio Usuarios
- `IniciarSesion`
- `CrearUsuario`
- `ConsultarUsuarios`
- `ListarPermisos`
- `ListarRoles`
- `VerificarToken`

## Tips y Troubleshooting

### Verificar que los servicios están corriendo

```bash
# Verificar contenedores Docker
docker ps

# O verificar procesos Java
ps aux | grep java
```

### Errores comunes

1. **"No such file or directory"**: Verifica que grpcurl esté instalado y en el PATH
2. **"Connection refused"**: Asegúrate que el servicio gRPC está corriendo en el puerto indicado
3. **"Token not found"**: Verifica que el archivo token.txt exista y contenga un token válido
4. **"Proto file not found"**: Verifica la ruta al archivo .proto
5. **"Unauthenticated"**: Debes proporcionar un token válido para endpoints protegidos
6. **"Method not found"**: Verifica el nombre del servicio y método en el archivo .proto

### Depuración

Usa el flag `--verbose` para ver información detallada:

```bash
python grpcurl_client.py --verbose --json datos.json --host localhost:9090 \
    --proto-path ./api/src/main/proto --proto catalogo.proto \
    --service isam.catalogo.CatalogoService --method CrearCategoria
```

### Gestión Automática de Tokens

El cliente puede extraer automáticamente tokens JWT de respuestas de login:

```bash
# Login con extracción automática de token
python grpcurl_client.py \
    --json login.json \
    --host localhost:9093 \
    --proto-path ./api/src/main/proto \
    --proto usuarios.proto \
    --service isam.usuarios.UsuarioService \
    --method IniciarSesion \
    --save-token \
    --token-output mi_token.txt
```

El cliente buscará automáticamente el token en campos como:
- `tokenJwt`
- `token_jwt`
- `token`
- `accessToken`
- `access_token`

## Alternativa Directa con grpcurl

Si prefieres usar grpcurl directamente sin el script de Python:

```bash
# Sin autenticación
grpcurl -plaintext -import-path ./api/src/main/proto -proto catalogo.proto \
    -d '{"nombre_categoria": "Electrónica", "descripcion": "Productos electrónicos"}' \
    localhost:9090 isam.catalogo.CatalogoService/CrearCategoria

# Con autenticación
grpcurl -plaintext -import-path ./api/src/main/proto -proto usuarios.proto \
    -rpc-header "authorization: Bearer TU_TOKEN_AQUI" \
    -d '{"nombre_usuario": "jdoe", "password": "password123"}' \
    localhost:9092 isam.usuarios.UsuariosService/IniciarSesion
```

El cliente Python simplemente automatiza la construcción de estos comandos.

---

## 📋 Referencia de Puertos

| Servicio | Puerto gRPC | Archivo Proto | Nombre del Servicio |
| :--- | :--- | :--- | :--- |
| **Usuarios** | `9093` | `usuarios.proto` | `isam.usuarios.UsuarioService` |
| **Catálogo** | `9090` | `catalogo.proto` | `isam.catalogo.CatalogoService` |
| **Inventario** | `9091` | `inventario.proto` | `isam.inventario.InventarioService` |
| **Ventas** | `9092` | `ventas.proto` | `isam.ventas.VentasService` |

## 🚀 Características Principales

- ✅ **Autenticación JWT**: Soporte completo para tokens Bearer
- ✅ **Gestión Automática de Tokens**: Extracción y guardado automático
- ✅ **Múltiples Campos de Token**: Detección automática de diferentes formatos
- ✅ **Manejo de Errores**: Mensajes claros y descriptivos
- ✅ **Depuración**: Modo verbose para troubleshooting
- ✅ **UTF-8**: Soporte completo para caracteres especiales
- ✅ **Cross-platform**: Funciona en Windows, Linux y macOS

## 🔄 Flujo de Trabajo Típico

1. **Login**: `--save-token` para obtener y guardar token
2. **Operaciones**: `--token token.txt` para usar el token guardado
3. **Seguridad**: Rechazo automático de peticiones sin autenticación

---

## 📝 Ejemplos Rápidos

```bash
# 1. Login y guardar token
python grpcurl_client.py --json login.json --host localhost:9093 \
    --proto-path ./api/src/main/proto --proto usuarios.proto \
    --service isam.usuarios.UsuarioService --method IniciarSesion \
    --save-token

# 2. Listar recursos con token
python grpcurl_client.py --json listar.json --token token.txt \
    --host localhost:9093 --proto-path ./api/src/main/proto \
    --proto usuarios.proto --service isam.usuarios.UsuarioService \
    --method ListarPermisos

# 3. Crear recurso sin autenticación
python grpcurl_client.py --json crear_categoria.json \
    --host localhost:9090 --proto-path ./api/src/main/proto \
    --proto catalogo.proto --service isam.catalogo.CatalogoService \
    --method CrearCategoria
```