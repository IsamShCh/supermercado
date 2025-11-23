# DICCIONARIO DE DATOS - SISTEMA DE GESTIÓN DE SUPERMERCADOS

----------

## SECCIÓN 1: ALMACENES

### PRODUCTOS

```
PRODUCTOS = {@SKU + [EAN | PLU] + Nombre + Descripcion + PrecioVenta + 
             OfertaAplicada + (FechaInicioOferta) + (FechaFinOferta) + 
             Caduca + EsGranel + IDCategoria + PoliticaRotacion + 
             UnidadMedida + (Etiquetas) + Estado}

@SKU = *Código único del productoEntity según formato empresa*
EAN = *Código de barras estándar internacional de 13 dígitos*
PLU = *Código de 4-5 dígitos para productos a granel*
Nombre = *Nombre comercial del productoEntity*
Descripcion = *Descripción detallada del productoEntity*
PrecioVenta = *Precio en formato decimal con 2 decimales, >= 0*
OfertaAplicada = *Booleano que indica si hay oferta activa*
FechaInicioOferta = *Fecha de inicio de la oferta en formato YYYY-MM-DD*
FechaFinOferta = *Fecha de fin de la oferta en formato YYYY-MM-DD*
Caduca = *Booleano que indica si el productoEntity tiene fecha de caducidad*
EsGranel = *Booleano que indica si se vende por peso*
IDCategoria = *Identificador de la categoría del productoEntity*
PoliticaRotacion = [FIFO | FEFO | LIFO]
UnidadMedida = *Unidad de medida del catálogo predefinido*
Etiquetas = *Cadena de texto con etiquetas concatenadas*
Estado = [Activo | Descatalogado]

```

### CATEGORIAS

```
CATEGORIAS = {@IDCategoria + NombreCategoria + Descripcion}

@IDCategoria = *Identificador único de la categoría*
NombreCategoria = *Nombre de la categoría*
Descripcion = *Descripción de la categoría*

```

### PROVEEDORES

```
PROVEEDORES = {@IDProveedor + NombreProveedor + Contacto + Direccion + 
               Telefono + Email}

@IDProveedor = *Identificador único del proveedor*
NombreProveedor = *Nombre comercial del proveedor*
Contacto = *Persona de contacto*
Direccion = *Dirección física del proveedor*
Telefono = *Número de teléfono*
Email = *Correo electrónico*

```

### OFERTAS

```
OFERTAS = {@IDOferta + SKU + PrecioPromocional + TipoPromocion + 
           FechaInicio + FechaFin + Estado}

@IDOferta = *Identificador único de la oferta*
SKU = *SKU del productoEntity en oferta*
PrecioPromocional = *Precio con descuento, formato decimal 2 decimales*
TipoPromocion = *Tipo de promoción aplicada*
FechaInicio = *Fecha inicio validez en formato YYYY-MM-DD*
FechaFin = *Fecha fin validez en formato YYYY-MM-DD*
Estado = [Activa | Vencida | Cancelada]

```

### INVENTARIO

```
INVENTARIO = {@IDInventario + SKU + [EAN | PLU] + CantidadAlmacen +
              CantidadEstanteria + UnidadMedida}

@IDInventario = *Identificador único del registro de inventario*
SKU = *SKU del productoEntity*
EAN = *Código EAN del productoEntity*
PLU = *Código PLU del productoEntity a granel*
CantidadAlmacen = *Cantidad disponible en almacén*
CantidadEstanteria = *Cantidad disponible en estantería*
UnidadMedida = *Unidad de medida correspondiente*

```

### LOTES

```
LOTES = {@IDLote + SKU + IDInventario + NumeroLote + Cantidad +
         (FechaCaducidad) + IDProveedor + FechaIngreso + UnidadMedida +
         Estado}

@IDLote = *Identificador único del lote*
IDInventario = *Identificador del inventario*
SKU = *SKU del productoEntity*
NumeroLote = *Número de lote del proveedor*
Cantidad = *Cantidad total ingresada*
FechaCaducidad = *Fecha de caducidad en formato YYYY-MM-DD*
IDProveedor = *Identificador del proveedor*
FechaIngreso = *Fecha de ingreso al sistema en formato YYYY-MM-DD*
UnidadMedida = *Unidad de medida del lote*
Estado = [Disponible | Bloqueado | Eliminado]
```

### MOVIMIENTOS_INVENTARIO 
``` 
MOVIMIENTOS_INVENTARIO = {@IDMovimiento + SKU + (IDLote) + TipoMovimiento + Cantidad + UnidadMedida + FechaHora + IDUsuario + Motivo + (Observaciones)} 

@IDMovimiento = *Identificador único del movimiento* 
SKU = *SKU del productoEntity* 
IDLote = *Identificador del lote afectado* 
TipoMovimiento = [Entrada | Salida | Ajuste | Traslado | Venta | Devolucion | Merma] 
Cantidad = *Cantidad del movimiento (positiva o negativa)* 
UnidadMedida = *Unidad de medida del movimiento* 
FechaHora = *Timestamp del movimiento* 
IDUsuario = *Usuario que realizó el movimiento* 
Motivo = *Motivo del movimiento* 
Observaciones = *Comentarios adicionales* 
```

### TICKETS_TEMPORALES

