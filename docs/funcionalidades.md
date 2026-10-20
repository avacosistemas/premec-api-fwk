# Inventario de funcionalidades

Inventario inicial basado en el código del backend. Referencias relativas a `ws-rest/src/main/java/ar/com/avaco/premec/`.

| ID | Área | Funcionalidad observada | Entradas REST representativas | Evidencia principal |
|---|---|---|---|---|
| F-01 | Formularios de servicio | Guarda formularios de visita/servicio, procesa formularios pendientes hacia SAP, permite retirar adjuntos de la cola y validar horas de máquina. | `POST /formulario`, `POST /formulariosEnviarJsonSap`, `GET /quitarArchivoColaEnvio`, `GET /validarHorasMaquina` | `ws/controller/FormularioRestController.java`; `ws/service/FormularioEPServiceImpl.java` |
| F-02 | Actividades y monitor | Consulta actividades asociadas a Service Calls, tarjetas de actividad por fecha, monitor, datos de reporte y horas de máquina. | `GET /actividadesPorServiceCall`, `/monitor`, `/actividades`, `/actividadesReporte`, `/horasMaquinaReporte` | `ws/controller/ActividadRestController.java`; `ws/service/ActividadEPServiceImpl.java` |
| F-03 | Grupos y checklist | Administra grupos de empleados, grupos de tipo de actividad, ítems de checklist y consulta usuarios asociados a grupos. | `/grupoEmpleado`, `/grupoTipoActividad`, `/itemChecklistGrupo` (GET/POST/PUT/DELETE según recurso) | Controladores homónimos; `domain/GrupoEmpleado.java`, `domain/GrupoTipoActividad.java`, `domain/ItemChecklistGrupo.java` |
| F-04 | Clientes | Lista clientes, busca opciones para combo, crea registros locales y dispara notificación. | `GET /cliente`, `GET /cliente/combo`, `GET /cliente/nofiticacion`, `POST /cliente` | `ws/controller/ClienteRestController.java`; `ws/service/ClienteEPServiceImpl.java` |
| F-05 | Problemas de máquina | Mantiene tipos de problema, consulta problemas por tipo y registra/gestiona problemas de máquina. | `/tipoProblemaMaquina`, `/problemaMaquina` | Controladores y servicios `TipoProblemaMaquina*`, `ProblemaMaquina*`; entidades en `domain/` |
| F-06 | Reclamos | Consulta reclamos y actividades de Service Call, obtiene equipos de cliente, expone estadísticas de máquina parada por máquina/cliente y permite enviar avisos de vencimiento. | `GET /reclamo`, `/reclamo/actividades`, `/customer/equipment/{cuit}`, `/enviarVencimientos`; `POST /reclamo/estadisticas/maquina/maquina-parada`, `/reclamo/estadisticas/cliente/maquina-parada` | `ws/controller/ReclamoRestController.java`; `ws/service/ReclamoEPServiceImpl.java`, `ReclamoEstadisticasEPServiceImpl.java` |
| F-07 | Reportes de servicio | Genera/descarga reportes asociados a actividades y registra su envío. | `GET /enviarreporte`, `/descargarreporte`, `/marcarenviada` | `ws/controller/ReporteRestController.java`; `ws/service/ReporteEPServiceImpl.java`; `reporte/InformeBuilder*.java` |
| F-08 | Indicadores mensuales | Consulta indicadores generales, por grupo de empleado y por empleado, incluyendo valoraciones negativas. | `/indicadorMensual*` | `ws/controller/IndicadorMensualController.java`; `ws/service/ActivityServiceImpl.java` |
| F-09 | Cierre mensual y novedades | Previsualiza métricas mensuales de empleados y, al confirmar, actualiza campos de cierre en timesheets existentes de SAP. También consulta novedades de contador y genera la planilla Excel correspondiente. | `GET/POST /cierremespreview`, `GET /novedadescontadorpreview`, `POST /enviarnovedadescontador` | `ws/controller/CierreMesRestController.java`, `NovedadesContadorController.java`; `ws/service/CierreMesServiceImpl.java`, `NovedadesContadorServiceImpl.java`, `LiquidacionServiceImpl.java` |
| F-10 | Liquidaciones y fichado | Genera liquidaciones/planillas desde los datos recibidos. Importa novedades de fichado desde Excel con validación por fila y permite enviarlas al sistema integrado. | `POST /enviarLiquidacion`, `/procesarExcel`, `/enviarFichados` | `ws/controller/LiquidacionController.java`, `NovedadesFichadoRestController.java`; `ws/service/LiquidacionServiceImpl.java`, `NovedadesFichadoServiceImpl.java` |
| F-11 | Recibos de sueldo | Procesa archivos de recibos, aprueba/rechaza, lista por usuario o con filtros, descarga y firma PDF. | `POST /procesarRecibos`, `/aprobarRechazarRecibos`; `GET /listarRecibos*`, `/descargarRecibo`, `/firmarRecibo` | `ws/controller/ReciboSueldoRestController.java`; `ws/service/ReciboSueldoServiceImpl.java` |
| F-12 | Partes de horas SAP (Timesheet) | Crea, consulta y actualiza timesheets y adjuntos; lista registros por usuario SAP y período. | No se identificó controlador propio en el inventario inicial | `ws/service/TimeSheetServiceImpl.java`, `AttachmentServiceImpl.java`; DTOs `ws/dto/timesheet/` |
| F-13 | Repuestos | Consulta repuestos por almacén asociado al usuario. | `GET /repuestos` | `ws/controller/RepuestoRestController.java`; `ws/service/RepuestoEPServiceImpl.java` |
| F-14 | Usuarios Premec | CRUD de usuarios de la aplicación y búsqueda por usuario. | `GET/POST/PUT/DELETE /usuarioPremec` | `ws/controller/UsuarioPremecRestController.java`; `ws/service/UsuarioPremecEPServiceImpl.java` |
| F-15 | Automatizaciones y notificaciones | Ejecuta envío de formularios SAP, reporte diario, notificaciones de reclamos y vencimientos. El controlador de jobs puede iniciar/detener y consultar dos jobs. | `/startStopEnvioFormularioSapJob`, `/isEnvioFormularioSapJobRunning`, `/startStopReporteDiarioJob`, `/isReporteDiarioJobRunning` | `jobs/Job*.java`; `ws/controller/JobRestController.java`; servicios `Notificacion*ServiceImpl.java` |
| F-16 | Correo | Expone envío de correo de prueba/operativo mediante servicio SMTP. | `GET /sendMail` | `ws/controller/EnvioMailRestController.java`; `applicationContext-mail.xml` |
| F-17 | Control de horas de máquina | Consulta registros de exceso/reset y mantiene datos de horas de máquina usados por formularios y reportes. | `GET /horasMaquinaExcesoReseteo` | `ws/controller/RegistroHorasMaquinaExcedidaReseteoRestController.java`; entidades `domain/RegistroHorasMaquina*.java` |

## Integraciones y arquitectura observadas

- SAP Business One vía servicios REST/OData; utilidades de integración y DTOs se encuentran en `ws/service`, `sap/` y `ws/dto`.
- SQL Server y PostgreSQL aparecen como dependencias/configuraciones de conexión. Los scripts de `script-bbdd/` incluyen creación de esquema de seguridad y datos de inicialización.
- Spring MVC/Security, Hibernate/JPA, Quartz, JasperReports y bibliotecas para PDF/Excel aparecen en `pom.xml` y archivos de configuración.
- La seguridad se conecta al filtro Spring Security en `web.xml`; los permisos efectivos deben revisarse en `spring/applicationContext-security.xml` antes de describir acceso por perfil.

## Límites de esta lectura

1. No se documentaron contratos completos de request/response, reglas de negocio línea por línea ni permisos efectivos.
2. Algunas rutas usan nombres singulares o inconsistentes (`/cliente/nofiticacion`, por ejemplo); se copiaron del código y no se corrigieron.
3. Los jobs usan Quartz; para confirmar frecuencia y activación hay que revisar la configuración de triggers y el entorno desplegado.
4. Algunas funcionalidades se observan en servicios sin ruta REST identificada; pueden ejecutarse desde jobs u otros componentes.
