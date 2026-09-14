/**
 * 
 */
package ar.com.avaco.premec.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.avaco.fwk.core.component.service.NJBaseService;
import ar.com.avaco.premec.domain.TipoProblemaMaquina;
import ar.com.avaco.premec.repository.TipoProblemaMaquinaRepository;

/**
 * @author avaco
 */

@Transactional
@Service("tipoProblemaMaquinaService")
public class TipoProblemaMaquinaServiceImpl extends NJBaseService<Long, TipoProblemaMaquina, TipoProblemaMaquinaRepository>
		implements TipoProblemaMaquinaService {

	@Resource(name = "tipoProblemaMaquinaRepository")
	public void setRepository(TipoProblemaMaquinaRepository tipoProblemaMaquinaRepository) {
		repository = tipoProblemaMaquinaRepository;
	}

}
