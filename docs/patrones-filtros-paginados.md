# Filtros paginados en endpoints REST

Para listados GET paginados, usar como referencia `ReclamoRestController` y `ReclamoFilterDTO`:

- Crear un DTO de filtro que extienda `SortPageDTO` y agregar los campos funcionales del filtro.
- Recibir el DTO directamente como argumento del controller para que Spring lo complete desde los query params.
- Aplicar valores por defecto de página y tamaño en el controller/servicio.
- Devolver `PageResponse` en `JSONResponse`, informando página, tamaño y total de registros.
- Aplicar ordenamiento mediante columnas permitidas (lista blanca), nunca concatenar libremente el índice recibido.
- Implementar paginación SQL con `OFFSET ... FETCH NEXT ...` y obtener el total con `COUNT(*) OVER()`.
