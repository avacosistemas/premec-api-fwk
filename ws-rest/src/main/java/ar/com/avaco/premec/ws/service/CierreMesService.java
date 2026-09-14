package ar.com.avaco.premec.ws.service;

import java.util.List;

import ar.com.avaco.premec.ws.dto.actividad.RegistroPreviewEmpleadoMensualDTO;

public interface CierreMesService {

	List<RegistroPreviewEmpleadoMensualDTO> getRegistrosCierre(String mes, String anio);

	void cerrarMes(List<RegistroPreviewEmpleadoMensualDTO> cierre, String anio, String mes);

	RegistroPreviewEmpleadoMensualDTO getRegistrosCierreIndividual(String mes, String anio, String usuario);

	RegistroPreviewEmpleadoMensualDTO getRegistrosCierreGeneral(String mes, String anio);

	RegistroPreviewEmpleadoMensualDTO getRegistrosCierreGrupoEmpleado(String mes, String anio, Long grupoEmpleado);

	List<RegistroPreviewEmpleadoMensualDTO> getRegistrosCierreEmpleados(String mes, String anio, String idsEmpleados);

}