```
TICKETS_TEMPORALES = {@IDTicketTemporal + IDUsuario + FechaHoraCreacion + 
                      {LineaTicket} + Estado}

@IDTicketTemporal = *Identificador único del ticket temporal*
IDUsuario = *Cajero que creó el ticket*
FechaHoraCreacion = *Timestamp de creación*
LineaTicket = NumeroLinea + SKU + [CodigoBarras | CodigoBarrasGranel] + 
              Cantidad + PrecioUnitario + Subtotal
Estado = [Activo | Cancelado]

```

### TICKETS

```
TICKETS = {@NumeroTicket + IDUsuario + FechaHora + {LineaVenta} + 
           Subtotal + TotalImpuestos + Total + IDPago}

@NumeroTicket = *Número único del ticket cerrado*
IDUsuario = *Cajero que procesó la venta*
FechaHora = *Timestamp de cierre del ticket*
LineaVenta = NumeroLinea + SKU + Descripcion + Cantidad + PrecioUnitario + 
             (Descuento) + Subtotal + Impuesto
Subtotal = *Suma de precios sin impuestos*
TotalImpuestos = *Total de impuestos aplicados*
Total = *Monto total de la venta*
IDPago = *Referencia al pago asociado*

```

### PAGOS

```
PAGOS = {@IDPago + NumeroTicket + MetodoPago + MontoRecibido + 
         MontoCambio + FechaHora}

@IDPago = *Identificador único del pago*
NumeroTicket = *Ticket asociado al pago*
MetodoPago = [Efectivo | TarjetaDebito | TarjetaCredito | Transferencia]
MontoRecibido = *Cantidad recibida del cliente*
MontoCambio = *Cambio devuelto al cliente*
FechaHora = *Timestamp del pago*

```

### DEVOLUCIONES

```
DEVOLUCIONES = {@IDDevolucion + NumeroTicket + FechaDevolucion + 
                {ProductoDevuelto} + MontoReembolsado + IDUsuario}

@IDDevolucion = *Identificador único de la devolución*
NumeroTicket = *Ticket original de la compra*
FechaDevolucion = *Fecha de procesamiento de la devolución*
ProductoDevuelto = SKU + Cantidad + PrecioOriginal + Subtotal
MontoReembolsado = *Total reembolsado al cliente*
IDUsuario = *Cajero que procesó la devolución*

```

### USUARIOS

```
USUARIOS = {@IDUsuario + NombreUsuario + HashContraseña + Salt + 
            NombreCompleto + {IDRol} + Estado + FechaCreacion + 
            (FechaUltimoAcceso) + RequiereCambioContraseña}

@IDUsuario = *Identificador único del usuario*
NombreUsuario = *Nombre de usuario para login*
HashContraseña = *Hash bcrypt de la contraseña*
Salt = *Salt utilizado en el hash*
NombreCompleto = *Nombre completo del usuario*
IDRol = *Identificador del rol asignado*
Estado = [Activo | Inactivo]
FechaCreacion = *Fecha de creación de la cuenta*
FechaUltimoAcceso = *Timestamp del último acceso*
RequiereCambioContraseña = *Booleano para forzar cambio de contraseña*

```

### ROLES

```
ROLES = {@IDRol + NombreRol + DescripcionRol + {IDPermiso}}

@IDRol = *Identificador único del rol*
NombreRol = *Nombre del rol*
DescripcionRol = *Descripción del rol*
IDPermiso = *Identificador del permiso asociado*

```

### PERMISOS

```
PERMISOS = {@IDPermiso + NombrePermiso + Descripcion + Recurso + Accion}

@IDPermiso = *Identificador único del permiso*
NombrePermiso = *Nombre descriptivo del permiso*
Descripcion = *Descripción del permiso*
Recurso = *Recurso sobre el que aplica el permiso*
Accion = [Crear | Leer | Actualizar | Eliminar | Ejecutar]

```

### SESIONES

```
SESIONES = {@TokenJWT + IDUsuario + FechaHoraInicio + (FechaHoraFin) + 
            Estado}

@TokenJWT = *Token JWT único de la sesión*
IDUsuario = *Usuario de la sesión*
FechaHoraInicio = *Timestamp de inicio de sesión*
FechaHoraFin = *Timestamp de cierre de sesión*
Estado = [Activa | Cerrada | Expirada]

```

### ALERTAS

```
ALERTAS = {@IDAlerta + TipoAlerta + SKU + Mensaje + FechaHoraGeneracion + 
           Estado + (FechaHoraAtendida) + (IDUsuarioAtendio)}

@IDAlerta = *Identificador único de la alerta*
TipoAlerta = [StockMinimo | Caducidad | Sistema]
SKU = *Producto relacionado con la alerta*
Mensaje = *Mensaje descriptivo de la alerta*
FechaHoraGeneracion = *Timestamp de generación*
Estado = [Pendiente | Atendida | Ignorada]
FechaHoraAtendida = *Timestamp de atención*
IDUsuarioAtendio = *Usuario que atendió la alerta*

```


----------

## SECCIÓN 2: FLUJOS DE DATOS

### FLUJOS DE ENTRADA

#### INFO_NUEVO_PRODUCTO

```
INFO_NUEVO_PRODUCTO = SKU + [EAN | PLU] + Nombre + Descripcion + 
                      PrecioVenta + (OfertaAplicada) + (FechaInicioOferta) + 
                      (FechaFinOferta) + Caduca + EsGranel + IDCategoria + 
                      PoliticaRotacion + IDProveedor + UnidadMedida

**Usado en AC1**

```

