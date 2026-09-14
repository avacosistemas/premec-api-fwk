package ar.com.avaco.premec.ws.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.fwk.core.component.dto.PageResponse;
import ar.com.avaco.premec.dto.ReclamoFilterDTO;
import ar.com.avaco.premec.sap.dto.CustomerReclamoStatsRequestDTO;
import ar.com.avaco.premec.sap.dto.EstadisticaClienteDTO;
import ar.com.avaco.premec.sap.dto.EstadisticaMaquinaDTO;
import ar.com.avaco.premec.sap.dto.MachineReclamoStatsRequestDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallActivityDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallReclamoListDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.sap.service.CustomerEquipmentCardsSapService;
import ar.com.avaco.premec.service.NotificacionVencimientoService;
import ar.com.avaco.premec.ws.service.ReclamoEPService;
import ar.com.avaco.premec.ws.service.ReclamoEstadisticasEPService;

@Controller
public class ReclamoRestController {

	@Autowired
	private ReclamoEPService service;

	@Autowired
	private ReclamoEstadisticasEPService estadisticasService;

	@Autowired
	private NotificacionVencimientoService vencimientoService;

	@Autowired
	private CustomerEquipmentCardsSapService maquinaService;

	@RequestMapping(value = "/reclamo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> list(ReclamoFilterDTO reclamoFilterDTO) {

		List<ServiceCallReclamoListDTO> list = this.service.list(reclamoFilterDTO);

		PageResponse pr = new PageResponse();
		pr.setPage(reclamoFilterDTO.getPage());
		pr.setPageSize(reclamoFilterDTO.getPageSize());
		pr.setTotalReg(list.isEmpty() ? 0 : list.get(0).getTotalRegistros());

		JSONResponse response = new JSONResponse();
		response.setStatus(JSONResponse.OK);
		response.setData(list);
		response.setPage(pr);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/reclamo/actividades", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> getActividades(@RequestParam Long idServiceCall) {
		List<ServiceCallActivityDTO> listActividades = this.service.listActividades(idServiceCall);
		JSONResponse response = new JSONResponse();
		response.setStatus(JSONResponse.OK);
		response.setData(listActividades);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/enviarVencimientos", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> enviarVencimientos() {
		vencimientoService.enviarNotificaciones();
		JSONResponse response = new JSONResponse();
		response.setStatus(JSONResponse.OK);
		response.setData(null);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/customer/equipment/{cuit}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> getMaquinasCustomer(
			@RequestParam(required = false, defaultValue = "") String maquina, @PathVariable String cuit)
			throws SapBusinessException {
		JSONResponse response = new JSONResponse();
		response.setData(maquinaService.listByCustomer(cuit, maquina));
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@PostMapping("/reclamo/estadisticas/maquina/maquina-parada")
	public ResponseEntity<JSONResponse> getEstadisticasPorMaquina(@RequestBody MachineReclamoStatsRequestDTO dto) {
		Map<String, EstadisticaMaquinaDTO> estadisticasMaquinaParada = this.estadisticasService
				.getEstadisticasMaquinaParada(dto);
		JSONResponse response = new JSONResponse();
		response.setOk(true);
		response.setData(estadisticasMaquinaParada);
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@PostMapping("/reclamo/estadisticas/cliente/maquina-parada")
	public ResponseEntity<JSONResponse> getEstadisticasPorCliente(@RequestBody CustomerReclamoStatsRequestDTO dto) {
		Map<String, EstadisticaClienteDTO> estadisticasCliente = this.estadisticasService.getEstadisticasCliente(dto);
		JSONResponse response = new JSONResponse();
		response.setOk(true);
		response.setData(estadisticasCliente);
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

}
