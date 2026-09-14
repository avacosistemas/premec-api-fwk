package ar.com.avaco.premec.ws.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.com.avaco.fwk.core.component.controller.AbstractDTORestController;
import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.fwk.core.exception.BusinessException;
import ar.com.avaco.premec.dto.ProblemaMaquinaDTO;
import ar.com.avaco.premec.filter.ProblemaMaquinaFilter;
import ar.com.avaco.premec.ws.service.ProblemaMaquinaEPService;

@RestController
public class ProblemaMaquinaRestController
		extends AbstractDTORestController<ProblemaMaquinaDTO, Long, ProblemaMaquinaEPService> {

	@RequestMapping(value = "/problemaMaquina", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> listByTipo(@RequestParam Long idTipoProblemaMaquina) {
		JSONResponse response = new JSONResponse();
		
		ProblemaMaquinaFilter filter = new ProblemaMaquinaFilter();
		filter.setIdTipoProblemaMaquina(idTipoProblemaMaquina);
		filter.setIdx("nombre");
		
		List<ProblemaMaquinaDTO> list = this.service.listFilter(filter);
		response.setData(list);
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@Override
	@RequestMapping(value = "/problemaMaquina", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> create(@RequestBody ProblemaMaquinaDTO dto) throws BusinessException {
		return super.create(dto);
	}

	@RequestMapping(value = "/problemaMaquina", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> update(@RequestBody ProblemaMaquinaDTO dto) throws BusinessException {
		return super.update(dto.getId(), dto);
	}

	@Override
	@RequestMapping(value = "/problemaMaquina/{idProblemaMaquina}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> delete(@PathVariable Long idProblemaMaquina) throws BusinessException {
		return super.delete(idProblemaMaquina);
	}

	@Override
	@Resource(name = "problemaMaquinaEPService")
	public void setService(ProblemaMaquinaEPService service) {
		this.service = service;
	}

}