#### INFO_NUEVO_PROVEEDOR

```
INFO_NUEVO_PROVEEDOR = NombreProveedor + Contacto +
						 Direccion + Telefono + Email

**Usado en AC43**

```

#### INFO_MODIFICAR_PRODUCTO

```
INFO_MODIFICAR_PRODUCTO = SKU + DatosActualizar

DatosActualizar = (Nombre) + (Descripcion) + (PrecioVenta) + (IDCategoria) + 
                  (OfertaAplicada) + (FechasOferta) + (PoliticaRotacion) + 
                  (IDProveedor) + (Etiquetas)

**Usado en AC2**

```

#### CONSULTA_PRODUCTO

```
CONSULTA_PRODUCTO = SKU

**Usado en AC3**

```

#### SOLICITUD_LISTAR_PRODUCTOS

```
SOLICITUD_LISTAR_PRODUCTOS = *Flujo vacío*

**Usado en AC4**

```

#### CRITERIOS_BUSQUEDA

```
CRITERIOS_BUSQUEDA = (Nombre) + (IDCategoria) + (PrecioMin) + (PrecioMax) + 
                     (EsGranel) + (Etiquetas)

**Usado en AC5**
**Al menos uno de los criterios debe estar presente**

```

#### SOLICITUD_DESCATALOGAR

```
SOLICITUD_DESCATALOGAR = SKU

**Usado en AC6**

```

#### SOLICITUD_RECATALOGAR

```
SOLICITUD_RECATALOGAR = SKU

**Usado en AC7**

```

#### SOLICITUD_ELIMINAR_PRODUCTO

```
SOLICITUD_ELIMINAR_PRODUCTO = SKU

**Usado en AC8**

```

#### INFO_NUEVA_CATEGORIA

```
INFO_NUEVA_CATEGORIA = NombreCategoria + Descripcion

**Usado en AC9**

```

#### INFO_MODIFICAR_CATEGORIA

```
INFO_MODIFICAR_CATEGORIA = IDCategoria + (NombreCategoria) + (Descripcion)

**Usado en AC10**

```

#### INFO_NUEVA_OFERTA

```
INFO_NUEVA_OFERTA = SKU + PrecioPromocional + TipoPromocion + FechaInicio + 
                    FechaFin

**Usado en AC11**

```

#### ASIGNACION_ETIQUETAS

```
ASIGNACION_ETIQUETAS = SKU + Etiquetas

Etiquetas = *Cadena de texto con etiquetas separadas por delimitador*

**Usado en AC12**

```

#### SOLICITUD_TRADUCCION

```
SOLICITUD_TRADUCCION = Codigo

Codigo = [SKU | EAN | PLU]

**Usado en AC13**

```

#### INFO_NUEVAS_EXISTENCIAS

```
INFO_NUEVAS_EXISTENCIAS = SKU + Cantidad + NumeroLote +
                          (FechaCaducidad) + IDProveedor + UnidadMedida

**Usado en AC14**
**Nota: EAN/PLU se obtienen del inventario asociado al SKU**

```

#### INFO_AJUSTE_INVENTARIO

```
INFO_AJUSTE_INVENTARIO = SKU + CantidadAjuste + TipoAjuste + MotivoDetallado +
                         UbicacionAjuste

TipoAjuste = [Merma | Robo | Caducado | ErrorConteo | ProductoEncontrado]
CantidadAjuste = *Puede ser positiva o negativa*
MotivoDetallado = *Descripción detallada del ajuste*
UbicacionAjuste = [AjustarAlmacen | AjustarEstanteria]

AjustarAlmacen = (IDLote)
AjustarEstanteria = (IDLote)
**Nota: Si se especifica IDLote, el ajuste se aplica solo a ese lote.
        Si no se especifica, se distribuye entre lotes según política FIFO**

**Usado en AC16**

```

#### SOLICITUD_ELIMINAR_LOTE

```
SOLICITUD_ELIMINAR_LOTE = IDLote + MotivoEliminacion

**Usado en AC17**

```

#### INFO_MOVER_STOCK

```
INFO_MOVER_STOCK = SKU + IDLote + CantidadTransladar + UnidadMedida

**Usado en AC18**

```

#### INFO_CONTABILIZACION

```
INFO_CONTABILIZACION = SKU + StockFisicoEstanteria + StockFisicoAlmacen

**Usado en AC19**

```

#### CONSULTA_INVENTARIO

```
CONSULTA_INVENTARIO = SKU

**Usado en AC20**

```

#### DATOS_DEVOLUCION

```
DATOS_DEVOLUCION = NumeroTicket + {ProductoDevolver}

ProductoDevolver = SKU + Cantidad

**Usado en AC21**

```

#### SOLICITUD_REPORTE_DEVOLUCIONES

```
SOLICITUD_REPORTE_DEVOLUCIONES = FechaInicio + FechaFin + (FiltroProducto) + 
                                 (FiltroCategoria) + (FiltroCajero)

**Usado en AC22**

```

#### SOLICITUD_NUEVO_TICKET

```
SOLICITUD_NUEVO_TICKET = *Flujo vacío*

**Usado en AC23**

```

#### INFO_AÑADIR_PRODUCTO

```
INFO_AÑADIR_PRODUCTO = IDTicketTemporal + CodigoBarras

CodigoBarras = [EAN | PLU | CodigoBarrasGranel]
CodigoBarrasGranel = *Código de barras especial que contiene SKU y peso*

**Usado en AC24**

```

