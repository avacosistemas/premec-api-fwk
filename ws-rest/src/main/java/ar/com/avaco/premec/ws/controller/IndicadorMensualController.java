package ar.com.avaco.premec.ws.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.premec.dto.RegistroInformeMensualEmpleadoDTO;
import ar.com.avaco.premec.dto.RegistroInformeMensualEmpleadoIndividualDTO;
import ar.com.avaco.premec.dto.RegistroInformeMensualGeneralDTO;
import ar.com.avaco.premec.service.IndicadorMensualService;

@Controller
public class IndicadorMensualController {

	@Autowired
	private IndicadorMensualService indicadorMensualService;

	@RequestMapping(value = "/indicadorMensual", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> indicadorMensual(@RequestParam String mes, @RequestParam String anio) {
		JSONResponse response = new JSONResponse();
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		RegistroInformeMensualEmpleadoDTO preview = this.indicadorMensualService.getIndicadorMensualIndividual(mes, anio, username);
		response.setData(preview);
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/indicadorMensualGeneral", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> indicadorMensualGeneral(@RequestParam String mes, @RequestParam String anio) {
		JSONResponse response = new JSONResponse();
		RegistroInformeMensualGeneralDTO registro = this.indicadorMensualService.getIndicadorMensualGeneral(mes, anio);
		response.setData(registro);
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/indicadorMensualGrupoEmpleado", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> indicadorMensualGrupoEmpleado(@RequestParam String mes,
			@RequestParam String anio, @RequestParam Long idGrupoEmpleado, @RequestParam Boolean agrupado) {
		JSONResponse response = new JSONResponse();
		if (agrupado) {
			RegistroInformeMensualEmpleadoIndividualDTO indicadoresGrupoEmpleado = this.indicadorMensualService.getIndicadoresGrupoEmpleadoAgrupado(mes, anio, idGrupoEmpleado);
			response.setData(indicadoresGrupoEmpleado);
		} else {
			List<RegistroInformeMensualEmpleadoIndividualDTO> indicadoresGrupoEmpleado = this.indicadorMensualService.getIndicadoresGrupoEmpleado(mes, anio, idGrupoEmpleado);
			response.setData(indicadoresGrupoEmpleado);
		}
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/indicadorMensualEmpleados", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> indicadorMensualEmpleados(@RequestParam String mes, @RequestParam String anio,
			String idsEmpleados, @RequestParam Boolean agrupado) {
		JSONResponse response = new JSONResponse();
		if (agrupado) {
			RegistroInformeMensualEmpleadoIndividualDTO preview = this.indicadorMensualService.getIndicadoresEmpleadosAgrupado(mes, anio, idsEmpleados);
			response.setData(preview);
		} else {
			List<RegistroInformeMensualEmpleadoIndividualDTO> preview = this.indicadorMensualService.getIndicadoresEmpleados(mes, anio, idsEmpleados);
			response.setData(preview);
		}
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

}
