package ar.com.avaco.premec.ws.service;

import java.util.List;

import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.ws.dto.RegistroHorasMaquinaDTO;
import ar.com.avaco.premec.ws.dto.RegistroInformeServicioDTO;
import ar.com.avaco.premec.ws.dto.RegistroMonitorDTO;
import ar.com.avaco.premec.ws.dto.actividad.ActividadReporteDTO;
import ar.com.avaco.premec.ws.dto.actividad.ActividadTarjetaDTO;

public interface ActividadEPService {

	List<ActividadTarjetaDTO> getActividades(String fecha, String usuarname) throws Exception;

	List<ActividadReporteDTO> getActividadesReporte() throws Exception;

	void marcarEnviado(Long idActividad) throws Exception;

	List<RegistroMonitorDTO> getActividadesMonitor(String skip) throws Exception;

	RegistroInformeServicioDTO getActividadesServiceCall(Long serviceCallId) throws Exception;

	ActividadReporteDTO getActividadReporte(Long actividadId) throws Exception;

	List<RegistroHorasMaquinaDTO> getHorasMaquinaReporte(String internalSerialNum) throws SapBusinessException;

	List<ActividadTarjetaDTO> getActividadesCrossJoin(String fecha, String username) throws SapBusinessException;

}
