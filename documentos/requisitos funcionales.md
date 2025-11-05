#SECTION - Catalogo

ID: REQ-1
#NOTE - Nombre: Crear productoEntity en catálogo
Descripción: El sistema permitirá a los administradores de inventario crear nuevos productos en el catálogo especificando SKU único, EAN/PLU, nombre, descripción, precio de venta, oferta aplicada, si caduca, si es a granel, categoría, política de rotacion de existencias y proveedor.
Precondiciones:
- Usuario autenticado con permisos de administrador de inventario
- SKU y el EAN o PLU no existe previamente
Postcondiciones:
- Producto disponible en catálogo para gestión de inventario
Restricciones:
- Las ofertas registradas deben incluir fecha de inicio y fin válidas (si no, la oferta no se aplica).
- SKU, el EAN o PLU debe cumplir el patrón/longitud definidos por la empresa y ser único.
- Si se introduce EAN, no se podra introducir PLU y viceversa
- El precio debe un valor numérico mayor o igual a 0, en formato 2 decimales.
- Un productoEntity solo puede pertenecer a una sola categoría
Prioridad: Alta


ID: REQ-2
#NOTE - Nombre: Modificar productoEntity en catálogo
Descripción: El sistema permitirá a los administradores de inventario modificar información de productos existentes incluyendo precio, descripción, nombre, categoría, etc.
Entradas:
- ID SKU
- Información actualizada (precio, descripción, nombre, categoría, etc.)
Salidas:
- Confirmación de modificación exitosa
- Detalles del productoEntity actualizado
Precondiciones:
- Usuario autenticado
- Usuario con permisos de administrador de inventario
- Producto existe en catálogo
Postcondiciones:
- Información del productoEntity actualizada
- Cambios registrados para monitoreo
Restricciones:
- No se podrá modificar el SKU del productoEntity.
- Si se modifica la oferta, esta debe incluir fecha de inicio y fin válidas.
- El precio debe ser un valor numérico mayor o igual a 0, en formato de dos decimales.
- Un productoEntity solo puede pertenecer a una sola categoría
Prioridad: Alta

ID: REQ-3
#NOTE - Nombre: Consultar productoEntity por SKU
Descripción: El sistema permitirá a los cajeros y a los administradores de inventario consultar productos introduciendo SKU para mostrar sus datos
Entradas:
- SKU del productoEntity
Proceso:
- Buscar productoEntity por SKU
- Recuperar la información de la bbdd
Salidas:
- Detalles sobre el productoEntity
- Error si productoEntity no encontrado
Precondiciones:
- Usuario autenticado
- Producto existe en catálogo
Prioridad: Alta

ID: REQ-4
#NOTE - Nombre: Listar todos productos
Descripción: El sistema permitirá a los cajeros y a los administradores de inventario obtener un listado de todos los productos
Entradas:
- 
Proceso:
- Listar información de la bbdd
Salidas:
- Detalles sobre el productoEntity
Precondiciones:
- Usuario autenticado
Prioridad: Alta

ID: REQ-5
#NOTE - Nombre: Buscar productos por criterios
Descripción: El sistema proporcionará funcionalidad de búsqueda de productos por nombre, categoría u otros atributos sin necesidad de código de barras.
Entradas:
- Criterios de búsqueda (nombre, categoría..j.)
Proceso:
- Analizar criterios de búsqueda
- Consultar base de datos
- Devolver resultados que coincidan
Salidas:
- Lista de productos que coinciden con criterios
Precondiciones:
- Usuario autenticado
Restricciones:
- Si la búsqueda combina múltiples criterios, los resultados se filtrarán aplicando condiciones lógicas AND.
Prioridad: Media

ID: REQ-6
#NOTE - Nombre: Descatalogar productos
Descripción: El sistema permitirá marcar productos como descatalogados sin eliminarlos, impidiendo nuevas ventas y adiciones a inventario, pero conservando información.
Entradas:
- ID SKU del productoEntity
Proceso:
- Buscarlo en la bbdd
- Marcar productoEntity como inactivo
Salidas:
- Confirmación de desactivación
Precondiciones:
- Usuario con permisos de administrador de catalogo.
- Producto existe
Postcondiciones:
- Producto no puede venderse pero datos preservados
- El productoEntity no puede tener existencias en estantería
Prioridad: Media

ID: REQ-7
#NOTE - Nombre: Volver a catalogar productos
Descripción: El sistema permitirá marcar productos como descatalogados sin eliminarlos, volviendo a permitir nuevas ventas y adiciones a inventario.
Entradas:
- ID SKU del productoEntity
Proceso:
- Buscarlo en la bbdd
- Marcar productoEntity como activo
Salidas:
- Confirmación de reactivacion
Precondiciones:
- Usuario con permisos de administrador de catalogo.
- Producto existe
Postcondiciones:
- Producto puede venderse y se puede volver a agregar nuevo stock de ese productoEntity
Prioridad: Media

ID: REQ-8
#NOTE - Nombre: Eliminacion de elemento del catálogo
Descripción: El sistema permitirá eliminación permanente de productos solo si no tienen historial de ventas, devoluciones o movimientos de inventario asociados.
Entradas:
- ID del productoEntity
Proceso:
- Verificar ausencia de historial de transacciones
- Eliminar productoEntity si no hay asociaciones
- Denegar eliminación si hay historial
Salidas:
- Confirmación de eliminación o mensaje de error
Precondiciones:
- Usuario con permisos de administrador de catalogo
- Producto sin historial de transacciones que generen problemas de integridad de los datos
Postcondiciones:
- Producto completamente removido del sistema.
Prioridad: Baja

