package ar.com.avaco.premec.ws.service;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ar.com.avaco.premec.ws.dto.employee.PlantillaData;

@Service("novedadesContadorService")
public class NovedadesContadorServiceImpl extends AbstractSapService implements NovedadesContadorService {

	@Value(value = "${exclusiones.actividades.calculo.horas.netas}")
	private String exclusionesActividadesCalculoHorasNetas;
	
	private ActivityService activityService;
	
	@Override
	public PlantillaData getRegistrosCierre(String mes, String anio) {
		PlantillaData previewNovedadesContador = this.activityService.getPreviewNovedadesContador(anio, mes); 
		return previewNovedadesContador;
	}

	@Resource
	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}
	
}
