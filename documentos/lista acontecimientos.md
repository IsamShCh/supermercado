LISTA DE ACONTECIMIENTOS - SISTEMA DE GESTIÓN DE SUPERMERCADOS
#SECTION - : CATÁLOGO
#NOTE - AC1: Administrador de inventario crea un nuevo producto en el catálogo
Entidad Externa: Administrador de Inventario
Flujo de entrada: INFO_NUEVO_PRODUCTO = SKU + EAN/PLU + Nombre + Descripción + PrecioVenta + OfertaAplicada + FechaInicioOferta + FechaFinOferta + Caduca + EsGranel + IDCategoria + PoliticaRotacion + IDProveedor + UnidadMedida
Almacenes: PRODUCTOS
Requisitos satisfechos: RF1, RF13, RF14
#NOTE - AC2: Administrador de inventario modifica un producto existente
Entidad Externa: Administrador de Inventario
Flujo de entrada: INFO_MODIFICAR_PRODUCTO = SKU + DatosActualizar
Almacenes: PRODUCTOS
Requisitos satisfechos: RF2
#NOTE - AC3: Usuario consulta un producto por SKU
Entidad Externa: Cajero / Administrador de Inventario
Flujo de entrada: CONSULTA_PRODUCTO = SKU
Almacenes: PRODUCTOS, PROVEEDORES, OFERTA, CATEGORIA
Requisitos satisfechos: RF3

#NOTE - AC3R: Usuario recibe información del producto
Entidad Externa: Cajero / Administrador de Inventario
Flujo de entrada: DETALLES_PRODUCTO = SKU + [EAN | PLU] + Nombre + Descripcion + PrecioVenta + (PrecioPromocional) + IDCategoria + NombreCategoria + UnidadMedida + PoliticaRotacion + Estado + (Etiquetas) 
Almacenes: PRODUCTOS, PROVEEDORES, OFERTA, CATEGORIA
Requisitos satisfechos: RF3

#NOTE - AC4: Usuario solicita listado completo de productos
Entidad Externa: Cajero / Administrador de Inventario
Flujo de entrada: SOLICITUD_LISTAR_PRODUCTO
Almacenes: PRODUCTOS, PROVEEDORES, OFERTA, CATEGORIA
Requisitos satisfechos: RF4

#NOTE - AC4R: Usuario recibe listado completo de producto
Entidad Externa: Cajero / Administrador de Inventario
Flujo de entrada: LISTA_PRODUCTOS = {DETALLES_PRODUCTO}
Almacenes: PRODUCTOS, PROVEEDORES, OFERTA, CATEGORIA
Requisitos satisfechos: RF4

#NOTE - AC5: Usuario busca productos según criterios
Entidad Externa: Cajero / Administrador de Inventario
Flujo de entrada: CRITERIOS_BUSQUEDA = (Nombre) + (IDCategoria) + (PrecioMin) + (PrecioMax) + (EsGranel) + (Etiquetas)
Almacenes: PRODUCTOS, PROVEEDORES, OFERTA, CATEGORIA
Requisitos satisfechos: RF5

#NOTE - AC5R: Usuario recibe productos según criterios
Entidad Externa: Cajero / Administrador de Inventario
Flujo de entrada: LISTA_PRODUCTOS = {DETALLES_PRODUCTO}
Almacenes: PRODUCTOS, PROVEEDORES, OFERTA, CATEGORIA
Requisitos satisfechos: RF5