ID: REQ-9
#NOTE - Nombre: Crear categorias
Descripción: El sistema permitirá crear categorías que se podran asignar productos a categorías para organizar el catálogo y facilitar búsquedas.
Entradas:
- Categoría
- Descripcion
Proceso:
- Crear categorías
Salidas:
- Estructura de categorías actualizada
Precondiciones:
- Usuario con permisos de administrador de catálogo o superiores
Postcondiciones:
- Categorías disponibles para organización de productos
Restricciones:
Prioridad: Media

ID: REQ-10
#NOTE - Nombre: Modificar categorias
Descripción: El sistema permitirá modificar categorías.
Entradas:
- Categoría
Proceso:
- Modificar categorías
Salidas:
- Categoría actualizada
Precondiciones:
- Usuario con permisos de administrador de catálogo o superiores
Postcondiciones:
- Categorías disponibles para organización de productos
Restricciones:
Prioridad: Media

ID: REQ-11
#NOTE - Nombre: Crear ofertas.
Descripción: El sistema permitirá definir precios promocionales temporales para productos con rangos de fechas de validez.
Entradas:
- ID SKU del productoEntity
- Precio promocional
- Tipo de promocion
- Fecha de inicio y fin de validez
Proceso:
- Buscar la entrada del produco en la bbdd
- Establecer oferta con período de validez
Salidas:
- Confirmación de la asignación de la promoción
Precondiciones:
- Usuario con permisos de gerencia comercial
Postcondiciones:
- Precio promocional aplicado durante período de validez
Restricciones:
- Solo puede estar activa una oferta a la vez en un productoEntity
Prioridad: Media

ID: REQ-12
#NOTE - Nombre: Asignar etiqutas personalizadas a productos.
Descripción: El sistema permitirá asignar etiquetas personalizadas a productos para mejorar capacidad de búsqueda.
Entradas:
- ID del productoEntity
- Nombres de etiquetas
Proceso:
- Asignar etiquetas a productos
Salidas:
- Etiquetas asociadas con productos
Precondiciones:
- Usuario con permisos de catálogo
Postcondiciones:
- Etiquetas disponibles para búsqueda y filtrado
Restricciones:
- Solo hay un campo etiquetas, y las nuevas se concatenan al final de las anteriores.
Prioridad: Media


ID: REQ-13
#NOTE - Nombre: Añadir productos que se venden por peso.
Descripción: El sistema permitirá configurar productos vendidos por peso especificando precio por kilogramo (a granel).
Entradas:
- SKU
- PLU
- Precio por kilogramo
- Demás datos
Proceso:
- Configurar productoEntity por peso
- Establecer reglas de decodificación de códigos de barras
Salidas:
- Producto configurado para ventas por peso
Precondiciones:
- El PLU y el SKU no deben existir previamente en el catálogo.
Postcondiciones:
- El productoEntity queda disponible para operaciones de inventario y venta que reconozcan su naturaleza de peso variable.
Restricciones:
- El SKU y el PLU deben ser validos en su formato.
- El precio debe ser un valor numérico mayor o igual a 0, expresado en formato de dos decimales y correspondiente al precio por unidad de peso.
- No se podrán registrar simultáneamente EAN y PLU para este tipo de productoEntity.
- Si el productoEntity tiene oferta activa, esta debe incluir fecha de inicio y fin válidas.
Prioridad: Alta



ID: REQ-14
#NOTE - Nombre: Soporte para distintas unidades de medida en productos del catálogo
Descripción: El sistema permitirá definir y gestionar distintas unidades de medida para los productos registrados en el catálogo del supermercado. Al crear o modificar un productoEntity, el administrador de inventario podrá seleccionar la unidad de medida adecuada según la naturaleza del productoEntity (por ejemplo: unidad, kilogramo, gramo, litro, mililitro, metro, paquete, docena, etc.). El sistema deberá utilizar la unidad seleccionada en todas las operaciones relacionadas con el productoEntity, como cálculo de precios, presentación en el punto de venta y gestión de inventario.
Entradas:
Proceso:
Salidas:
Precondiciones:
Postcondiciones:
Restricciones:
- Las unidades de medida deben seleccionarse de un catálogo predefinido configurado por la empresa.
- No se permitirá registrar un productoEntity sin una unidad de medida válida.
Prioridad: Alta



ID: REQ-15
#NOTE - Nombre: Restricción de identificadores exclusivos en productos
Descripción: El sistema garantizará que cada productoEntity registrado en el catálogo tenga asignado únicamente un tipo de identificador entre PLU o EAN. No se permitirá que un productoEntity tenga ambos identificadores simultáneamente. Esta restricción asegura la unicidad y consistencia de los productos en el catálogo y evita conflictos en la gestión de inventario y ventas.
Entradas:
Proceso:
Salidas:
Precondiciones:
Postcondiciones:
Restricciones:
- Solo se permite uno de los dos identificadores por productoEntity (PLU o EAN).
- Esta validación se aplica tanto al crear como al modificar un productoEntity.
- No se permiten combinaciones de ambos en ninguna operación del catálogo.
Prioridad: Alta



