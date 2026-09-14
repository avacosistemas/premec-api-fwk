/**
 * 
 */
package ar.com.avaco.premec.repository;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import ar.com.avaco.fwk.core.component.repository.NJBaseRepository;
import ar.com.avaco.premec.domain.RegistroHorasMaquinaExcedidaReseteo;

@Repository("registroHorasMaquinaExcedidaReseteoRepository")
public class RegistroHorasMaquinaExcedidaReseteoRepositoryImpl extends NJBaseRepository<Long, RegistroHorasMaquinaExcedidaReseteo>
		implements RegistroHorasMaquinaExcedidaReseteoRepositoryCustom {

	public RegistroHorasMaquinaExcedidaReseteoRepositoryImpl(EntityManager entityManager) {
		super(RegistroHorasMaquinaExcedidaReseteo.class, entityManager);
	}
}
