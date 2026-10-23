package ar.com.avaco.premec.ws.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.fwk.core.component.dto.PageResponse;
import ar.com.avaco.premec.domain.UsuarioPremec;
import ar.com.avaco.premec.dto.AusentismoAgrupadoDTO;
import ar.com.avaco.premec.dto.AusentismoFilterDTO;
import ar.com.avaco.premec.dto.TipoAusentismoDTO;
import ar.com.avaco.premec.service.IndicadorMensualService;

@Controller
public class AusentismoRestController {

	@Autowired
	private IndicadorMensualService indicadorMensualService;

	@RequestMapping(value = "/tipoausentismo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> tiposAusentismo() {
		List<TipoAusentismoDTO> tipos = indicadorMensualService.getTiposAusentismo();
		JSONResponse response = new JSONResponse();
		response.setData(tipos);
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/ausentismo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> ausentismo(@RequestBody AusentismoFilterDTO filter) {
		if (filter.getPage() == null || filter.getPage() < 1)
			filter.setPage(1);
		if (filter.getPageSize() == null || filter.getPageSize() < 1)
			filter.setPageSize(20);
		List<AusentismoAgrupadoDTO> listado = indicadorMensualService.getAusentismos(filter);
		PageResponse page = new PageResponse();
		page.setPage(filter.getPage());
		page.setPageSize(filter.getPageSize());
		int totalRegistros = listado.isEmpty() ? 0 : listado.get(0).getTotalRegistros();
		page.setTotalReg(totalRegistros);
		JSONResponse response = new JSONResponse();
		response.setData(listado);
		response.setPage(page);
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/miausentismo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> miausentismo(@RequestBody AusentismoFilterDTO filter) {
		if (filter.getPage() == null || filter.getPage() < 1)
			filter.setPage(1);
		if (filter.getPageSize() == null || filter.getPageSize() < 1)
			filter.setPageSize(20);

		UsuarioPremec u = (UsuarioPremec) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		filter.setEmpleadosIds(new ArrayList<>(Collections.singletonList(u.getId())));

		List<AusentismoAgrupadoDTO> listado = indicadorMensualService.getAusentismos(filter);
		PageResponse page = new PageResponse();
		page.setPage(filter.getPage());
		page.setPageSize(filter.getPageSize());
		int totalRegistros = listado.isEmpty() ? 0 : listado.get(0).getTotalRegistros();
		page.setTotalReg(totalRegistros);
		JSONResponse response = new JSONResponse();
		response.setData(listado);
		response.setPage(page);
		response.setStatus(JSONResponse.OK);
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}
}
