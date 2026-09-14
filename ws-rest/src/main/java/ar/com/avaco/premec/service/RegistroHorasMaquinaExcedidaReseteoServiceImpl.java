/**
 * 
 */
package ar.com.avaco.premec.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.avaco.fwk.core.component.service.NJBaseService;
import ar.com.avaco.premec.domain.RegistroHorasMaquinaExcedidaReseteo;
import ar.com.avaco.premec.repository.RegistroHorasMaquinaExcedidaReseteoRepository;

/**
 * @author avaco
 */

@Transactional
@Service("registroHorasMaquinaExcedidaReseteoService")
public class RegistroHorasMaquinaExcedidaReseteoServiceImpl extends NJBaseService<Long, RegistroHorasMaquinaExcedidaReseteo, RegistroHorasMaquinaExcedidaReseteoRepository>
		implements RegistroHorasMaquinaExcedidaReseteoService {

	@Resource(name = "registroHorasMaquinaExcedidaReseteoRepository")
	public void setRepository(RegistroHorasMaquinaExcedidaReseteoRepository repository) {
		this.repository = repository;
	}

}