#### SOLICITUD_BORRAR_TICKET

```
SOLICITUD_BORRAR_TICKET = IDTicketTemporal

**Usado en AC25**

```

#### INFO_ELIMINAR_PRODUCTO_TICKET

```
INFO_ELIMINAR_PRODUCTO_TICKET = IDTicketTemporal + SKU

**Usado en AC26**

```

#### CONSULTA_TICKET

```
CONSULTA_TICKET = IDTicket

IDTicket = [IDTicketTemporal | NumeroTicket]

**Usado en AC27**

```

#### INFO_PAGO

```
INFO_PAGO = IDTicketTemporal + MetodoPago + MontoRecibido

MetodoPago = [Efectivo | TarjetaDebito | TarjetaCredito | Transferencia]

**Usado en AC28**

```

#### SOLICITUD_CERRAR_TICKET

```
SOLICITUD_CERRAR_TICKET = IDTicketTemporal

**Usado en AC29**

```

#### CREDENCIALES_LOGIN

```
CREDENCIALES_LOGIN = NombreUsuario + Contraseña

Contraseña = *Contraseña en texto plano (se hasheará en el sistema)*

**Usado en AC30**

```

#### SOLICITUD_LOGOUT

```
SOLICITUD_LOGOUT = TokenJWT

**Usado en AC31**

```

#### INFO_NUEVO_USUARIO

```
INFO_NUEVO_USUARIO = NombreUsuario + ContraseñaTemporal + NombreCompleto + 
                     {IDRol}

**Usado en AC32**

```

#### INFO_MODIFICAR_USUARIO

```
INFO_MODIFICAR_USUARIO = IDUsuario + (NombreUsuario) + (NombreCompleto) + 
                         ({IDRol})

**Usado en AC33**

```

#### SOLICITUD_DESACTIVAR_USUARIO

```
SOLICITUD_DESACTIVAR_USUARIO = IDUsuario

**Usado en AC34**

```

#### INFO_GESTION_ROL

```
INFO_GESTION_ROL = Accion + (IDRol) + NombreRol + (DescripcionRol)

Accion = [Crear | Modificar | Eliminar]

**Usado en AC35**

```

#### INFO_ASIGNAR_PERMISOS

```
INFO_ASIGNAR_PERMISOS = IDRol + {IDPermiso}

**Usado en AC36**

```

#### CAMBIO_CONTRASEÑA

```
CAMBIO_CONTRASEÑA = ContraseñaActual + ContraseñaNueva

**Usado en AC37**

```

#### SOLICITUD_RESTABLECER_CONTRASEÑA

```
SOLICITUD_RESTABLECER_CONTRASEÑA = IDUsuario

**Usado en AC38**

```

#### SOLICITUD_REPORTE_CATALOGO

```
SOLICITUD_REPORTE_CATALOGO = TipoReporte + (Filtros)

TipoReporte = [ListaPorCategoria | ProductosConOfertas | EstadisticasGenerales]
Filtros = (IDCategoria) + (Estado) + (EsGranel)

**Usado en AC39**

```

#### SOLICITUD_REPORTE_VENTAS

```
SOLICITUD_REPORTE_VENTAS = FechaInicio + FechaFin

**Usado en AC42**

```

----------

### FLUJOS DE SALIDA

#### CONFIRMACION_PRODUCTO_CREADO

```
CONFIRMACION_PRODUCTO_CREADO = SKU + Mensaje + DetallesProducto

DetallesProducto = Nombre + PrecioVenta + IDCategoria + UnidadMedida

**Salida de AC1**

```
#### CONFIRMACION_PROVEEDOR_CREADO

```
CONFIRMACION_PROVEEDOR_CREADO = Mensaje + DetallesProveedor

DetallesProveedor = @IDProveedor + NombreProveedor + Contacto + Direccion + 
					Telefono + Email
 
**Salida de AC43**

```

#### CONFIRMACION_PRODUCTO_MODIFICADO

```
CONFIRMACION_PRODUCTO_MODIFICADO = SKU + Mensaje + DetallesActualizados

**Salida de AC2**

```

#### DETALLES_PRODUCTO

```
DETALLES_PRODUCTO = SKU + [EAN | PLU] + Nombre + Descripcion + PrecioVenta + 
                    (PrecioPromocional) + IDCategoria + NombreCategoria + 
                    UnidadMedida + PoliticaRotacion + Estado + (Etiquetas)

**Salida de AC3, AC13**

```

#### LISTA_PRODUCTOS

```
LISTA_PRODUCTOS = {DETALLES_PRODUCTO}

**Salida de AC4, AC5**

```

#### CONFIRMACION_DESCATALOGADO

```
CONFIRMACION_DESCATALOGADO = SKU + Mensaje

**Salida de AC6**

```

#### CONFIRMACION_RECATALOGADO

```
CONFIRMACION_RECATALOGADO = SKU + Mensaje

**Salida de AC7**

```

#### CONFIRMACION_ELIMINACION

```
CONFIRMACION_ELIMINACION = SKU + Mensaje

**Salida de AC8**

```

#### CONFIRMACION_CATEGORIA_CREADA

```
CONFIRMACION_CATEGORIA_CREADA = IDCategoria + NombreCategoria + Mensaje

**Salida de AC9**

```

#### CONFIRMACION_CATEGORIA_MODIFICADA

```
CONFIRMACION_CATEGORIA_MODIFICADA = IDCategoria + Mensaje

**Salida de AC10**

```

