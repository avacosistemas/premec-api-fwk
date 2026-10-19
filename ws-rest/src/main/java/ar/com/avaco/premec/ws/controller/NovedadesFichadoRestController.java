package ar.com.avaco.premec.ws.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.fwk.core.exception.ErrorValidationException;
import ar.com.avaco.premec.dto.EmpleadoFichadoDTO;
import ar.com.avaco.premec.dto.EmpleadoFichados;
import ar.com.avaco.premec.ws.dto.ArchivoDTO;
import ar.com.avaco.premec.ws.service.NovedadesFichadoService;

@Controller
public class NovedadesFichadoRestController {

	@Value(value = "${path.recibo.prueba}")
	private String pathPruebas;

	private NovedadesFichadoService service;

	@RequestMapping(value = "/procesarExcel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> procesarExcelFichado(@RequestBody ArchivoDTO archivo) throws IOException {
		JSONResponse response = new JSONResponse();
		try {
			List<EmpleadoFichados> parsearExcelNovedadesFichado = service
					.parsearExcelNovedadesFichado(archivo.getFile());
			response.setData(parsearExcelNovedadesFichado);
			response.setStatus(JSONResponse.OK);
		} catch (ErrorValidationException e) {

			Map<String, String> erroresOrdenados = new TreeMap<String, String>((a, b) -> {
				String[] partesA = a.split(" ", 2);
				String[] partesB = b.split(" ", 2);

				int filaA = Integer.parseInt(partesA[0]);
				int filaB = Integer.parseInt(partesB[0]);

				if (filaA != filaB) {
					return Integer.compare(filaA, filaB);
				}

				if (partesA.length == 1 && partesB.length == 1) {
					return 0;
				}

				if (partesA.length == 1) {
					return -1;
				}

				if (partesB.length == 1) {
					return 1;
				}

				return partesA[1].compareTo(partesB[1]);
			});

			erroresOrdenados.putAll(e.getErrors());

			response.setData(erroresOrdenados);
			response.setError("Se encontraron los siguientes errores");
			response.setStatus(JSONResponse.ERROR);
		}
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/enviarFichados", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> enviarFichados(@RequestBody EmpleadoFichadoDTO fichados) throws IOException {
		JSONResponse response = new JSONResponse();
		service.enviarFichados(fichados.getFichados());
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@Resource(name = "novedadesFichadoService")
	public void setService(NovedadesFichadoService service) {
		this.service = service;
	}

}
