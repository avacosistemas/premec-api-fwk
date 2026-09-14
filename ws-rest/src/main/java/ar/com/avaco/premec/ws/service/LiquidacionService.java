package ar.com.avaco.premec.ws.service;

import java.io.IOException;
import java.util.List;

import ar.com.avaco.premec.ws.dto.employee.FueraConvenio;
import ar.com.avaco.premec.ws.dto.employee.Jornal;
import ar.com.avaco.premec.ws.dto.employee.Mensual;

public interface LiquidacionService {

	void generarExcel(String periodo, List<FueraConvenio> fueraConvenio, List<Mensual> mensuales, List<Jornal> jornales)
			throws IOException;

}
