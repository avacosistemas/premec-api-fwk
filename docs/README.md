# Documentación funcional

Esta carpeta mantiene un inventario de funcionalidades observado en el código de `ws-rest` y una lista para darles seguimiento.

- [Inventario funcional](funcionalidades.md): módulos, acciones y evidencia en el código.
- [Seguimiento](seguimiento.md): estado documental y pendientes de validación funcional/operativa.
- [Flujo de recibos del usuario](recibos.md): listado SAP y paginado desde base, consulta de pendientes de firma y resolución del usuario autenticado.

## Alcance y criterio

El repositorio contiene una aplicación backend Java 11 empaquetada como WAR, basada en Spring MVC/Spring Security, con integraciones con SAP, PostgreSQL y SQL Server. La inspección cubrió controladores REST, servicios, jobs Quartz y scripts SQL. La interfaz web/cliente consumidor no forma parte de este repositorio.

Los estados indican el nivel de documentación, no el estado de despliegue en producción. Una ruta listada en un controlador demuestra que está declarada en el código; no demuestra que esté habilitada por configuración, publicada o usada actualmente. Las rutas se documentan sin prefijo de contexto de despliegue.
