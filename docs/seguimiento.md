# Seguimiento de funcionalidades

Registro inicial. Actualizar este archivo cuando se valide el comportamiento con responsables funcionales o al cambiar el código.

Estados: **Inventariada** = localizada en código; **En validación** = contrastando reglas/uso real; **Validada** = confirmada por responsable funcional y operación; **Revisar** = hay dudas o deuda que requiere seguimiento.

| ID | Área | Estado | Próximo paso para seguimiento |
|---|---|---|---|
| F-01 | Formularios de servicio | Inventariada | Confirmar estados de cola, reintentos SAP, adjuntos/fotos y validaciones obligatorias. |
| F-02 | Actividades y monitor | Inventariada | Documentar significado de estados, filtros y origen de datos SAP. |
| F-03 | Grupos y checklist | Inventariada | Validar reglas de asignación, borrado y efecto de cambios en formularios. |
| F-04 | Clientes | Inventariada | Aclarar origen de clientes y comportamiento del endpoint de notificación. |
| F-05 | Problemas de máquina | Inventariada | Identificar ciclo de vida, responsables y vínculo con reclamos/Service Calls. |
| F-06 | Reclamos | Inventariada | Confirmar criterios de vencimiento, estadísticas y destinatarios de avisos. |
| F-07 | Reportes de servicio | Inventariada | Acordar plantilla, adjuntos, ciclo de envío y estados del reporte. |
| F-08 | Indicadores mensuales | Revisar | Validar resultados con los filtros de categoría/estado y exclusión de S/V aplicados en cierre e indicadores. |
| F-09 | Cierre mensual y novedades | Revisar | Validar reglas de cierre, reversión y período bloqueado; revisar el manejo de errores que hoy puede reportar éxito aunque el procesamiento SAP haya fallado parcialmente. |
| F-10 | Liquidaciones y fichado | Inventariada | Documentar formato de planillas, validaciones y sistema receptor. |
| F-11 | Recibos de sueldo | Inventariada | Confirmar permisos, firma, privacidad, formatos y rechazo. |
| F-12 | Timesheets SAP | Inventariada | Localizar invocadores y describir ciclo de creación/edición en SAP. |
| F-13 | Repuestos | Inventariada | Confirmar filtros por usuario/almacén y disponibilidad de stock. |
| F-14 | Usuarios Premec | Inventariada | Revisar altas/bajas, sincronización y roles/permisos asignados. |
| F-15 | Automatizaciones y notificaciones | Inventariada | Verificar triggers, horarios, zona horaria, reintentos y monitoreo del entorno. |
| F-16 | Correo | Inventariada | Identificar uso del endpoint, plantillas y configuración de SMTP por ambiente. |
| F-17 | Control de horas de máquina | Inventariada | Definir reglas de exceso/reset, auditoría y relación con validación del formulario. |

## Próxima revisión sugerida

1. Revisar `spring/applicationContext-security.xml` y documentar autorización por endpoint.
2. Inspeccionar triggers Quartz/configuración para distinguir jobs activos de clases disponibles.
3. Contrastar el inventario con usuarios funcionales y con la API desplegada; registrar rutas obsoletas o clientes consumidores.
4. Completar parámetros y ejemplos de request/response para las operaciones priorizadas.

## Hallazgos de análisis: cierre mensual

- `GET /cierremespreview` arma el rango desde el primer hasta el último día del mes y consulta actividades/horas en SAP. Las exclusiones para el cálculo de horas netas se inyectan desde `exclusiones.actividades.calculo.horas.netas`.
- El preview agrupa por empleado SAP. Incluye horas facturables, ociosas, fichadas, efectividad, conteos de valoraciones, porcentaje de valoración, objetivo de actividades y cumplimiento del objetivo. `cantidadActividades` cuenta solo actividades valoradas (excluye `U_Valoracion IS NULL`, S/V); ese total también se usa para calcular el cumplimiento del objetivo. `cantSV` se mantiene como indicador informativo.
- `POST /cierremespreview` recibe la lista completa de registros desde el cliente. `cerrarMes` busca timesheets de SAP del período y, para cada registro, toma el primero cuyo `UserId` coincide con `usuarioSap`.
- Para cada coincidencia, `RegistroPreviewEmpleadoMensualDTO.generarMapUpdate()` actualiza los UDF `U_cumplimientoobjetivo`, `U_viaticos`, `U_adelanto`, `U_prestamo`, `U_premio`, `U_porcentajevaloracion`, `U_porcentajeefectividad`, `U_cantidadactividades` y `U_aumento`.
- Si no encuentra timesheet, solo escribe logs y continúa. Además, el controlador captura excepciones, imprime el stack trace y devuelve `JSONResponse.OK`; revisar cómo detectar y comunicar cierres parciales/fallidos.
- El DTO tiene campos de carga manual (viáticos, adelanto, préstamo, premio, gratificaciones/aumentos y novedades), pero `novedades` no se incluye en el mapa de actualización. `premioAsistencia` se convierte con `Boolean.valueOf`, por lo que solo el texto `true` produce `SI`; cualquier otro valor produce `NO`.
- Confirmar con negocio si debe actualizarse un único timesheet por persona y mes, qué sucede ante duplicados/ausentes, si los campos recibidos por el POST se validan contra el preview y si deben actualizarse también `novedades` u otros campos.
- Ajustes aplicados: el total de actividades del cierre ya no suma registros sin valoración (`U_Valoracion IS NULL`); el subquery filtra `U_categoria = 'Mantenimiento-reparacion'` y solo considera registros donde `U_estado` no es nulo ni igual a `Pendiente`. Las comparaciones convierten los campos de texto a `NVARCHAR(MAX)` por su tipo `ntext`. Validar el resultado funcional con datos representativos.
- Se extendió el mismo criterio a `IndicadorMensualServiceImpl`: agregados general, grupal/individual y consultas de detalle de valoraciones negativas filtran la categoría y excluyen estados nulos o `Pendiente`. En agregados, `cantidadActividades` excluye `U_Valoracion IS NULL`; `cantSV` se conserva como campo informativo.

## Cómo mantenerlo

Al agregar o cambiar una ruta, actualizar la fila de área correspondiente y la evidencia en `funcionalidades.md`. Si la modificación altera una regla, anotar fecha, decisión y responsable en este archivo. Los estados deben reflejar evidencia, no suposiciones.