ID: REQ-16
#NOTE - Nombre: Traducción entre identificadores
Descripción: El sistema permitirá a los usuarios autenticados obtener los identificadores de un productoEntity a partir de otro identificador. Es decir, se podrá ingresar un SKU para consultar el PLU o EAN correspondiente, o ingresar un PLU o EAN para obtener el SKU asociado. Esta funcionalidad facilita la identificación cruzada de productos en el catálogo e inventario y asegura consistencia entre los distintos sistemas de gestión de productos.
Entradas:
- El PLU o EAN, o el SKU.
Proceso:
- Se identifica el formato del codigo para reconocer si es PLU, EAN o SKU.
- Se recorre el catálogo el busca de ese codigo.
- Se devuelve el otro codigo correspondiente.
Salidas:
- El PLU o EAN si se introdujo un SKU, o el SKU si se introdujo un PLU o EAN
Precondiciones:
- Usuario autenticado.
Postcondiciones:
Prioridad: Alta
#!SECTION


-----------

#SECTION - Inventario


ID: REQ-17
#NOTE - Nombre: Agregar nuevas existencias
Descripción: El sistema permitirá registrar entradas nuevas de existencias especificando productoEntity, EAN,cantidad inicial, unidad de medida, lote, fecha de caducidad y proveedor para productos existentes.
Entradas:v
- ID SKU del productoEntity
- EAN
- Cantidad
- Número de lote
- Fecha de caducidad, si es que caduca
- Proveedor
Proceso:
- Validar existencia del productoEntity
- Registrar movimiento de nuevas existencias a inventario con un identificador unico
- Calculamos el total de stock sumando las cantidades disponibles de todos los productos.
Salidas:
- Información de las nuevas exitencias introducidas
- Stock total del productoEntity
Precondiciones:
- Usuario con permisos de administrador de inventario
- El productoEntity existe en el catalogo y no se encuentra descatalogado.
Postcondiciones:
- Nuevo lote de productoEntity agregado.
- Se registra que la cantidad de existencias de ese lote en las estanterias es 0 unidades (la unidad que corresponda)
- Se registra que la cantidad de existencias de ese lote en almacén es de X unidades, siendo X = cantidad inicial.
Restricciones:
- La cantidad agregada debe ser un valor numérico positivo.
- Si el productoEntity es perecedero, la fecha de caducidad debe ser posterior a la fecha de ingreso.
- El SKU debe estar en un formato valido
- Cada lote deberá tener un identificador único
- Las unidad de medida del lote debe ser igual a la unidad de medida recogida en el catálogo.
- El EAN del lote debe coincidir con el EAN del productoEntity.
Prioridad: Alta



ID: REQ-18
#NOTE - Nombre: Agregar nuevas existencias de productos a granel
Descripción: El sistema permitirá registrar entradas nuevas de existencias de productos a granel especificando productoEntity, la cantidad ingresada, unidad de medida, lote, fecha de caducidad y proveedor para productos existentes.
Entradas:
- ID SKU del productoEntity
- PLU
- Cantidad
- Número de lote
- Fecha de caducidad, si es que caduca, o su fecha de cosecha, fecha de consumo preferente, vida util estimada.
- Proveedor
Proceso:
- Validar existencia del productoEntity
- Registrar movimiento de nuevas existencias a inventario con un identificador unico
- Calculamos el total de stock sumando las cantidades disponibles de todos los productos.
Salidas:
- Información de las nuevas exitencias introducidas
- Stock total del productoEntity
Precondiciones:
- Usuario con permisos de administrador de inventario
- El productoEntity existe en el catalogo y no se encuentra descatalogado.
Postcondiciones:
- Nuevo lote de productoEntity agregado.
- Se registra que la cantidad de existencias de ese lote en las estanterias es 0 unidades (la unidad que corresponda)
- Se registra que la cantidad de existencias de ese lote en almacén es de X unidades, siendo X = la cantidad ingresada.
Restricciones:
- La cantidad agregada debe ser un valor positivo y expresada en la unidad de medida definida para el productoEntity.
- Si el productoEntity es perecedero, la fecha de caducidad debe ser posterior a la fecha de ingreso.
- El SKU y PLU debe estar en un formato valido
- Cada lote deberá tener un identificador único
- Las unidad de medida del lote debe ser igual a la unidad de medida recogida en el catálogo.
- El PLU del lote debe coincidir con el PLU del productoEntity.
Prioridad: Alta



ID: REQ-19
#NOTE - Nombre: Historial de movimientos de inventario 
Descripción: El sistema almacenará historial completo de movimientos de inventario incluyendo entradas, ventas, devoluciones y ajustes.
Entradas:
- Detalles de movimientos de inventario
Proceso:
- Registrar cada movimiento
- Mantener historial cronológico
Salidas:
- Historial completo de movimientos
Precondiciones:
- Operación de inventario realizada
Postcondiciones:
- Movimiento registrado en historial
Nota: para esto es que vamos a usar kafka y eventos.
Prioridad: Alta


ID: REQ-20
#NOTE - Nombre: Ajuste manual de inventario
Descripción: El sistema permitirá ajustes manuales de inventario especificando tipo de ajuste, cantidad y motivo detallado.
Entradas:
- ID SKU del productoEntity
- Cantidad de ajuste
- Tipo de ajuste (merma, robo, caducado, error de conteo, productoEntity encontrado)
- Motivo detallado
Proceso:
- Aplicar ajuste
- Registrar con justificación y publicar evento
Salidas:
- Niveles de stock actualizados
Precondiciones:
- Usuario con permisos de inventario
Postcondiciones:
- Ajuste auditado y registrado
Prioridad: Alta


