/**
 * 
 */
package ar.com.avaco.premec.repository;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import ar.com.avaco.fwk.core.component.repository.NJBaseRepository;
import ar.com.avaco.premec.domain.UsuarioPremec;

@Repository("usuarioPremecRepository")
public class UsuarioPremecRepositoryImpl extends NJBaseRepository<Long, UsuarioPremec>
		implements UsuarioPremecRepositoryCustom {

	public UsuarioPremecRepositoryImpl(EntityManager entityManager) {
		super(UsuarioPremec.class, entityManager);
	}

}
