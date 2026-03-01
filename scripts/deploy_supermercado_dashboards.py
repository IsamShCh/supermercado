import requests
import json
import time

# === CONFIGURACIÓN ===
METABASE_URL = "http://localhost:3000"
ADMIN_EMAIL = "admin@supermercado.com"
ADMIN_PASSWORD = "Password123!"

session = requests.Session()

def get_token():
    try:
        print(f"Conectando a {METABASE_URL}...")
        res = session.post(f"{METABASE_URL}/api/session", json={"username": ADMIN_EMAIL, "password": ADMIN_PASSWORD})
        if res.status_code == 200:
            session.headers.update({'X-Metabase-Session': res.json()['id']})
            print("Login exitoso.")
            return True
        print(f"Login fallido: {res.text}")
        return False
    except Exception as e:
        print(f"Error de conexión: {e}")
        return False

# === GESTIÓN DE BASE DE DATOS ===
def setup_database():
    """Busca la base de datos 'reportes-db' y devuelve su ID. Si no existe, la crea."""
    try:
        # 1. Obtener bases de datos existentes
        res = session.get(f'{METABASE_URL}/api/database')
        databases = res.json()
        if isinstance(databases, dict) and 'data' in databases: databases = databases['data']
        
        for db in databases:
            if db.get('name') == 'reportes-db':
                print(f"Base de datos 'reportes-db' encontrada (ID: {db.get('id')})")
                return db.get('id')

        # 2. Si no existe, crearla
        print("Base de datos no encontrada. Creando conexión...")
        db_config = {
            "engine": "postgres",
            "name": "reportes-db",
            "details": {
                "host": "reportes-db", 
                "port": 5432, 
                "dbname": "reportes-db",
                "user": "postgres", 
                "password": "contrasena_secreta",
                "ssl": False
            },
            "is_full_sync": True
        }
        create_res = session.post(f'{METABASE_URL}/api/database', json=db_config)
        if create_res.status_code == 200:
            new_id = create_res.json().get('id')
            print(f"Base de datos creada exitosamente (ID: {new_id})")
            return new_id
        else:
            print(f"Error creando base de datos: {create_res.text}")
            return None
    except Exception as e:
        print(f"Excepción en setup_database: {e}")
        return None

# === INICIO EJECUCIÓN ===
if not get_token(): exit(1)
DB_ID = setup_database()
if not DB_ID: exit(1)

# 1. Crear Colección Principal
collection_data = {"name": "TABLERO DE MANDO - JEFE DE PLANTA", "color": "#509EE3"}
# Intentar borrar si existe (limpieza) - Opcional, pero útil para desarrollo
# (Omitido para no borrar datos accidentalmente, creará una nueva si se ejecuta varias veces)
res_col = session.post(f'{METABASE_URL}/api/collection', json=collection_data)
if res_col.status_code == 200:
    COLLECTION_ID = res_col.json()["id"]
    print(f"Colección creada (ID: {COLLECTION_ID})")
else:
    # Si falla, probablemente ya existe, buscamos una genérica o fallamos
    print(f"No se pudo crear colección (quizás ya existe). Usando ID 1 (Our analytics).")
    COLLECTION_ID = 1 

# 2. Crear el Dashboard Vacío
dash_data = {"name": "Command Center - Supermercado", "collection_id": COLLECTION_ID, "parameters": []}
res_dash = session.post(f'{METABASE_URL}/api/dashboard', json=dash_data)
if res_dash.status_code == 200:
    DASHBOARD_ID = res_dash.json()["id"]
    print(f"Dashboard creado (ID: {DASHBOARD_ID})")
else:
    print(f"Error creando dashboard: {res_dash.text}")
    exit(1)

# === DEFINICIÓN DE TARJETAS (KPIs) ===

def create_card(name, sql, display="scalar", vis_settings={}, width=6, height=4):
    card_data = {
        "name": name,
        "collection_id": COLLECTION_ID,
        "dataset_query": {
            "database": DB_ID,
            "type": "native",
            "native": {"query": sql}
        },
        "display": display,
        "visualization_settings": vis_settings
    }
    res = session.post(f'{METABASE_URL}/api/card', json=card_data)
    if res.status_code != 200:
        print(f"Error creando tarjeta '{name}': {res.text}")
        return None
    
    card_id = res.json()['id']
    print(f"  -> Tarjeta creada: {name}")
    return {"card_id": card_id, "width": width, "height": height}

cards_config = []

print("\n--- Fila 1: LOS SEMÁFOROS (Vistazo Rápido) ---")

# KPI 1: Ventas Totales
sql_ventas = "SELECT SUM(total_linea) FROM fact_ventas WHERE fecha_hora >= CURRENT_DATE"
cards_config.append(create_card("Ventas Totales (Hoy)", sql_ventas, width=6, height=4))

# KPI 2: Tickets Totales
sql_tickets = "SELECT COUNT(DISTINCT numero_ticket) FROM fact_ventas WHERE fecha_hora >= CURRENT_DATE"
cards_config.append(create_card("Tickets (Hoy)", sql_tickets, width=6, height=4))

# KPI 3: Ticket Promedio
sql_ticket_avg = "SELECT CAST(SUM(total_linea) / NULLIF(COUNT(DISTINCT numero_ticket), 0) AS DECIMAL(10,2)) FROM fact_ventas WHERE fecha_hora >= CURRENT_DATE"
cards_config.append(create_card("Ticket Medio", sql_ticket_avg, width=6, height=4))