ID: REQ-21
#NOTE - Nombre: Eliminar elementos del inventario
Descripción: El sistema permitirá eliminar elementos del inventario, junto con su motivo de eliminación
Entradas:
- Información de lote/lote
- Razón de bloqueo agregada y publicación de evento
Proceso:
- Marcar stock como bloqueado
- Prevenir ventas
Salidas:
- Estado de stock bloqueado
Precondiciones:
- Permisos de inventario
Postcondiciones:
- Lote del stock eliminado no disponible para venta
Prioridad: Baja



ID: REQ-22
#NOTE - Nombre: Mover stock a estanterías
Descripción: El sistema permitirá registrar el movimiento de stock de un lote desde el inventario hasta la estantería para que los clientes lo puedan comprar.
Entradas:
- SKU
- Lote
- Cantidad a transladar
- Unidades de medida
Proceso:
- Buscar el productoEntity en el catálogo 
- Buscar el lote en el inventario
- Asegurarse de que el movimiento no contraviene la politica de rotación de existencias asociada al productoEntity (ese dato viene recogido en el catálogo). (FIFO, FEFO, LIFO)
- Incrementar la cantidad de productoEntity en estantería
- Decrementar la cantidad de productoEntity en almacén
Salidas:
- Detalles sobre el movimiento o mensaje de error (no se cumple con la politica, esta cadudado, )
Precondiciones:
- Usuario autenticado con permisos de administrador de inventario.
- El productoEntity identificado por el SKU debe existir en el catálogo y el lote debe existir
- El lote no está caducado
Postcondiciones:
- Las existencias en el almacén se reducen según la cantidad trasladada.
- Las existencias en las estanterías aumentan con la cantidad recibida.
Restricciones:
- No se puede trasladar más cantidad de la que existe en el almacén.
- Solo los productos activos en el catálogo pueden ser trasladados.
- El movimiento no puede contradecir la logica de la política de rotación de existencias
Prioridad: Alta


ID: REQ-23
#NOTE - Nombre: Contabilización manual de stock
Descripción: El sistema debe permitir iniciar procesos de contabilización manual de stock en inventario y en almacen de un productoEntity, de forma que se puedan ver las discrepancias entre el stock fisico y el lógico de un productoEntity. 
IMPORTANTE: una vez los productos pasan fisicamente a estantería perdemos su trazabilidad. Ya no podemos saber a que lote pertenece cada productoEntity de la estantería
Entradas:
- SKU
- Stock del productoEntity en estanterias
- Stock dek productoEntity en almacén
Proceso:
- Comparar físico total en estanteria vs lógico total en estanteria
- Comparar físico total en almacen vs lógico total en almacen
- Generar reportes de discrepancias
- Ajustar stock lógico (aplicando politica correspondiente)
Salidas:
- Reportes de discrepancias
Precondiciones:
- Usuario autenticado con permisos de administrador de inventario.
- El productoEntity a contabilizar debe existir en el catálogo y tener registro de stock en el sistema.
Postcondiciones:
- Niveles de stock corregidos
- Se registra en el sistema la fecha, el usuario y los ajustes realizados durante la contabilización.
Restricciones:
- La contabilización no puede modificar productos que estén descatalogados.
Prioridad: Media


ID: REQ-24
#NOTE - Nombre: Consultar inventario de un productoEntity
Descripción: El sistema permitirá a los usuarios autenticados obtener la información de inventario de un productoEntity específico. Para ello, el usuario deberá indicar el SKU productoEntity. El sistema mostrará las existencias totales disponibles tanto en almacen como en estantería, desglosadas por lote incluyendo fechas de caducidad, unidad de medida y cualquier información relevante
Entradas:
- SKU
- Opcionalmente el lote
Proceso:
Salidas:
- Toda la informacion del inventario del productoEntity
Precondiciones:
- Usuario autenticado con permisos de consulta de inventario.
- productoEntity consultado debe existir en el catálogo.
Postcondiciones:
- Se muestra el inventario actual del productoEntity con detalles por lote.
- 
Restricciones:
- Solo se puede consultar un productoEntity por operación.
Prioridad: Alta


ID: REQ-60
#NOTE - Nombre: Crear un proveedor
Descripción: El sistema permitirá a los administradores de inventario crear nuevos proveedores especificando nombre, información de contacto, dirección y otros datos relevantes para gestionar el suministro de productos.
Entradas: 
- Informacion sobre el proveedor
Proceso:
- Verificamos que no exite ya en la tabla
- Creamos una nueva entrada en la tabla de proveedores
Salidas:
- Confirmacion o mensaje de error
Precondiciones:
- Tener rol y permisos de gestión de inventario
- No haber otro proveedor con el mismo nombre
Postcondiciones:
- Nueva entrada en la tabla creada
Prioridad: Alta


#!SECTION


------------------


#SECTION - Devoluciones

