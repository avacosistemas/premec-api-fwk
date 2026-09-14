package ar.com.avaco.premec.ws.controller;

import javax.annotation.Resource;

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
import ar.com.avaco.premec.dto.TipoProblemaMaquinaDTO;
import ar.com.avaco.premec.ws.service.TipoProblemaMaquinaEPService;

@RestController
public class TipoProblemaMaquinaRestController
		extends AbstractDTORestController<TipoProblemaMaquinaDTO, Long, TipoProblemaMaquinaEPService> {

	@Override
	@RequestMapping(value = "/tipoProblemaMaquina", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> list() {
		return super.list();
	}

	@Override
	@RequestMapping(value = "/tipoProblemaMaquina", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> create(@RequestBody TipoProblemaMaquinaDTO dto) throws BusinessException {
		return super.create(dto);
	}

	@RequestMapping(value = "/tipoProblemaMaquina", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> update(@RequestBody TipoProblemaMaquinaDTO dto)
			throws BusinessException {
		return super.update(dto.getId(), dto);
	}

	@Override
	@RequestMapping(value = "/tipoProblemaMaquina/{idTipoProblemaMaquina}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> delete(@PathVariable Long idTipoProblemaMaquina) throws BusinessException {
		return super.delete(idTipoProblemaMaquina);
	}

	@Override
	@Resource(name = "tipoProblemaMaquinaEPService")
	public void setService(TipoProblemaMaquinaEPService service) {
		this.service = service;
	}

}
