package ar.com.avaco.premec.service;

import java.util.List;

import ar.com.avaco.premec.dto.RegistroInformeMensualEmpleadoDTO;
import ar.com.avaco.premec.dto.RegistroInformeMensualEmpleadoIndividualDTO;
import ar.com.avaco.premec.dto.RegistroInformeMensualGeneralDTO;
import ar.com.avaco.premec.ws.dto.actividad.ActivityValoracionSapDTO;

public interface IndicadorMensualService {

	RegistroInformeMensualGeneralDTO getIndicadorMensualGeneral(String mes, String anio);

	RegistroInformeMensualEmpleadoDTO getIndicadorMensualIndividual(String mes, String anio, String username);

	List<RegistroInformeMensualEmpleadoIndividualDTO> getIndicadoresGrupoEmpleado(String mes, String anio,
			Long idGrupoEmpleado);

	List<RegistroInformeMensualEmpleadoIndividualDTO> getIndicadoresEmpleados(String mes, String anio,
			String idsEmpleados);

	RegistroInformeMensualEmpleadoIndividualDTO getIndicadoresEmpleadosAgrupado(String mes, String anio,
			String idsEmpleados);

	RegistroInformeMensualEmpleadoIndividualDTO getIndicadoresGrupoEmpleadoAgrupado(String mes, String anio,
			Long idGrupoEmpleado);

	List<ActivityValoracionSapDTO> getActividadesValoracionPorEmpleados(List<Long> idsEmpleados, Integer mes, Integer anio);

	List<ActivityValoracionSapDTO> getActividadesValoracion(String username, Integer mes, Integer anio);
	
	List<ActivityValoracionSapDTO> getActividadesValoracion(Integer mes, Integer anio);

	List<ActivityValoracionSapDTO> getActividadesValoracionPorGrupoEmpleado(Long idGrupoEmpleado, Integer mes, Integer anio);

}
