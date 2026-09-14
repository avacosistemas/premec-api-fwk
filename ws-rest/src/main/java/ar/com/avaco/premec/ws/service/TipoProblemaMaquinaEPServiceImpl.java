package ar.com.avaco.premec.ws.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.avaco.fwk.core.component.epservice.CRUDEPBaseService;
import ar.com.avaco.premec.domain.TipoProblemaMaquina;
import ar.com.avaco.premec.dto.TipoProblemaMaquinaDTO;
import ar.com.avaco.premec.service.TipoProblemaMaquinaService;

@Service("tipoProblemaMaquinaEPService")
public class TipoProblemaMaquinaEPServiceImpl
		extends CRUDEPBaseService<Long, TipoProblemaMaquinaDTO, TipoProblemaMaquina, TipoProblemaMaquinaService>
		implements TipoProblemaMaquinaEPService {

	@Resource(name = "tipoProblemaMaquinaService")
	protected void setService(TipoProblemaMaquinaService service) {
		this.service = service;
	}

	@Override
	protected TipoProblemaMaquina convertToEntity(TipoProblemaMaquinaDTO dto) {
		ObjectMapper mapper = new ObjectMapper();
		TipoProblemaMaquina tpm = mapper.convertValue(dto, TipoProblemaMaquina.class);
		return tpm;
	}

	@Override
	protected TipoProblemaMaquinaDTO convertToDto(TipoProblemaMaquina entity) {
		ObjectMapper mapper = new ObjectMapper();
		TipoProblemaMaquinaDTO tcmdto = mapper.convertValue(entity, TipoProblemaMaquinaDTO.class);
		return tcmdto;
	}

}
