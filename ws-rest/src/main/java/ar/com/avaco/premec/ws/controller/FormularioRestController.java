package ar.com.avaco.premec.ws.controller;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.premec.dto.UsuarioPremecDTO;
import ar.com.avaco.premec.ws.dto.formulario.FormularioDTO;
import ar.com.avaco.premec.ws.service.FormularioEPService;
import ar.com.avaco.premec.ws.service.UsuarioPremecEPService;

@RestController
public class FormularioRestController {

	@Autowired
	private FormularioEPService formularioEPService;

	@Autowired
	private UsuarioPremecEPService usuarioPremecEPService;
	
	@RequestMapping(value = "/formulario", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> saveFormulario(@RequestBody FormularioDTO formularioDTO) {
		JSONResponse response = new JSONResponse();
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		UsuarioPremecDTO usuario = usuarioPremecEPService.findByUsername(name);
		try {
			this.formularioEPService.grabarFormulario(formularioDTO, usuario.getUsuariosap().toString());
			response.setStatus(JSONResponse.OK);
		} catch (Exception e) {
			response.setStatus(JSONResponse.ERROR);
			response.setData(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/formulariosEnviarJsonSap", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> formulariosEnviar() {
		JSONResponse response = new JSONResponse();
		try {
			this.formularioEPService.enviarFormulariosFromFiles();
			response.setStatus(JSONResponse.OK);
		} catch (Exception e) {
			response.setStatus(JSONResponse.ERROR);
			response.setData(e.getLocalizedMessage());
			e.printStackTrace();
		}
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/quitarArchivoColaEnvio", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> quitarArchivoColaEnvio(@RequestParam long actividadId) {
		JSONResponse response = new JSONResponse();
		try {
			this.formularioEPService.quitarArchivoColaEnvio(actividadId);
			response.setData("Archivo de actividad " + actividadId + " quitado de cola de envios con exito");
			response.setStatus(JSONResponse.OK);
		} catch (Exception e) {
			response.setStatus(JSONResponse.ERROR);
			response.setData(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/validarHorasMaquina", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> validarHorasMaquina(@RequestParam long serviceCallId,
			@RequestParam int horasMaquina) {
		JSONResponse response = new JSONResponse();
		try {
			this.formularioEPService.validarHorasMaquina(serviceCallId, horasMaquina);
			response.setStatus(JSONResponse.OK);
		} catch (Exception e) {
			response.setStatus(JSONResponse.ERROR);
			response.setData(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@Resource(name = "formularioEPService")
	public void setService(FormularioEPService service) {
		this.formularioEPService = service;
	}
}