#### CONFIRMACION_OFERTA_CREADA

```
CONFIRMACION_OFERTA_CREADA = IDOferta + SKU + PrecioPromocional + 
                             FechaInicio + FechaFin + Mensaje

**Salida de AC11**

```

#### CONFIRMACION_ETIQUETAS_ASIGNADAS

```
CONFIRMACION_ETIQUETAS_ASIGNADAS = SKU + Etiquetas + Mensaje

**Salida de AC12**

```

#### RESULTADO_TRADUCCION

```
RESULTADO_TRADUCCION = CodigoEntrada + CodigoSalida

CodigoEntrada = [SKU | EAN | PLU]
CodigoSalida = [SKU | EAN | PLU]

**Salida de AC13**

```

#### CONFIRMACION_EXISTENCIAS_AGREGADAS

```
CONFIRMACION_EXISTENCIAS_AGREGADAS = IDLote + SKU + Cantidad + StockTotal + 
                                     Mensaje

**Salida de AC14, AC15**

```

#### CONFIRMACION_AJUSTE_INVENTARIO

```
CONFIRMACION_AJUSTE_INVENTARIO = SKU + CantidadAjustada + StockActual + 
                                 Mensaje

**Salida de AC16**

```

#### CONFIRMACION_LOTE_ELIMINADO

```
CONFIRMACION_LOTE_ELIMINADO = IDLote + Mensaje

**Salida de AC17**

```

#### CONFIRMACION_STOCK_MOVIDO

```
CONFIRMACION_STOCK_MOVIDO = SKU + IDLote + CantidadMovida + 
                            StockAlmacenActual + StockEstanteriaActual + 
                            Mensaje

**Salida de AC18**

```

#### REPORTE_CONTABILIZACION

```
REPORTE_CONTABILIZACION = SKU + StockLogicoEstanteria + 
                          StockFisicoEstanteria + DiscrepanciaEstanteria + 
                          StockLogicoAlmacen + StockFisicoAlmacen + 
                          DiscrepanciaAlmacen + {AjusteRealizado}

AjusteRealizado = IDLote + CantidadAjustada + Motivo

**Salida de AC19**

```

#### DETALLES_INVENTARIO

```
DETALLES_INVENTARIO = SKU + NombreProducto + StockTotalAlmacen + 
                      StockTotalEstanteria + UnidadMedida + {DetalleLote}

DetalleLote = IDLote + NumeroLote + CantidadAlmacen + CantidadEstanteria + 
              (FechaCaducidad) + FechaIngreso

**Salida de AC20**

```

#### CONFIRMACION_DEVOLUCION

```
CONFIRMACION_DEVOLUCION = IDDevolucion + NumeroTicket + {ProductoDevuelto} + 
                          MontoReembolsado + Mensaje

ProductoDevuelto = SKU + NombreProducto + Cantidad + PrecioOriginal

**Salida de AC21**

```

#### REPORTE_DEVOLUCIONES

```
REPORTE_DEVOLUCIONES = FechaInicio + FechaFin + {DetalleDevoluciones} + 
                       ResumenGeneral

DetalleDevoluciones = SKU + NombreProducto + NombreCategoria + 
                      CantidadTotalDevuelta + MontoTotalReembolsado
                      
ResumenGeneral = TotalProductosDevueltos + MontoTotalReembolsadoPeriodo + 
                 NumeroTransaccionesDevolucion

**Salida de AC22**

```

#### CONFIRMACION_TICKET_CREADO

```
CONFIRMACION_TICKET_CREADO = IDTicketTemporal + FechaHoraCreacion + 
                             NombreCajero

**Salida de AC23**

```

#### CONFIRMACION_PRODUCTO_AÑADIDO

```
CONFIRMACION_PRODUCTO_AÑADIDO = IDTicketTemporal + SKU + NombreProducto + 
                                Cantidad + PrecioUnitario + Subtotal + 
                                SubtotalTicketActual

**Salida de AC24**

```

#### CONFIRMACION_TICKET_BORRADO

```
CONFIRMACION_TICKET_BORRADO = IDTicketTemporal + Mensaje

**Salida de AC25**

```

#### CONFIRMACION_PRODUCTO_ELIMINADO_TICKET

```
CONFIRMACION_PRODUCTO_ELIMINADO_TICKET = IDTicketTemporal + SKU + 
                                         SubtotalTicketActual + Mensaje

**Salida de AC26**

```

#### DETALLES_TICKET

```
DETALLES_TICKET = IDTicket + [IDTicketTemporal | NumeroTicket] + 
                  FechaHora + NombreCajero + {LineaTicket} + Subtotal + 
                  TotalImpuestos + Total + (MetodoPago) + Estado

LineaTicket = NumeroLinea + SKU + NombreProducto + Cantidad + PrecioUnitario + 
              (Descuento) + Subtotal

Estado = [Temporal | Cerrado | Cancelado]

**Salida de AC27**

```

#### CONFIRMACION_PAGO_PROCESADO

```
CONFIRMACION_PAGO_PROCESADO = IDPago + IDTicketTemporal + MetodoPago + 
                              MontoRecibido + MontoCambio + Mensaje

**Salida de AC28**

```

#### TICKET_CERRADO

