package ar.com.avaco.premec.ws.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.com.avaco.fwk.core.component.controller.AbstractDTORestController;
import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.fwk.core.exception.BusinessException;
import ar.com.avaco.premec.dto.ClienteDTO;
import ar.com.avaco.premec.service.NotificacionReclamoService;
import ar.com.avaco.premec.ws.dto.ComboDTO;
import ar.com.avaco.premec.ws.service.ClienteEPService;

@RestController
public class ClienteRestController extends AbstractDTORestController<ClienteDTO, Long, ClienteEPService> {

	@Autowired
	private NotificacionReclamoService notificacionService;
	
	@Override
	@RequestMapping(value = "/cliente", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> list() {
		return super.list();
	}

	@RequestMapping(value = "/cliente/combo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> list(@RequestParam String nombre) {
		List<ClienteDTO> listPattern = this.service.listPattern("nombre", nombre);
		List<ComboDTO> comboList = new ArrayList<ComboDTO>();
		listPattern.forEach(x -> {
			comboList.add(new ComboDTO(x.getNombre(), x.getUsername()));
		});
		JSONResponse response = new JSONResponse();
		response.setStatus(JSONResponse.OK);
		response.setData(comboList);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/cliente/nofiticacion", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> notificar() {
		notificacionService.enviarNotificaciones();
		return null;
	}
	
	@Override
	@RequestMapping(value = "/cliente", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> create(@RequestBody ClienteDTO dto) throws BusinessException {
		return super.create(dto);
	}
	
	@Override
	@Resource(name = "clienteEPService")
	public void setService(ClienteEPService service) {
		this.service = service;
	}

}
