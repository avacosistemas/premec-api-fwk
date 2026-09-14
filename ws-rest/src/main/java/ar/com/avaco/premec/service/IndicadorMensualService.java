package ar.com.avaco.premec.service;

import java.util.List;

import ar.com.avaco.premec.dto.RegistroInformeMensualEmpleadoDTO;
import ar.com.avaco.premec.dto.RegistroInformeMensualEmpleadoIndividualDTO;
import ar.com.avaco.premec.dto.RegistroInformeMensualGeneralDTO;

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

}