#NOTE - AC6: Administrador de inventario descataloga un producto
Entidad Externa: Administrador de Inventario
Flujo de entrada: SOLICITUD_DESCATALOGAR = SKU
Almacenes: PRODUCTOS, INVENTARIO
Requisitos satisfechos: RF6
#NOTE - AC7: Administrador de inventario recataloga un producto
Entidad Externa: Administrador de Inventario
Flujo de entrada: SOLICITUD_RECATALOGAR = SKU
Almacenes: PRODUCTOS
Requisitos satisfechos: RF7
#TODO - AC8: Administrador de inventario elimina definitivamente un producto NO INCLUIR, ESTO ES UN RIESGO PARA LA INTEGRIDAD
Entidad Externa: Administrador de Inventario
Flujo de entrada: SOLICITUD_ELIMINAR_PRODUCTO = SKU
Almacenes: PRODUCTOS, OFERTAS, VENTAS, DEVOLUCIONES, INVENTARIO.
Requisitos satisfechos: RF8
#NOTE - AC9: Administrador de inventario crea una categoría
Entidad Externa: Administrador de Inventario
Flujo de entrada: INFO_NUEVA_CATEGORIA = NombreCategoria + Descripción
Almacenes: CATEGORIAS
Requisitos satisfechos: RF9
#NOTE - AC10: Administrador de inventario modifica una categoría
Entidad Externa: Administrador de Inventario
Flujo de entrada: INFO_MODIFICAR_CATEGORIA = IDCategoria + NuevosValores
Almacenes: CATEGORIAS
Requisitos satisfechos: RF10
#NOTE - AC11: Gerente de tienda crea una oferta para un producto
Entidad Externa: Gerente de Tienda
Flujo de entrada: INFO_NUEVA_OFERTA = SKU + PrecioPromocional + TipoPromocion + FechaInicio + FechaFin
Almacenes: PRODUCTOS, OFERTAS
Requisitos satisfechos: RF11
#NOTE - AC12: Administrador de inventario asigna etiquetas a un producto
Entidad Externa: Administrador de Inventario
Flujo de entrada: ASIGNACION_ETIQUETAS = SKU + Etiquetas
Almacenes: PRODUCTOS
Requisitos satisfechos: RF12
#NOTE - AC13: Usuario solicita traducción entre identificadores
Entidad Externa: Cajero / Administrador de Inventario
Flujo de entrada: SOLICITUD_TRADUCCION = Codigo
Almacenes: PRODUCTOS
Requisitos satisfechos: RF16

#NOTE - AC13R: Usuario obtiene traduccion entre identificadores
Entidad Externa: Cajero / Administrador de Inventario
Flujo de entrada: RESULTADO_TRADUCCION = CodigoEntrada + CodigoSalida
Almacenes: PRODUCTOS
Requisitos satisfechos: RF16
#!SECTION


#SECTION - : INVENTARIO
#NOTE - AC14: Administrador de inventario registra nuevas existencias (incluido a granel)
Entidad Externa: Administrador de Inventario
Flujo de entrada: INFO_NUEVAS_EXISTENCIAS = SKU + [EAN|PLU] + Cantidad + NumeroLote + (FechaCaducidad) + IDProveedor + UnidadMedida
Almacenes: PRODUCTOS (lectura), INVENTARIO, LOTES, PROVEEDORES (lectura), MOVIMIENTOS_INVENTARIO
Requisitos satisfechos: RF17
#TODO - AC15: Administrador de inventario registra existencias de producto a granel BORRADO
Entidad Externa: Administrador de Inventario
Flujo de entrada: INFO_EXISTENCIAS_GRANEL = SKU + PLU + Cantidad + NumeroLote + FechaCaducidad + IDProveedor + UnidadMedida
Almacenes: PRODUCTOS (lectura), INVENTARIO, LOTES, PROVEEDORES (lectura), MOVIMIENTOS_INVENTARIO
Requisitos satisfechos: RF18
#NOTE - AC16: Administrador de inventario ajusta manualmente el inventario
Entidad Externa: Administrador de Inventario
Flujo de entrada: INFO_AJUSTE_INVENTARIO = SKU + CantidadAjuste + TipoAjuste + MotivoDetallado + UbicacionAjuste + (IDLote)
Almacenes: PRODUCTOS (lectura), INVENTARIO, LOTES, MOVIMIENTOS_INVENTARIO
Requisitos satisfechos: RF20
Nota: El ajuste puede aplicarse a almacén o estantería. Si se especifica IDLote, el ajuste se aplica a ese lote específico; si no, se distribuye entre lotes según política FIFO.
#TODO - AC17: Administrador de inventario elimina un lote del inventario  ESTO AFECTA LA INTEGRIDAD
Entidad Externa: Administrador de Inventario
Flujo de entrada: SOLICITUD_ELIMINAR_LOTE = IDLote + MotivoEliminacion
Almacenes: LOTES, INVENTARIO, MOVIMIENTOS_INVENTARIO
Requisitos satisfechos: RF21
#NOTE - AC18: Administrador de inventario mueve stock a estanterías
Entidad Externa: Administrador de Inventario
Flujo de entrada: INFO_MOVER_STOCK = SKU + IDLote + CantidadTransladar + UnidadMedida
Almacenes: PRODUCTOS (lectura para política de rotación), LOTES (lectura), INVENTARIO, MOVIMIENTOS_INVENTARIO
Requisitos satisfechos: RF22
#NOTE - AC19: Administrador de inventario contabiliza stock manualmente
Entidad Externa: Administrador de Inventario
Flujo de entrada: INFO_CONTABILIZACION = SKU + StockFisicoEstanteria + [StockFisicoAlmacenTotal | {LoteID + StockFisicoLote}]
Almacenes: PRODUCTOS (lectura), INVENTARIO, LOTES, MOVIMIENTOS_INVENTARIO
Flujo de salida: RESULTADO_CONTABILIZACION = InventarioActualizado + ReporteDiscrepancias + {MovimientosInventario}
Requisitos satisfechos: RF23
Nota: Permite dos modalidades para almacén:
  1. Contabilización rápida: Solo stock total en almacén (sistema distribuye con FIFO)
  2. Contabilización detallada: Stock físico por cada lote (ajustes precisos)
