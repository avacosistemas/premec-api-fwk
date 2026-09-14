package ar.com.avaco.premec.ws.controller;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ar.com.avaco.fwk.core.component.controller.AbstractDTORestController;
import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.premec.filter.RegistroHorasMaquinaExcedidaReseteoFilter;
import ar.com.avaco.premec.filter.RegistroHorasMaquinaExcedidaReseteoFilterDTO;
import ar.com.avaco.premec.ws.dto.RegistroHorasMaquinaExcedidaReseteoDTO;
import ar.com.avaco.premec.ws.service.RegistroHorasMaquinaExcedidaReseteoEPService;

@RestController
public class RegistroHorasMaquinaExcedidaReseteoRestController
		extends AbstractDTORestController<RegistroHorasMaquinaExcedidaReseteoDTO, Long, RegistroHorasMaquinaExcedidaReseteoEPService> {

	@RequestMapping(value = "/horasMaquinaExcesoReseteo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> list(RegistroHorasMaquinaExcedidaReseteoFilterDTO filter) {
		JSONResponse response = new JSONResponse();
		try {
			if (StringUtils.isBlank(filter.getIdx())) {
				filter.setIdx("fechaActual");
				filter.setAsc(false);
			}
			List<RegistroHorasMaquinaExcedidaReseteoDTO> listFilter = this.service.listFilter(new RegistroHorasMaquinaExcedidaReseteoFilter(filter));
			response.setData(listFilter);
			response.setStatus(JSONResponse.OK);
		} catch (Exception e) {
			response.setStatus(JSONResponse.ERROR);
			response.setData(e);
			e.printStackTrace();
		}
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}
	
	@Override
	@Resource(name = "registroHorasMaquinaExcedidaReseteoEPService")
	public void setService(RegistroHorasMaquinaExcedidaReseteoEPService service) {
		this.service = service;
	}

	
}