# KPI 4: Mermas (Pérdidas) - En Rojo
sql_mermas = "SELECT ABS(SUM(cantidad)) FROM fact_movimientos_inventario WHERE tipo_movimiento IN ('MERMA', 'CADUCADO', 'ROBO', 'AJUSTE_NEGATIVO') AND fecha_hora >= CURRENT_DATE"
cards_config.append(create_card("Pérdidas/Mermas (Unidades)", sql_mermas, width=6, height=4, 
                                vis_settings={"card.title_color": "#EF8C8C", "card.background_color": "#F9DCDC"}))

print("\n--- Fila 2: EL RITMO DE LA TIENDA (Operativa) ---")

# KPI 5: Ventas por Hora
sql_horas = """
SELECT EXTRACT(HOUR FROM fecha_hora) as hora, COUNT(DISTINCT numero_ticket) as transacciones 
FROM fact_ventas WHERE fecha_hora >= CURRENT_DATE 
GROUP BY hora ORDER BY hora
"""
cards_config.append(create_card("Ritmo de Ventas (Hora)", sql_horas, display="area", width=16, height=6, 
                                vis_settings={"graph.dimensions": ["hora"], "graph.metrics": ["transacciones"]}))

# KPI 6: Métodos de Pago
sql_pago = "SELECT metodo_pago, COUNT(DISTINCT numero_ticket) as total FROM fact_ventas WHERE fecha_hora >= CURRENT_DATE GROUP BY metodo_pago"
cards_config.append(create_card("Métodos de Pago", sql_pago, display="pie", width=8, height=6,
                                vis_settings={"pie.show_legend": True, "pie.show_total": True}))

print("\n--- Fila 3: PRODUCTO Y MERCANCÍA ---")

# KPI 7: Top 10 Productos
sql_top = """
SELECT nombre_producto_snapshot as Producto, SUM(cantidad) as Unidades, SUM(total_linea) as Ingresos 
FROM fact_ventas WHERE fecha_hora >= CURRENT_DATE 
GROUP BY nombre_producto_snapshot ORDER BY Ingresos DESC LIMIT 10
"""
cards_config.append(create_card("Top 10 Productos", sql_top, display="table", width=12, height=8))

# KPI 8: Ventas por Categoría
sql_cat = """
SELECT categoria_snapshot as Categoria, SUM(total_linea) as Ventas 
FROM fact_ventas WHERE fecha_hora >= CURRENT_DATE 
GROUP BY categoria_snapshot ORDER BY Ventas ASC
"""
cards_config.append(create_card("Ventas por Categoría", sql_cat, display="bar", width=12, height=8,
                                vis_settings={"graph.dimensions": ["categoria"], "graph.metrics": ["ventas"], "graph.x_axis.axis_enabled": False}))

print("\n--- Fila 4: LA TRASTIENDA (Inventario Crítico) ---")

# KPI 9: Movimientos Sospechosos
sql_sospecha = """
SELECT to_char(fecha_hora, 'HH24:MI') as Hora, sku, tipo_movimiento, motivo, cantidad 
FROM fact_movimientos_inventario 
WHERE tipo_movimiento IN ('MERMA', 'ROBO', 'AJUSTE_NEGATIVO') AND fecha_hora >= CURRENT_DATE 
ORDER BY fecha_hora DESC LIMIT 10
"""
cards_config.append(create_card("Últimos Movimientos Críticos", sql_sospecha, display="table", width=24, height=6))


# === COLOCACIÓN EN EL DASHBOARD (CORREGIDO) ===
print("\nConstruyendo el Dashboard...")

dashboard_cards = []

# Iteramos con 'enumerate' para generar un ID secuencial (i+1) que Metabase exige
for i, card_info in enumerate(cards_config):
    if not card_info: continue
    
    # Coordenadas exactas para tu diseño solicitado:
    # Fila 1 (Semáforos) -> Row 0
    # Fila 2 (Ritmo)     -> Row 4
    # Fila 3 (Producto)  -> Row 10
    # Fila 4 (Trastienda)-> Row 18
    
    idx = i # Usamos el índice original
    
    row = 0
    col = 0
    
    if idx <= 3:   # Fila 1 (4 KPIs de ancho 6)
        row = 0
        col = idx * 6
    elif idx == 4: # Ritmo Ventas (Ancho 16)
        row = 4
        col = 0
    elif idx == 5: # Métodos Pago (Ancho 8)
        row = 4
        col = 16
    elif idx == 6: # Top Prod (Ancho 12)
        row = 10
        col = 0
    elif idx == 7: # Categorias (Ancho 12)
        row = 10
        col = 12
    elif idx == 8: # Sospechosos (Ancho 24)
        row = 18
        col = 0

    dashboard_cards.append({
        "id": i + 1,  # <--- CORRECCIÓN CRÍTICA: ID secuencial único para el mapeo
        "card_id": card_info['card_id'],
        "row": row,
        "col": col,
        "size_x": card_info['width'],
        "size_y": card_info['height'],
        "series": [],
        "visualization_settings": {},
        "parameter_mappings": []
    })

# Enviar configuración al Dashboard
res_put = session.put(f'{METABASE_URL}/api/dashboard/{DASHBOARD_ID}/cards', json={"cards": dashboard_cards})

if res_put.status_code == 200:
    print(f"\n¡ÉXITO! Dashboard configurado correctamente.")
    print(f"   Visita: {METABASE_URL}/dashboard/{DASHBOARD_ID}")
else:
    print(f"\nError finalizando dashboard: {res_put.text}")