Para estantería solo se permite stock total (sin trazabilidad por lotes).
#NOTE - AC20: Usuario consulta inventario de un producto
Entidad Externa: Administrador de Inventario / Cajero
Flujo de entrada: CONSULTA_INVENTARIO = SKU
Almacenes: PRODUCTOS (lectura), INVENTARIO (lectura), LOTES (lectura)
Requisitos satisfechos: RF24
#NOTE - AC20R: Usuario obtiene inventario de un producto
Entidad Externa: Administrador de Inventario / Cajero
Flujo de entrada: DETALLES_INVENTARIO++ = SKU + NombreProducto + StockTotalAlmacen + StockTotalEstanteria + UnidadMedida + {DetalleLote}
Almacenes: PRODUCTOS (lectura), INVENTARIO (lectura), LOTES (lectura)
Requisitos satisfechos: RF24

#NOTE - AC43: Usuario agrega un nuevo proveedor
Entidad Externa: Administrador de Inventario
Flujo de entrada: INFO_NUEVO_PROVEEDOR = NombreProveedor + Contacto + Direccion + Telefono + Email
Almacenes: PROVEEDOR
Requisitos satisfechos: RF60
#!SECTION

#SECTION - : DEVOLUCIONES
#NOTE - AC21: Cajero procesa una devolución
Entidad Externa: Cajero
Flujo de entrada: DATOS_DEVOLUCION = NumeroTicket + ListaSKU + ListaCantidades
Almacenes: TICKETS (lectura), PRODUCTOS (lectura para verificar categoría), DEVOLUCIONES
Requisitos satisfechos: RF25, RF26

#!SECTION

#SECTION - : VENTAS
#NOTE - AC23: Cajero inicia un nuevo ticket de venta
Entidad Externa: Cajero
Flujo de entrada: SOLICITUD_NUEVO_TICKET
Almacenes: TICKETS_TEMPORALES
Requisitos satisfechos: RF28
#NOTE - AC24: Cajero añade producto al ticket
Entidad Externa: Cajero
Flujo de entrada: INFO_AÑADIR_PRODUCTO++ = IDTicketTemporal + CodigoBarras
Almacenes: TICKETS_TEMPORALES, PRODUCTOS (lectura), INVENTARIO (lectura para validar stock)
Requisitos satisfechos: RF29, RF33, RF34, RF40

#NOTE - AC24R: Cajero obtiene confirmacion al añadir al ticket
Entidad Externa: Cajero
Flujo de entrada: CONFIRMACION_PRODUCTO_AÑADIDO = IDTicketTemporal + SKU + NombreProducto + Cantidad + PrecioUnitario + Subtotal + SubtotalTicketActual
Almacenes: TICKETS_TEMPORALES, PRODUCTOS (lectura), INVENTARIO (lectura para validar stock)
Requisitos satisfechos: RF29, RF33, RF34, RF40