```
TICKET_CERRADO = NumeroTicket + FechaHora + NombreCajero + {LineaVenta} + 
                 Subtotal + TotalImpuestos + Total + MetodoPago + 
                 MontoRecibido + MontoCambio

LineaVenta = NumeroLinea + SKU + NombreProducto + Cantidad + PrecioUnitario + 
             (Descuento) + (PromocionAplicada) + Subtotal + Impuesto

**Salida de AC29**

```

#### TOKEN_AUTENTICACION

```
TOKEN_AUTENTICACION = TokenJWT + IDUsuario + NombreUsuario + {NombreRol} + 
                      FechaExpiracion + Mensaje

**Salida de AC30**

```

#### CONFIRMACION_LOGOUT

```
CONFIRMACION_LOGOUT = Mensaje

**Salida de AC31**

```

#### CONFIRMACION_USUARIO_CREADO

```
CONFIRMACION_USUARIO_CREADO = IDUsuario + NombreUsuario + {NombreRol} + 
                              Mensaje

**Salida de AC32**

```

#### CONFIRMACION_USUARIO_MODIFICADO

```
CONFIRMACION_USUARIO_MODIFICADO = IDUsuario + NombreUsuario + Mensaje

**Salida de AC33**

```

#### CONFIRMACION_USUARIO_DESACTIVADO

```
CONFIRMACION_USUARIO_DESACTIVADO = IDUsuario + NombreUsuario + Mensaje

**Salida de AC34**

```

#### CONFIRMACION_ROL_GESTIONADO

```
CONFIRMACION_ROL_GESTIONADO = (IDRol) + NombreRol + Accion + Mensaje

Accion = [Creado | Modificado | Eliminado]

**Salida de AC35**

```

#### CONFIRMACION_PERMISOS_ASIGNADOS

```
CONFIRMACION_PERMISOS_ASIGNADOS = IDRol + NombreRol + {NombrePermiso} + 
                                  Mensaje

**Salida de AC36**

```

#### CONFIRMACION_CONTRASEÑA_CAMBIADA

```
CONFIRMACION_CONTRASEÑA_CAMBIADA = IDUsuario + Mensaje

**Salida de AC37**

```

#### CONFIRMACION_CONTRASEÑA_RESTABLECIDA

```
CONFIRMACION_CONTRASEÑA_RESTABLECIDA = IDUsuario + ContraseñaTemporal + 
                                       Mensaje

**Salida de AC38**

```

#### REPORTE_CATALOGO
```
REPORTE_CATALOGO = TipoReporte + FechaGeneracion + {DetalleReporte} + 
                   (Estadisticas)

DetalleReporte = SKU + Nombre + PrecioVenta + (PrecioPromocional) + 
                 NombreCategoria + Estado + UnidadMedida

Estadisticas = TotalProductos + ProductosActivos + ProductosDescatalogados + 
               ProductosConOferta + (PromedioPrecios)

**Salida de AC39**

```

#### ALERTA_STOCK_MINIMO

```
ALERTA_STOCK_MINIMO = {DetalleAlerta}

DetalleAlerta = IDAlerta + SKU + NombreProducto + StockActual + UmbralMinimo + 
                DiferenciaStock + FechaHoraGeneracion

**Salida de AC40 (temporal)**

```

#### REPORTE_CADUCIDAD

```
REPORTE_CADUCIDAD = TipoReporte + FechaGeneracion + {ProductoCaducidad}

TipoReporte = [Proximos7Dias | Proximos3Dias | Vencidos]

ProductoCaducidad = SKU + NombreProducto + IDLote + NumeroLote + 
                    FechaCaducidad + CantidadAlmacen + CantidadEstanteria + 
                    DiasRestantes

DiasRestantes = *Número de días hasta caducidad (negativo si vencido)*

**Salida de AC41 (temporal)**

```

#### REPORTE_VENTAS

```
REPORTE_VENTAS = FechaInicio + FechaFin + {DetalleVentas} + ResumenPeriodo

DetalleVentas = Fecha + NumeroTicket + NombreCajero + Total + MetodoPago

ResumenPeriodo = TotalTickets + MontoTotalVentas + VentaPromedio + 
                 VentasPorMetodoPago + ProductosMasVendidos

VentasPorMetodoPago = MetodoPago + CantidadTransacciones + MontoTotal

ProductosMasVendidos = SKU + NombreProducto + CantidadVendida + MontoTotal

**Salida de AC42**

```

----------

## SECCIÓN 3: MENSAJES DE ERROR

#### ERROR_PRODUCTO_NO_ENCONTRADO

```
ERROR_PRODUCTO_NO_ENCONTRADO = CodigoBuscado + Mensaje

Mensaje = *"Producto no encontrado en el catálogo"*

```

#### ERROR_SKU_DUPLICADO

```
ERROR_SKU_DUPLICADO = SKU + Mensaje

Mensaje = *"El SKU ya existe en el sistema"*

```

#### ERROR_EAN_PLU_DUPLICADO

```
ERROR_EAN_PLU_DUPLICADO = [EAN | PLU] + Mensaje

Mensaje = *"El código EAN o PLU ya está registrado"*

```

#### ERROR_STOCK_INSUFICIENTE

```
ERROR_STOCK_INSUFICIENTE = SKU + StockDisponible + CantidadSolicitada + Mensaje

Mensaje = *"Stock insuficiente para completar la operación"*

```

#### ERROR_PRODUCTO_DESCATALOGADO

```
ERROR_PRODUCTO_DESCATALOGADO = SKU + Mensaje

Mensaje = *"El productoEntity está descatalogado y no puede ser utilizado"*

```

