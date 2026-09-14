package ar.com.avaco.premec.ws.controller;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.premec.ws.dto.actividad.RegistroPreviewEmpleadoMensualDTO;
import ar.com.avaco.premec.ws.service.CierreMesService;

@Controller
public class CierreMesRestController {

	private CierreMesService cierreMesService;

	@RequestMapping(value = "/cierremespreview", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> cierreMesPreview(@RequestParam String mes, @RequestParam String anio) {
		JSONResponse response = new JSONResponse();
		List<RegistroPreviewEmpleadoMensualDTO> preview = this.cierreMesService.getRegistrosCierre(mes, anio);
		response.setData(preview);
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/cierremespreview", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> cerrarMes(@RequestBody List<RegistroPreviewEmpleadoMensualDTO> cierre, @RequestParam String mes, @RequestParam String anio) throws IOException {
		JSONResponse response = new JSONResponse();
		try {
			this.cierreMesService.cerrarMes(cierre, anio, mes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@Resource(name = "cierreMesService")
	public void setCierreMesService(CierreMesService cierreMesService) {
		this.cierreMesService = cierreMesService;
	}

}
