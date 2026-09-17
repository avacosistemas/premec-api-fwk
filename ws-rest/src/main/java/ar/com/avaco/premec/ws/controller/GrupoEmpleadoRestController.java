package ar.com.avaco.premec.ws.controller;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ar.com.avaco.fwk.core.component.controller.AbstractDTORestController;
import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.fwk.core.exception.BusinessException;
import ar.com.avaco.premec.dto.GrupoEmpleadoDTO;
import ar.com.avaco.premec.ws.service.GrupoEmpleadoEPService;

@RestController
public class GrupoEmpleadoRestController
		extends AbstractDTORestController<GrupoEmpleadoDTO, Long, GrupoEmpleadoEPService> {

	@RequestMapping(value = "/grupoEmpleado/usuarios", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> listUsuarios() {
		JSONResponse response = new JSONResponse();
		try {
			response.setData(this.service.listUsuarios());
			response.setStatus(JSONResponse.OK);
		} catch (Exception e) {
			response.setStatus(JSONResponse.ERROR);
			response.setData(e);
			e.printStackTrace();
		}
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@Override
	@RequestMapping(value = "/grupoEmpleado", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> list() {
		return super.list();
	}

	@Override
	@RequestMapping(value = "/grupoEmpleado", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> create(@RequestBody GrupoEmpleadoDTO dto) throws BusinessException {
		// TODO Auto-generated method stub
		return super.create(dto);
	}

	@RequestMapping(value = "/grupoEmpleado", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> update(@RequestBody GrupoEmpleadoDTO dto)
			throws BusinessException {
		return super.update(dto.getId(), dto);
	}

	@Override
	@RequestMapping(value = "/grupoEmpleado/{idGrupoEmpleado}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> delete(@PathVariable Long idGrupoEmpleado) throws BusinessException {
		return super.delete(idGrupoEmpleado);
	}

	@Override
	@Resource(name = "grupoEmpleadoEPService")
	public void setService(GrupoEmpleadoEPService service) {
		this.service = service;
	}

}
