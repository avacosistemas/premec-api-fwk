# Listado de recibos del usuario

## Endpoint

`GET /listarRecibosPorUsuario` devuelve los recibos asociados al usuario autenticado. No recibe filtros ni paginación en la petición. La ruta se documenta sin el prefijo de contexto del despliegue.

El controlador delega en `ReciboSueldoServiceImpl.listarRecibosPorUsuario()` y responde con un `JSONResponse`: `status` indica el resultado y `data` contiene la lista de `RegistroReciboPorUsuarioDTO`.

## Cómo se determina el usuario

1. El servicio obtiene el `username` del principal de Spring Security.
2. Busca el registro `UsuarioPremec` por ese username y obtiene su `usuariosap`.
3. Si el usuario no tiene asociado un SAP ID, la consulta falla con un error de validación.
4. Convierte el SAP ID a número y consulta los timesheets de ese empleado.

No se recibe un ID de empleado del cliente; el alcance de la consulta se deriva de la sesión autenticada.

## Consulta a SAP

`TimeSheetServiceImpl.listTimeSheetByUsuarioSap()` consulta `ProjectManagementTimeSheet` en SAP con el filtro `UserID eq <SAP ID>` y `AttachmentEntry ne null`. Expande `Attachments2` y ordena los timesheets por `DateFrom desc`.

El servicio transforma cada línea de `Attachments2.Lines` en un recibo. Por eso, un timesheet puede producir varias filas. Para cada fila:

- `attachmentEntry` proviene del timesheet y `absEntry` de la línea del attachment.
- `year` y `month` se extraen de `DateFrom`; `monthString` se genera en español.
- `freeText` se separa por el primer `|`: antes queda `tipo` y después `descripcion`. Si no hay separador, la descripción queda vacía.
- `filePath` se forma con `TargetPath`, `FileName` y `FileExtension`.
- `firmado` es `true` cuando `Firmado` vale `tYES`; en los demás casos es `false`.
- `lineNum` conserva el número de línea del attachment.

La API devuelve todas las filas encontradas para el SAP ID. Los filtros de año, mes, tipo y referencia de la pantalla `/recibos` no se envían a este endpoint: los aplica el front sobre la respuesta.

## Referencias en el código

- `ws/controller/ReciboSueldoRestController.java`: declaración del endpoint y envoltorio de la respuesta.
- `ws/service/ReciboSueldoServiceImpl.java`: resolución del usuario SAP y armado de los DTO de recibo.
- `ws/service/TimeSheetServiceImpl.java`: consulta de timesheets y expansión de attachments.
- `ws/dto/timesheet/RegistroReciboPorUsuarioDTO.java`: campos que devuelve cada fila.

## Listado paginado desde la base

`GET /listarRecibosPorUsuarioDB` es una ruta adicional para el usuario autenticado. Mantiene la resolución de usuario mediante el principal de Spring Security y el SAP ID asociado, pero lee los recibos desde SQL Server (`OTSH` y `ATC1`) y no invoca el servicio de timesheets de SAP. La ruta original `/listarRecibosPorUsuario` permanece disponible sin cambios.

Acepta `page` (base cero), `pageSize` (por defecto 10, máximo 100), `year`, `month`, `tipo` y `attachmentEntry`. Año y mes se aplican a `DateFrom`; `tipo` coincide con el texto anterior al primer `|` de `FreeText`; `attachmentEntry` busca por coincidencia parcial. La respuesta conserva el formato paginado `JSONResponse`, con la lista en `data` y los metadatos en `page`.

El conteo se ejecuta por separado de la consulta de filas para que también se informe el total cuando una página solicitada queda vacía. La ordenación es por fecha descendente y línea del attachment. La consulta del nuevo método se restringe al `UserID` SAP del usuario autenticado.

## Recibos pendientes de firma

`GET /listarRecibosPendientesFirma` devuelve, sin paginación ni filtros de período o tipo, todos los recibos cuyo `EDocSign` no sea `Y` para el SAP ID asociado al usuario autenticado. La respuesta es una lista de `RegistroReciboPorUsuarioDTO` en `JSONResponse.data`. El listado incluye la información necesaria para previsualizar y firmar cada recibo mediante las rutas existentes `descargarRecibo` y `firmarRecibo`.