#### ERROR_POLITICA_ROTACION

```
ERROR_POLITICA_ROTACION = SKU + IDLote + PoliticaRotacion + Mensaje

Mensaje = *"El movimiento contradice la política de rotación de existencias"*

```

#### ERROR_LOTE_CADUCADO

```
ERROR_LOTE_CADUCADO = IDLote + FechaCaducidad + Mensaje

Mensaje = *"El lote está caducado y no puede ser utilizado"*

```

#### ERROR_DEVOLUCION_FUERA_PLAZO

```
ERROR_DEVOLUCION_FUERA_PLAZO = NumeroTicket + FechaCompra + DiasTranscurridos + 
                               Mensaje

Mensaje = *"La devolución excede el plazo máximo de 30 días"*

```

#### ERROR_DEVOLUCION_PRODUCTO_FRESCO

```
ERROR_DEVOLUCION_PRODUCTO_FRESCO = SKU + NombreProducto + NombreCategoria + 
                                   Mensaje

Mensaje = *"Los productos frescos no admiten devolución"*

```

#### ERROR_CREDENCIALES_INVALIDAS

```
ERROR_CREDENCIALES_INVALIDAS = NombreUsuario + Mensaje

Mensaje = *"Usuario o contraseña incorrectos"*

```

#### ERROR_TOKEN_EXPIRADO

```
ERROR_TOKEN_EXPIRADO = TokenJWT + FechaExpiracion + Mensaje

Mensaje = *"El token ha expirado. Por favor, inicie sesión nuevamente"*

```

#### ERROR_PERMISOS_INSUFICIENTES

```
ERROR_PERMISOS_INSUFICIENTES = IDUsuario + RecursoSolicitado + AccionSolicitada + 
                               Mensaje

Mensaje = *"No tiene permisos para realizar esta operación"*

```

#### ERROR_USUARIO_INACTIVO

```
ERROR_USUARIO_INACTIVO = IDUsuario + NombreUsuario + Mensaje

Mensaje = *"La cuenta de usuario está desactivada"*

```

#### ERROR_TICKET_NO_ENCONTRADO

```
ERROR_TICKET_NO_ENCONTRADO = IDTicket + Mensaje

Mensaje = *"El ticket no existe en el sistema"*

```

#### ERROR_CATEGORIA_CON_PRODUCTOS

```
ERROR_CATEGORIA_CON_PRODUCTOS = IDCategoria + CantidadProductos + Mensaje

Mensaje = *"No se puede eliminar una categoría que contiene productos"*

```

#### ERROR_PRODUCTO_CON_HISTORIAL

```
ERROR_PRODUCTO_CON_HISTORIAL = SKU + Mensaje

Mensaje = *"No se puede eliminar un productoEntity con historial de transacciones"*

```

#### ERROR_LOTE_NO_ENCONTRADO

```
ERROR_LOTE_NO_ENCONTRADO = IDLote + Mensaje

Mensaje = *"El lote no existe en el inventario"*

```

#### ERROR_FORMATO_INVALIDO

```
ERROR_FORMATO_INVALIDO = Campo + ValorRecibido + FormatoEsperado + Mensaje

Mensaje = *"El formato del campo no es válido"*

```

#### ERROR_FECHA_INVALIDA

```
ERROR_FECHA_INVALIDA = Fecha + Mensaje

Mensaje = *"La fecha no es válida o está fuera del rango permitido"*

```

#### ERROR_OFERTA_ACTIVA

```
ERROR_OFERTA_ACTIVA = SKU + IDOfertaActual + Mensaje

Mensaje = *"Ya existe una oferta activa para este productoEntity"*

```

#### ERROR_CANTIDAD_INVALIDA

```
ERROR_CANTIDAD_INVALIDA = Cantidad + Mensaje

Mensaje = *"La cantidad debe ser un valor positivo"*

```

#### ERROR_PRECIO_INVALIDO

```
ERROR_PRECIO_INVALIDO = Precio + Mensaje

Mensaje = *"El precio debe ser mayor o igual a 0 con formato de 2 decimales"*

```

#### ERROR_UNIDAD_MEDIDA_INCONSISTENTE

```
ERROR_UNIDAD_MEDIDA_INCONSISTENTE = UnidadCatalogo + UnidadRecibida + Mensaje

Mensaje = *"La unidad de medida no coincide con la del productoEntity en catálogo"*

```

#### ERROR_EAN_PLU_SIMULTANEOS

```
ERROR_EAN_PLU_SIMULTANEOS = SKU + Mensaje

Mensaje = *"No se puede registrar EAN y PLU simultáneamente"*

```

#### ERROR_CONTRASEÑA_ACTUAL_INCORRECTA

```
ERROR_CONTRASEÑA_ACTUAL_INCORRECTA = Mensaje

Mensaje = *"La contraseña actual no es correcta"*

```

#### ERROR_NOMBRE_USUARIO_DUPLICADO

```
ERROR_NOMBRE_USUARIO_DUPLICADO = NombreUsuario + Mensaje

Mensaje = *"El nombre de usuario ya está en uso"*

```

#### ERROR_ROL_NO_ENCONTRADO

```
ERROR_ROL_NO_ENCONTRADO = IDRol + Mensaje

Mensaje = *"El rol especificado no existe"*

```

#### ERROR_TICKET_NO_PERTENECE_USUARIO

