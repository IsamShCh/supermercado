erDiagram
    %% ========================================
    %% BD AUTENTICACION
    %% ========================================
    USUARIO {
        string IDUsuario PK
        string NombreUsuario UK
        string HashContraseña
        string Salt
        string NombreCompleto
        enum Estado "Activo, Inactivo"
        timestamp FechaCreacion
        timestamp FechaUltimoAcceso
        boolean RequiereCambioContraseña
    }
    
    ROLES {
        string IDRol PK
        string NombreRol
        string DescripcionRol
    }
    
    PERMISOS {
        string IDPermiso PK
        string NombrePermiso
        string Descripcion
        string Recurso
        enum Accion "Crear, Leer, Actualizar, Eliminar, Ejecutar"
    }
    
    SESION {
        string TokenJWT PK
        string IDUsuario FK
        timestamp FechaHoraInicio
        timestamp FechaHoraFin
        enum Estado "Activa, Cerrada, Expirada"
    }
    
    USUARIO_ROLES {
        string IDUsuario FK
        string IDRol FK
    }
    
    ROL_PERMISOS {
        string IDRol FK
        string IDPermiso FK
    }

    %% ========================================
    %% BD CATALOGO
    %% ========================================
    PRODUCTOS {
        string SKU PK
        string EAN UK
        string PLU UK
        string Nombre
        text Descripcion
        decimal PrecioVenta
        boolean Caduca
        boolean EsGranel
        string IDCategoria FK
        enum PoliticaRotacion "FIFO, FEFO, LIFO"
        enum UnidadMedida
        text Etiquetas
        enum Estado "Activo, Descatalogado"
    }
    
    CATEGORIAS {
        string IDCategoria PK
        string NombreCategoria
        text Descripcion
    }
    
    PROVEEDORES {
        string IDProveedor PK
        string NombreProveedor
        string Contacto
        string Direccion
        string Telefono
        string Email
    }
    
    OFERTAS {
        string IDOferta PK
        string SKU FK
        decimal PrecioPromocional
        string TipoPromocion
        date FechaInicio
        date FechaFin
        enum Estado "Activa, Vencida, Cancelada"
    }

    %% ========================================
    %% BD VENTAS
    %% ========================================
    TICKETS {
        string IDTicket PK
        string NumeroTicket UK
        string IDUsuario FK
        timestamp FechaHora
        decimal Subtotal
        decimal TotalImpuestos
        decimal Total
        string IDPago FK
        enum Estado "Temporal, Cerrado, Cancelado"
    }
    
    ITEM_TICKET {
        string IDItemTicket PK
        string IDTicket FK
        int NumeroLinea
        string SKU FK
        string Descripcion
        decimal Cantidad
        decimal PrecioUnitario
        decimal Descuento
        decimal Subtotal
        decimal Impuesto
    }
    
    PAGOS {
        string IDPago PK
        string NumeroTicket FK
        enum MetodoPago "Efectivo, TarjetaDebito, TarjetaCredito, Transferencia"
        decimal MontoRecibido
        decimal MontoCambio
        timestamp FechaHora
    }
    
    DEVOLUCIONES {
        string IDDevolucion PK
        string NumeroTicket FK
        date FechaDevolucion
        decimal MontoReembolsado
        string IDUsuario FK
    }
    
    PRODUCTO_DEVOLUCION {
        string IDDevolucion FK
        string SKU FK
        decimal Cantidad
        decimal PrecioOriginal
        decimal Subtotal
    }

    %% ========================================
    %% BD INVENTARIO
    %% ========================================
    INVENTARIO {
        string IDInventario PK
        string SKU FK
        decimal CantidadAlmacen
        decimal CantidadEstanteria
        enum UnidadMedida
    }
    
    LOTES {
        string IDLote PK
        string SKU FK
                string IDInventario FK
        string EAN
        string PLU
        string NumeroLote
        decimal Cantidad
        date FechaCaducidad
        string IDProveedor FK
        date FechaIngreso
        enum UnidadMedida
        enum Estado "Disponible, Bloqueado, Eliminado"
    }
    
    MOVIMIENTOS_INVENTARIO {
        string IDMovimiento PK
        string SKU FK
        string IDLote FK
        enum TipoMovimiento "Entrada, Salida, Ajuste, TrasladoEstanteria, Venta, Devolucion, Merma"
        decimal Cantidad
        enum UnidadMedida
        timestamp FechaHora
        string IDUsuario FK
        string Motivo
        text Observaciones
    }

    %% ========================================
    %% BD MONITOREO
    %% ========================================
    ALERTAS {
        string IDAlerta PK
        enum TipoAlerta "StockMinimo, Caducidad, Sistema"
        string SKU FK
        text Mensaje
        timestamp FechaHoraGeneracion
        enum Estado "Pendiente, Atendida, Ignorada"
        timestamp FechaHoraAtendida
        string IDUsuarioAtendio FK
    }
    
    REPORTE_VENTAS_DIARIO {
        string IDReporte PK
        date Fecha
        string SKU FK
        string NombreProducto
        string NombreCategoria
        decimal CantidadVendida
        decimal MontoTotal
        int NumeroTransacciones
        timestamp FechaActualizacion
    }
    
    REPORTE_DEVOLUCIONES_DIARIO {
        string IDReporte PK
        date Fecha
        string SKU FK
        string NombreProducto
        string NombreCategoria
        decimal CantidadDevuelta
        decimal MontoReembolsado
        int NumeroTransacciones
        timestamp FechaActualizacion
    }
    
    REPORTE_CATALOGO {
        string IDReporte PK
        date FechaGeneracion
        int TotalProductos
        int ProductosActivos
        int ProductosDescatalogados
        int ProductosConOferta
        decimal PromedioPrecios
        timestamp FechaActualizacion
    }
    
    REPORTE_CADUCIDAD {
        string IDReporte PK
        date FechaGeneracion
        string SKU FK
        string NombreProducto
        string IDLote FK
        string NumeroLote
        date FechaCaducidad
        decimal CantidadAlmacen
        decimal CantidadEstanteria
        int DiasRestantes
        enum Categoria "Proximos7Dias, Proximos3Dias, Vencidos"
        timestamp FechaActualizacion
    }
    
    METRICAS_SISTEMA {
        string IDMetrica PK
        timestamp FechaHora
        string NombreMetrica
        decimal Valor
        string Unidad
        text Contexto
    }

    %% ========================================
    %% RELACIONES - BD AUTENTICACION
    %% ========================================
    USUARIO ||--o{ USUARIO_ROLES : "tiene"
    ROLES ||--o{ USUARIO_ROLES : "asignado a"
    ROLES ||--o{ ROL_PERMISOS : "tiene"
    PERMISOS ||--o{ ROL_PERMISOS : "asignado a"
    USUARIO ||--o{ SESION : "inicia"

    %% ========================================
    %% RELACIONES - BD CATALOGO
    %% ========================================
    CATEGORIAS ||--o{ PRODUCTOS : "contiene"
    %% PROVEEDORES ||--o{ PRODUCTOS : "suministra"
    PRODUCTOS ||--o{ OFERTAS : "tiene"

    %% ========================================
    %% RELACIONES - BD VENTAS
    %% ========================================
    TICKETS ||--o{ ITEM_TICKET : "contiene"
    TICKETS ||--o| PAGOS : "procesado con"
    TICKETS ||--o{ DEVOLUCIONES : "genera"
    DEVOLUCIONES ||--o{ PRODUCTO_DEVOLUCION : "incluye"

    %% ========================================
    %% RELACIONES - BD INVENTARIO
    %% ========================================
    INVENTARIO ||--o{ LOTES : "compuesto por"
    LOTES ||--o{ MOVIMIENTOS_INVENTARIO : "genera"
        PROVEEDORES ||--o{ LOTES : "suministra"