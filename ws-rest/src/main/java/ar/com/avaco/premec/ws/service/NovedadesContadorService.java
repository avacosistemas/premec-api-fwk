package ar.com.avaco.premec.ws.service;

import ar.com.avaco.premec.ws.dto.employee.PlantillaData;

public interface NovedadesContadorService {

	PlantillaData getRegistrosCierre(String mes, String anio);

}
