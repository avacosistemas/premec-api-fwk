package ar.com.avaco.premec.ws.service;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.core.component.epservice.CRUDEPBaseService;
import ar.com.avaco.premec.domain.ProblemaMaquina;
import ar.com.avaco.premec.domain.TipoProblemaMaquina;
import ar.com.avaco.premec.dto.ProblemaMaquinaDTO;
import ar.com.avaco.premec.service.ProblemaMaquinaService;
import ar.com.avaco.premec.service.TipoProblemaMaquinaService;

@Service("problemaMaquinaEPService")
public class ProblemaMaquinaEPServiceImpl
		extends CRUDEPBaseService<Long, ProblemaMaquinaDTO, ProblemaMaquina, ProblemaMaquinaService>
		implements ProblemaMaquinaEPService {

	@Resource(name = "problemaMaquinaService")
	protected void setService(ProblemaMaquinaService service) {
		this.service = service;
	}

	@Autowired
	private TipoProblemaMaquinaService tipoProblemaMaquinaService;

	@Override
	protected ProblemaMaquina convertToEntity(ProblemaMaquinaDTO dto) {
		ProblemaMaquina pm = new ProblemaMaquina();
		pm.setId(dto.getId());
		pm.setNombre(dto.getNombre());
		TipoProblemaMaquina tipoProblemaMaquina = this.tipoProblemaMaquinaService.get(dto.getIdTipoProblemaMaquina());
		pm.setTipoProblemaMaquina(tipoProblemaMaquina);
		return pm;
	}

	@Override
	protected ProblemaMaquinaDTO convertToDto(ProblemaMaquina entity) {
		ProblemaMaquinaDTO pm = new ProblemaMaquinaDTO();
		pm.setId(entity.getId());
		pm.setNombre(entity.getNombre());
		pm.setIdTipoProblemaMaquina(entity.getTipoProblemaMaquina().getId());
		pm.setTipoProblema(entity.getTipoProblemaMaquina().getNombre());
		return pm;
	}

}