#NOTE - AC25: Cajero cancela ticket temporal
Entidad Externa: Cajero
Flujo de entrada: SOLICITUD_BORRAR_TICKET = IDTicketTemporal
Almacenes: TICKETS_TEMPORALES
Requisitos satisfechos: RF30
#NOTE - AC26: Cajero elimina producto del ticket temporal
Entidad Externa: Cajero
Flujo de entrada: INFO_ELIMINAR_PRODUCTO_TICKET = IDTicketTemporal + SKU
Almacenes: TICKETS_TEMPORALES
Requisitos satisfechos: RF31
#NOTE - AC27: Usuario consulta contenido de un ticket
Entidad Externa: Cajero / Gerente de Tienda
Flujo de entrada: CONSULTA_TICKET = IDTicket
Almacenes: TICKETS_TEMPORALES (lectura), TICKETS (lectura), PRODUCTOS (lectura)
Requisitos satisfechos: RF32
#NOTE - AC27R: Usuario obtiene contenido de un ticket
Entidad Externa: Cajero / Gerente de Tienda
Flujo de entrada: DETALLES_TICKET++ = IDTicket + [IDTicketTemporal | NumeroTicket] + FechaHora + NombreCajero + {LineaTicket} + Subtotal + TotalImpuestos + Total + (MetodoPago) + Estado
Almacenes: TICKETS_TEMPORALES (lectura), TICKETS (lectura), PRODUCTOS (lectura)
Requisitos satisfechos: RF32

#NOTE - AC28: Cajero procesa el pago de un ticket
Entidad Externa: Cajero
Flujo de entrada: INFO_PAGO = IDTicketTemporal + MetodoPago + MontoRecibido
Almacenes: TICKETS_TEMPORALES, PAGOS
Requisitos satisfechos: RF37

#NOTE - AC29: Cajero cierra un ticket de venta
Entidad Externa: Cajero
Flujo de entrada: SOLICITUD_CERRAR_TICKET = IDTicketTemporal
Almacenes: TICKETS_TEMPORALES (lectura), TICKETS, PRODUCTOS (lectura para precios/impuestos), INVENTARIO, LOTES, PAGOS (lectura), MOVIMIENTOS_INVENTARIO
Requisitos satisfechos: RF35, RF36, RF38, RF39 (aplicación automática de promociones)
#!SECTION

#SECTION - : AUTENTICACIÓN Y AUTORIZACIÓN
#NOTE - AC30: Usuario inicia sesión en el sistema
Entidad Externa: Cajero / Administrador de Inventario / Gerente de Tienda / Administrador del Sistema
Flujo de entrada: CREDENCIALES_LOGIN = NombreUsuario + Contraseña
Almacenes: USUARIOS (lectura), ROLES (lectura), SESIONES
Requisitos satisfechos: RF41, RF42

#NOTE - AC30R: Usuario obtiene token del sistema
Entidad Externa: Cajero / Administrador de Inventario / Gerente de Tienda / Administrador del Sistema
Flujo de entrada: TOKEN_AUTENTICACION = TokenJWT + IDUsuario + NombreUsuario + {NombreRol} + FechaExpiracion + Mensaje
Almacenes: USUARIOS (lectura), ROLES (lectura), SESIONES
Requisitos satisfechos: RF41, RF42

#NOTE - AC31: Usuario cierra sesión
Entidad Externa: Cajero / Administrador de Inventario / Gerente de Tienda / Administrador del Sistema
Flujo de entrada: SOLICITUD_LOGOUT = TokenJWT
Almacenes: SESIONES
Requisitos satisfechos: RF43
#NOTE - AC32: Administrador del sistema crea una cuenta de usuario
Entidad Externa: Administrador del Sistema
Flujo de entrada: INFO_NUEVO_USUARIO = NombreUsuario + ContraseñaTemporal + ListaRoles
Almacenes: USUARIOS, ROLES (lectura)
Requisitos satisfechos: RF45, RF48
#NOTE - AC33: Administrador del sistema modifica un usuario existente
Entidad Externa: Administrador del Sistema
Flujo de entrada: INFO_MODIFICAR_USUARIO = IDUsuario + (NombreUsuario) + (NombreCompleto) + ({IDRol})
Almacenes: USUARIOS, ROLES (lectura)
Requisitos satisfechos: RF46
#NOTE - AC34: Administrador del sistema desactiva una cuenta de usuario
Entidad Externa: Administrador del Sistema
Flujo de entrada: SOLICITUD_DESACTIVAR_USUARIO = IDUsuario
Almacenes: USUARIOS
Requisitos satisfechos: RF47
#NOTE - AC35: Administrador del sistema gestiona roles
Entidad Externa: Administrador del Sistema
Flujo de entrada: INFO_GESTION_ROL = Accion + NombreRol + DescripcionRol
Almacenes: ROLES
Requisitos satisfechos: RF49
#NOTE - AC36: Administrador del sistema asigna permisos a un rol
Entidad Externa: Administrador del Sistema
Flujo de entrada: INFO_ASIGNAR_PERMISOS = IDRol + ListaPermisos
Almacenes: ROLES, PERMISOS
Requisitos satisfechos: RF50, RF51
#NOTE - AC37: Usuario cambia su propia contraseña
Entidad Externa: Cajero / Administrador de Inventario / Gerente de Tienda / Administrador del Sistema
Flujo de entrada: CAMBIO_CONTRASEÑA = ContraseñaActual + ContraseñaNueva
Almacenes: USUARIOS
Requisitos satisfechos: RF52
#NOTE - AC38: Administrador del sistema restablece una contraseña olvidada
Entidad Externa: Administrador del Sistema
Flujo de entrada: SOLICITUD_RESTABLECER_CONTRASEÑA = IDUsuario
Almacenes: USUARIOS
Requisitos satisfechos: RF53