ID: REQ-25
#NOTE - Nombre: Procesar devolución por ticket
Descripción: El sistema permitirá procesar devoluciones de productos introduciendo el número de ticket original, verificando que se cumple la política de devolución (30 días máximo, sin productos frescos) y calculando el monto a reembolsar. Los productos devueltos no se reincorporan al inventario.
Entradas:
- Número de ticket
- SKUs de productos a devolver
- Cantidades a devolver por cada productoEntity
Proceso:
- Validar que el ticket existe en el sistema
- Validar que la fecha de compra no supera los 30 días desde la fecha actual
- Validar que ningún productoEntity a devolver pertenece a la categoría "frescos"
- Calcular monto a reembolsar: suma de (precio_original_ticket × cantidad_devuelta) para cada productoEntity
- Registrar la devolución en la base de datos con: ticket_id, fecha_devolución, productos_devueltos, cantidades, monto_total
Salidas:
- Confirmación de devolución procesada o mensaje de rechazo indicando motivo
- Monto total a reembolsar al cliente
Precondiciones:
- Usuario autenticado con permisos de cajero o superior
- Ticket válido existe en el sistema
Postcondiciones:
- Devolución registrada en el sistema
- Los productos devueltos NO se reincorporan al inventario (se consideran merma automáticamente)
Restricciones:
- El plazo máximo para devoluciones es de 30 días desde la fecha de compra
- No se aceptan devoluciones de productos de la categoría "frescos"
- La cantidad a devolver no puede exceder la cantidad original comprada en el ticket
- Solo se puede procesar una devolución por ticket (no devoluciones parciales múltiples)
Prioridad: Alta


ID: REQ-26
#NOTE - Nombre: Política general de devoluciones
Descripción: El sistema aplicará una política de devolución única y fija para todas las transacciones. Los productos podrán ser devueltos en un plazo máximo de 30 días desde la fecha de compra, presentando el ticket original. Quedan excluidos de esta política los productos de la categoría "frescos", los cuales no admiten devolución bajo ninguna circunstancia.
Entradas:
- Fecha de la venta original (obtenida del ticket)
- Categoría del productoEntity a devolver (obtenida del catálogo)
Proceso:
- Al procesar una devolución, el sistema valida automáticamente:
    1. Que la fecha de la devolución no supere los 30 días desde la fecha de compra
    2. Que el productoEntity a devolver no pertenezca a la categoría "frescos"
- Si alguna condición no se cumple, la devolución se rechaza automáticamente
Salidas:
- Autorización o denegación de la devolución basada en el cumplimiento de la política
Precondiciones:
- Se inicia un proceso de devolución
- La categoría de los productos está correctamente definida en el catálogo
Postcondiciones:
- La devolución solo se permite si se cumplen las condiciones establecidas
Restricciones:
- Esta política es una regla de negocio fija y no puede ser configurada o modificada por los usuarios
- No existen excepciones a esta política
- Los productos devueltos se consideran merma y no se reincorporan al inventario
Prioridad: Alta


ID: REQ-27
#NOTE - Nombre: Reporte de devoluciones
Descripción: El sistema permitirá generar reportes de devoluciones procesadas en un período de tiempo especificado, mostrando productos devueltos, cantidades y montos totales reembolsados. Este reporte permite al negocio analizar patrones de devolución y calcular pérdidas por merma.
Entradas:
- Rango de fechas (fecha_inicio, fecha_fin)
- Opcionalmente: filtro por productoEntity, categoría o cajero
Proceso:
- Consultar base de datos de devoluciones en el rango especificado
- Agrupar devoluciones por productoEntity (SKU)
- Calcular totales: cantidad_devuelta y monto_reembolsado por productoEntity
- Calcular total general de todas las devoluciones del período
Salidas:
- Reporte conteniendo:
    - Por cada productoEntity: SKU, nombre, categoría, cantidad_total_devuelta, monto_total_reembolsado
    - Resumen general: total_productos_devueltos, monto_total_reembolsado_período
    - Número total de transacciones de devolución procesadas
Precondiciones:
- Usuario autenticado con permisos de gerencia o administración
- Existen devoluciones registradas en el sistema
Postcondiciones:
- Reporte generado disponible para visualización o exportación
Restricciones:
- El rango de fechas no puede ser mayor a 1 año
- Todos los productos devueltos se consideran merma en el reporte
Prioridad: Media

#!SECTION





-------------------------
#SECTION - Ventas


ID: REQ-28
#NOTE - Nombre: Crear ticket
Descripción: El sistema permitirá iniciar nuevas transacciones de venta desde TPV, generando ticket temporal con número único.
Entradas:
- Ninguna
Proceso:
- Generar número único de ticket
- Crear ticket temporal
Salidas:
- Ticket id nuevo creado
Precondiciones:
- Usuario autenticado como cajero
Postcondiciones:
- Sesión activa de ventas iniciada
Prioridad: Alta


ID: REQ-29
#NOTE - Nombre: Añadir productos al ticket
Descripción: El sistema permitirá añadir un productoEntity al ticket temporal junto con sus datos.
Entradas:
- Código de barras del productoEntity
- SKU del productoEntity
Proceso:
- Recuperar productoEntity
- Añadir al ticket
Salidas:
- Producto añadido al ticket
Precondiciones:
- SKU válido
Postcondiciones:
- Producto añadido ticket actual
Prioridad: Alta


ID: REQ-30
#NOTE - Nombre: Borrar ticket temporal
Descripción: El sistema permitirá eliminar un ticket temporal junto con sus datos.
Entradas:
- ID del ticket temporal
Proceso:
- Decodificar código de barras
- Recuperar productoEntity
- Añadir al ticket
Salidas:
- Producto añadido al ticket
Precondiciones:
- Código de barras válido escaneado
Postcondiciones:
- Producto en ticket actual
Prioridad: Alta


