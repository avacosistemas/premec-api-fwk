package ar.com.avaco.premec.ws.service;

import java.io.IOException;
import java.util.List;

import ar.com.avaco.premec.dto.EmpleadoFichados;
import ar.com.avaco.premec.ws.dto.actividad.ActividadEmpleadoFechaHoras;

public interface NovedadesFichadoService {

	List<EmpleadoFichados> parsearExcelNovedadesFichado(byte[] archivoBytes) throws IOException;

	void enviarFichados(List<EmpleadoFichados> registros);

}