#NOTE - AC38R: Administrador del sistema obtiene una contraseña temporal
Entidad Externa: Administrador del Sistema
Flujo de entrada: CONFIRMACION_CONTRASEÑA_RESTABLECIDA++ = IDUsuario + ContraseñaTemporal + Mensaje
Almacenes: USUARIOS
Requisitos satisfechos: RF53
#!SECTION

#SECTION - : MONITOREO Y REPORTES
#NOTE - AC39: Gerente de tienda solicita reporte del catálogo
Entidad Externa: Gerente de Tienda
Flujo de entrada: SOLICITUD_REPORTE_CATALOGO = TipoReporte + FiltrosOpcionales
Almacenes: PRODUCTOS (lectura), CATEGORIAS (lectura), OFERTAS (lectura)
Requisitos satisfechos: RF54

#NOTE - AC39R: Gerente de tienda obtiene reporte del catálogo
Entidad Externa: Gerente de Tienda
Flujo de entrada: REPORTE_CATALOGO++ = TipoReporte + FechaGeneracion + {DetalleReporte} + (Estadisticas)
Almacenes: PRODUCTOS (lectura), CATEGORIAS (lectura), OFERTAS (lectura)
Requisitos satisfechos: RF54

#NOTE - AC40: Sistema genera alertas por stock mínimo (TEMPORAL)
Entidad Externa: (Ninguna - acontecimiento temporal)
Flujo de entrada: (Se dispara automáticamente según configuración)
Almacenes: PRODUCTOS (lectura), INVENTARIO (lectura), ALERTAS
Requisitos satisfechos: RF55
#NOTE - AC41: Sistema genera reporte de productos próximos a caducar (TEMPORAL)
Entidad Externa: (Ninguna - acontecimiento temporal)
Flujo de entrada: (Se dispara diariamente)
Almacenes: LOTES (lectura), PRODUCTOS (lectura), INVENTARIO (lectura), REPORTES_CADUCIDAD
Requisitos satisfechos: RF56
#NOTE - AC42: Gerente de tienda solicita reporte de ventas
Entidad Externa: Gerente de Tienda
Flujo de entrada: SOLICITUD_REPORTE_VENTAS = FechaInicio + FechaFin
Almacenes: TICKETS (lectura), PRODUCTOS (lectura)
Requisitos satisfechos: RF57
#NOTE - AC44: Administrador de sistemas monitorea el sistema
Entidad Externa: Administrador de sistema
Flujo de entrada: (Ninguna - acontecimiento temporal)
Almacenes: ALERTAS
Requisitos satisfechos: RF59
#NOTE - AC22: Gerente de tienda solicita reporte de devoluciones
Entidad Externa: Gerente de Tienda
Flujo de entrada: SOLICITUD_REPORTE_DEVOLUCIONES = FechaInicio + FechaFin + (FiltroProducto) + (FiltroCategoria) + (FiltroCajero)
Almacenes: DEVOLUCIONES (lectura), PRODUCTOS (lectura), CATEGORIAS (lectura), USUARIOS (lectura)
Requisitos satisfechos: RF27, RF58


#!SECTION