ID: REQ-31
#NOTE - Nombre: Eliminar productos del ticket temporal
Descripción: El sistema permitirá eliminar productos del ticket temporal actual si cliente decide no comprarlos.
Entradas:
- Producto a eliminar
Proceso:
- Identificar productoEntity en ticket
- Eliminar del ticket
Salidas:
- Producto eliminado
Precondiciones:
- Producto en ticket actual
- Ser el creador del ticket
Postcondiciones:
- Ticket actualizado
Prioridad: Alta



ID: REQ-32
#NOTE - Nombre: Constultar contenido de ticket
Descripción: El sistema permitirá consultar información de un ticket, ya sea temporal o cerrado
Entradas:
- Id del ticket
Proceso:
- Buscar id del ticket en la bbdd
- Recuperar y mostrar los datos asociados al ticket.
Salidas:
- Información detallada del ticket
Precondiciones:
- Usuario autenticado con permisos de ventas o administración.
- El ticket debe existir en el sistema
Postcondiciones:
- La operación no modifica el estado ni los datos del ticket.
Prioridad: Alta



ID: REQ-33
#NOTE - Nombre: Procesar códigos de barras por peso
Descripción: El sistema decodificará automáticamente códigos de barras especiales para productos a granel extraerá código sku de productoEntity, peso/precio y añadirá al ticket.
Entradas:
- Código de barras variable (prefijo 2)
Proceso:
- Decodificar peso/precio
- Buscar productoEntity
- Calcular monto correcto
- Añadir al ticket mostrando peso
Salidas:
- Producto por peso añadido
Precondiciones:
- Código de barras de peso válido
Postcondiciones:
- Monto correcto calculado
Prioridad: Alta


#TODO - ID: REQ-34
#NOTE - Nombre: Sistema especial para procesar venta de productos basados en peso (granel)
Descripción: El sistema permitirá venta de productos a granel basándose en peso decodificado del código especial de barras variable escaneado en caja para productos por peso cuando se complete la venta. Este codigo de barras especial no es ni PLU ni EAN, sino uno generado en la tienda cuando los clientes pesan su productoEntity. Este es escaneado en caja. Este codigo de barras especial para granel nos dice que productoEntity es y cuando peso de ese productoEntity hay.
NOTA: como diablos voy a saber yo si me esta llegando mercancia constantemente de productos PLU, de que lote tengo yo que descrementar. Como apaño puedo intentar decrementar del primero que halla llegado y que no esté caducado y que este en estantería
Entradas:
- Código especial de barras variable decodificado para granel
Proceso:
- Obtener SKU del codigo especial de barras
- Obtener el peso
-  
Precondiciones:
- El código de barras es el codigo de barras especial para granel
Postcondiciones:
- 
Prioridad: Alta


ID: REQ-35
#NOTE - Nombre: Correspondencia entre stock lógico y físico al vender
Descripción: El sistema mantendrá coherencia entre stock físico y lógico al decrementar automáticamente stock [de estanteria] cuando se venden productos. Esto incluye los productos a granel también.
Entradas:
- SKU del productoEntity
- Cantidad
- Unidades de medida
Proceso:
- Deducir a que lote pertenecía el productoEntity aplicando la politica de rotación de existendias correspondiente. (FIFO, FEFO, LIFO)
- Decrementar el stock en la estanteria de ese productoEntity en el lote deducido.
Salidas:
- Niveles de stock actualizados
Precondiciones:
- Stock suficiente disponible
Postcondiciones:
- Stock lógico y físico sincronizados [en teoría]
- Stock en estantería decrementado
Prioridad: Alta

ID: REQ-36
#NOTE - Nombre: Calcular totales e impuestos
Descripción: El sistema calculará automáticamente subtotal de productos, aplicará impuestos según categoría y calculará total final.
Entradas:
- Productos en ticket
Proceso:
- Sumar subtotales
- Aplicar tasas de impuesto
- Calcular total final
Salidas:
- Desglose de impuestos y total
Precondiciones:
- Productos añadidos al ticket
Postcondiciones:
- Total calculado correctamente
Prioridad: Alta

ID: REQ-37
#NOTE - Nombre: Procesar pago
Descripción: El sistema permitirá seleccionar método de pago, registrar monto recibido, calcular cambio y asociar información al ticket.
Entradas:
- Método de pago y monto recibido
Proceso:
- Registrar detalles de pago
- Calcular cambio si aplica
Salidas:
- Pago registrado
Precondiciones:
- Total calculado
Postcondiciones:
- Pago asociado con ticket
Prioridad: Alta

ID: REQ-38
#NOTE - Nombre: Cerrar ticket de venta
Descripción: El sistema cerrará el ticket de venta con número único, fecha, hora, productos, precios, impuestos, total, método de pago una vez finalizado el proceso de venta.
Entradas:
- ID del ticket
Proceso:
- Buscar ticket en la bbdd
- Comprobar estado del ticket
- Cerrar ticket
Salidas:
- Información completa del ticket cerrado
Precondiciones:
- Al menos un productoEntity agregado
- Pago procesado exitosamente
Postcondiciones:
- Ticket cerrado
Prioridad: Alta


