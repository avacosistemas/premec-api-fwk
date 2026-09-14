/**
 * 
 */
package ar.com.avaco.premec.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.avaco.fwk.core.component.service.NJBaseService;
import ar.com.avaco.premec.domain.ProblemaMaquina;
import ar.com.avaco.premec.repository.ProblemaMaquinaRepository;

@Transactional
@Service("problemaMaquinaService")
public class ProblemaMaquinaServiceImpl extends NJBaseService<Long, ProblemaMaquina, ProblemaMaquinaRepository>
		implements ProblemaMaquinaService {

	@Resource(name = "problemaMaquinaRepository")
	public void setRepository(ProblemaMaquinaRepository problemaMaquinaRepository) {
		repository = problemaMaquinaRepository;
	}

}