```
ERROR_TICKET_NO_PERTENECE_USUARIO = IDTicketTemporal + IDUsuarioTicket + 
                                    IDUsuarioActual + Mensaje

Mensaje = *"El ticket no pertenece al usuario actual"*

```

#### ERROR_TICKET_YA_CERRADO

```
ERROR_TICKET_YA_CERRADO = NumeroTicket + Mensaje

Mensaje = *"El ticket ya ha sido cerrado y no puede modificarse"*

```

#### ERROR_DISCREPANCIA_INVENTARIO

```
ERROR_DISCREPANCIA_INVENTARIO = SKU + StockLogico + StockFisico + 
                                Discrepancia + Mensaje

Mensaje = *"Se ha detectado una discrepancia en el inventario"*
**Nota: Esta es una advertencia, no necesariamente bloquea la operación**

```

----------

## SECCIÓN 4: DATOS ELEMENTALES COMUNES

```
SKU = *Código alfanumérico único del productoEntity, formato definido por empresa*
EAN = *13 dígitos numéricos*
PLU = *4-5 dígitos numéricos*
Nombre = *Cadena de texto hasta 200 caracteres*
Descripcion = *Cadena de texto hasta 1000 caracteres*
PrecioVenta = *Decimal(10,2), >= 0*
PrecioPromocional = *Decimal(10,2), >= 0*
Cantidad = *Decimal(10,3), > 0 para la mayoría de operaciones*
CantidadAjuste = *Decimal(10,3), puede ser positiva o negativa*
Fecha = *Formato YYYY-MM-DD*
FechaHora = *Formato YYYY-MM-DD HH:MM:SS*
Timestamp = *Formato ISO 8601 con zona horaria*
Mensaje = *Cadena de texto descriptiva*
Estado = *Estado del registro*
IDUsuario = *Identificador único de usuario*
IDCategoria = *Identificador único de categoría*
IDProveedor = *Identificador único de proveedor*
IDLote = *Identificador único de lote*
IDTicketTemporal = *Identificador único de ticket temporal*
NumeroTicket = *Número único de ticket cerrado*
TokenJWT = *Token JWT cifrado*
NombreUsuario = *Cadena alfanumérica de 5-50 caracteres*
Contraseña = *Cadena de al menos 8 caracteres*
HashContraseña = *Hash bcrypt de la contraseña*
Salt = *Salt para el hash*
UnidadMedida = [Unidad | Kilogramo | Gramo | Litro | Mililitro | Metro | Paquete | Docena]
PoliticaRotacion = [FIFO | FEFO | LIFO]
**FIFO = First In, First Out**
**FEFO = First Expired, First Out**
**LIFO = Last In, First Out**
NumeroLote = *Identificador alfanumérico del lote del proveedor*
Impuesto = *Decimal(5,2) representando porcentaje de impuesto*
Subtotal = *Decimal(10,2) suma de precios sin impuestos*
Total = *Decimal(10,2) monto total incluyendo impuestos*
MontoRecibido = *Decimal(10,2) cantidad recibida del cliente*
MontoCambio = *Decimal(10,2) cambio devuelto al cliente*
MontoReembolsado = *Decimal(10,2) cantidad reembolsada en devolución*
CodigoBarras = *Cadena que representa código de barras escaneado*
CodigoBarrasGranel = *Código especial: Prefijo(1) + PLU(5) + Peso(5) o Precio(4) + CheckDigit(1)*
Etiquetas = *Cadena de texto con etiquetas separadas por coma o punto y coma*
Recurso = *Nombre del recurso del sistema*
Accion = [Crear | Leer | Actualizar | Eliminar | Ejecutar]
TipoMovimiento = [Entrada | Salida | Ajuste | Traslado | Venta | Devolucion | Merma]
TipoAjuste = [Merma | Robo | Caducado | ErrorConteo | ProductoEncontrado]
MetodoPago = [Efectivo | TarjetaDebito | TarjetaCredito | Transferencia]
Booleano = [Verdadero | Falso]
DiasTranscurridos = *Entero representando días*
DiasRestantes = *Entero representando días (puede ser negativo si está vencido)*
StockActual = *Decimal(10,3) cantidad actual en inventario*
StockDisponible = *Decimal(10,3) cantidad disponible para venta*
StockLogico = *Decimal(10,3) cantidad según registros del sistema*
StockFisico = *Decimal(10,3) cantidad según conteo físico*
Discrepancia = *Decimal(10,3) diferencia entre stock lógico y físico*
UmbralMinimo = *Decimal(10,3) cantidad mínima configurada*

```

----------

## NOTAS FINALES

1.  Los almacenes están definidos en **plural** según la convención del DAN.
2.  Los flujos de datos están definidos en **mayúsculas con guiones bajos**.
3.  Se ha utilizado `@` para identificar las claves primarias en los almacenes.
4.  Los campos opcionales están entre paréntesis `()`.
5.  Las iteraciones (listas) están entre llaves `{}`.
6.  Las alternativas están entre corchetes `[]` con `|` como separador.
7.  Los datos elementales están definidos entre asteriscos `*...*`.
8.  Se han incluido mensajes de error comunes para documentar las respuestas negativas del sistema.
9.  Todos los flujos están relacionados con sus acontecimientos correspondientes mediante comentarios `**Usado en ACxx**` o `**Salida de ACxx**`.

----------

¿Necesitas que añada algo más al diccionario de datos o que profundice en alguna sección específica?