ID: REQ-39
quiero pasar de los descuentos
#NOTE - Nombre: Aplicar promociones automáticas
Descripción: El sistema detectará automáticamente cuando se cumplen condiciones de promociones especificadas en el catálogo y aplicará descuentos correspondientes.
Entradas:
- Productos calificadores
Proceso:
- Detectar condiciones de promoción
- Aplicar descuentos automáticamente
- Mostrar promoción en ticket
Salidas:
- Promoción aplicada
Precondiciones:
- Productos califican para promoción
Postcondiciones:
- Descuento correcto calculado
Prioridad: Media


ID: REQ-40
#NOTE - Nombre: Validar stock antes de venta
Descripción: El sistema verificará disponibilidad de stock suficiente antes de añadir productos al ticket y emitirá un aviso en caso de incoherencia con el stock logico. La venta se completará de todos modos
Entradas:
- Selección de productoEntity
Proceso:
- Verificar stock disponible
- Emitir evento si hay incoherencia
Salidas:
- Resultado de validación de stock
Precondiciones:
- Producto seleccionado
Postcondiciones:
- Producto agregado
Prioridad: Alta


#!SECTION 

---------------------------

#SECTION - Autenticacion y autorización

ID: REQ-41
#NOTE - Nombre: Iniciar sesión de usuario
Descripción: El sistema permitirá iniciar sesión con nombre de usuario y contraseña para obtener token JWT de acceso a servicios.
Entradas:
- Nombre de usuario y contraseña
Proceso:
- Validar credenciales
- Generar token JWT
Salidas:
- Token JWT
Precondiciones:
- Credenciales válidas
Postcondiciones:
- Usuario autenticado para sesión
Prioridad: Alta

ID: REQ-42
#NOTE - Nombre: Validar tokens JWT en microservicios
Descripción: Cada microservicio recibirá token en llamadas gRPC, validará firma, vigencia y extraerá identidad y roles del usuario.
Entradas:
- Token JWT en solicitud
Proceso:
- Verificar firma del token
- Verificar expiración
- Extraer identidad y roles
Salidas:
- Resultado de autenticación
Precondiciones:
- Token proporcionado
Postcondiciones:
- Solicitud autorizada o denegada
Prioridad: Alta

ID: REQ-43
#NOTE - Nombre: Cerrar sesión de usuario
Descripción: El sistema permitirá cierre de sesión explícito al final del turno para asegurar que token ya no pueda utilizarse.
Entradas:
- Solicitud de cierre de sesión
Proceso:
- Invalidar token actual
Salidas:
- Confirmación de cierre de sesión
Precondiciones:
- Sesión activa
Postcondiciones:
- Token invalidado
Prioridad: Media

ID: REQ-44
#NOTE - Nombre: Manejar tokens expirados
Descripción: El sistema denegará solicitudes con tokens expirados y forzará nueva autenticación.
Entradas:
- Token expirado
Proceso:
- Detectar expiración
- Devolver error claro
Salidas:
- Error "Token Expirado"
Precondiciones:
- Token expirado
Postcondiciones:
- Solicitud denegada, re-autenticación requerida
Prioridad: Alta

ID: REQ-45
#NOTE - Nombre: Crear cuentas de usuario
Descripción: El sistema permitirá crear nuevas cuentas de usuario asignando nombre de usuario, contraseña temporal y roles.
Entradas:
- Detalles de usuario y roles
Proceso:
- Validar entrada
- Crear cuenta
- Asignar roles
Salidas:
- Usuario creado
Precondiciones:
- Permisos de administrador
Postcondiciones:
- Nuevo usuario puede autenticarse
Prioridad: Alta

ID: REQ-46
#NOTE - Nombre: Modificar usuarios existentes
Descripción: El sistema permitirá modificar información de usuarios existentes incluyendo nombres o roles asignados.
Entradas:
- Información actualizada de usuario
Proceso:
- Actualizar datos de usuario
- Mantener asignaciones de roles
Salidas:
- Usuario actualizado
Precondiciones:
- Permisos de administrador
Postcondiciones:
- Información de usuario actual
Prioridad: Media

ID: REQ-47
#NOTE - Nombre: Desactivar cuentas de usuario
Descripción: El sistema permitirá desactivar cuentas de usuario para impedir futuros inicios de sesión pero mantener registro histórico.
Entradas:
- Usuario a desactivar
Proceso:
- Marcar como inactivo
- Prevenir autenticación
Salidas:
- Usuario desactivado
Precondiciones:
- Permisos de administrador
Postcondiciones:
- Usuario no puede autenticarse
Prioridad: Alta

ID: REQ-48
#NOTE - Nombre: Almacenar contraseñas de forma segura
Descripción: El sistema hasheará y salteará contraseñas de forma segura usando algoritmos como bcrypt antes de almacenarlas.
Entradas:
- Contraseña en texto plano
Proceso:
- Hashear con sal usando bcrypt
- Almacenar hash
Salidas:
- Almacenamiento seguro de contraseña
Precondiciones:
- Contraseña proporcionada
Postcondiciones:
- Contraseña almacenada de forma segura
Prioridad: Alta

ID: REQ-49
#NOTE - Nombre: Gestionar roles de usuario
Descripción: El sistema permitirá crear, ver, modificar y eliminar roles para definir perfiles de acceso.
Entradas:
- Definiciones de roles
Proceso:
- Crear/modificar/eliminar roles
- Asignar permisos
Salidas:
- Roles configurados
Precondiciones:
- Permisos de administrador
Postcondiciones:
- Roles disponibles para asignación
Prioridad: Alta

ID: REQ-50
#NOTE - Nombre: Asignar permisos a roles
Descripción: El sistema permitirá asignar permisos específicos a roles para control granular de qué puede hacer cada perfil.
Entradas:
- Mapeos de rol y permiso
Proceso:
- Asociar permisos con roles
Salidas:
- Asignaciones de permisos
Precondiciones:
- Roles existen
Postcondiciones:
- Roles tienen permisos definidos
Prioridad: Alta

ID: REQ-51
#NOTE - Nombre: Verificar permisos de usuario
Descripción: Los microservicios verificarán permisos de usuario después de validar token y denegarán acceso si no tienen permiso requerido.
Entradas:
- Roles de usuario y acción solicitada
Proceso:
- Verificar permisos de rol
- Permitir/denegar acción
Salidas:
- Resultado de autorización
Precondiciones:
- Usuario autenticado
Postcondiciones:
- Acciones autorizadas permitidas
Prioridad: Alta

ID: REQ-52
#NOTE - Nombre: Cambiar contraseña propia
Descripción: El sistema permitirá cambiar contraseña propia proporcionando contraseña actual y nueva.
Entradas:
- Contraseña actual y nueva
Proceso:
- Verificar contraseña actual
- Actualizar a nueva contraseña
Salidas:
- Contraseña cambiada
Precondiciones:
- Usuario autenticado
Postcondiciones:
- Nueva contraseña activa
Prioridad: Media

ID: REQ-53
#NOTE - Nombre: Restablecer contraseñas olvidadas
Descripción: Los administradores podrán restablecer contraseñas de usuarios olvidadas asignando contraseña temporal.
Entradas:
- Usuario a restablecer
Proceso:
- Generar contraseña temporal
- Requerir cambio en próximo inicio de sesión
Salidas:
- Contraseña temporal proporcionada
Precondiciones:
- Permisos de administrador
Postcondiciones:
- Usuario puede recuperar acceso
Prioridad: Baja

#!SECTION

-------------------------

#SECTION - Monitoreo y observabilidad

ID: REQ-54
#NOTE - Nombre: Generar reportes del catálogo
Descripción: El sistema permitirá generar reportes del catálogo incluyendo listas por categoría, productos con ofertas activas y estadísticas generales.
Entradas:
- Tipo de reporte seleccionado
Proceso:
- Consultar datos relevantes del catálogo
- Generar reporte
Salidas:
- Reporte en formato especificado
Precondiciones:
- Usuario con permisos apropiados
Postcondiciones:
- Reporte disponible para visualización/impresión
Prioridad: Media



ID: REQ-55
#NOTE - Nombre: Aletas por stock mínimo
Descripción: El sistema permitirá establecer umbrales de stock mínimo y alertará cuando existencias bajen de ese nivel.
Entradas:
- Umbrales de stock mínimo por productoEntity
Proceso:
- Monitorear niveles de stock
- Generar alertas cuando se rompen umbrales
Salidas:
- Notificaciones de alerta
Precondiciones:
- Umbrales configurados
Postcondiciones:
- Alertas enviadas a usuarios designados
Prioridad: Media


ID: REQ-57
#NOTE - Reportes de caducicad
Descripción: El sistema generará reportes diarios automáticos de productos que caducan en próximos 7 días, 3 días y productos vencidos.
Entradas:
- Umbrales de alerta (días)
Proceso:
- Monitorear fechas de caducidad
- Generar alertas diarias
Salidas:
- Notificaciones de caducidad
Precondiciones:
- Productos con fechas de caducidad
Postcondiciones:
- Alertas enviadas a gerencia
Prioridad: Alta


ID: REQ-57
#NOTE - Nombre: Reportes de ventas por período
Descripción: El sistema permitirá generar reportes de ventas por períodos especificados.
Entradas:
- Rango de fechas
Proceso:
- Agregar datos de ventas
- Generar reportes
Salidas:
- Reporte de ventas por período
Precondiciones:
- Datos de ventas disponibles
Postcondiciones:
- Reportes generados
Prioridad: Media



ID: REQ-58
#NOTE - Nombre: Reportes diarios de devoluciones
Descripción: El sistema generará reportes diarios consolidados de devoluciones incluyendo productos, motivos, montos y productos clasificados como merma.
Entradas:
- Rango de fechas (diario)
Proceso:
- Agregar datos de devoluciones
- Generar reporte consolidado
Salidas:
- Reporte diario de devoluciones
Precondiciones:
- Devoluciones procesadas
Postcondiciones:
- Reporte disponible para análisis
Prioridad: Media


ID: REQ-59
#NOTE - Nombre: Monitoreo del rendimiento del sistema
Descripción: El sistema permitirá supervisar continuamente su rendimiento mediante la recopilación y registro de métricas clave como uso de CPU, memoria, tiempos de respuesta, disponibilidad de servicios y carga de usuarios. Los datos recolectados permitirán detectar incidencias, optimizar el funcionamiento y garantizar la estabilidad operativa.
Entradas:
- Parámetros de rendimiento definidos
- Intervalo de recolección de métricas
Proceso:
- Registrar y almacenar métricas de rendimiento
- Analizar los datos recolectados
- Generar alertas al superar umbrales críticos
Salidas:
- Registros de métricas históricas
- Alertas y reportes de rendimiento
Precondiciones:
- Sistema en ejecución
- Permisos adecuados para acceder a métricas
Postcondiciones:
- Métricas disponibles para consulta y análisis
- Alertas generadas ante anomalías detectadas
Prioridad: Medio


#!SECTION