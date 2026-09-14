/**
 * 
 */
package ar.com.avaco.premec.repository;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import ar.com.avaco.fwk.core.component.repository.NJBaseRepository;
import ar.com.avaco.premec.domain.ProblemaMaquina;

@Repository("problemaMaquinaRepository")
public class ProblemaMaquinaRepositoryImpl extends NJBaseRepository<Long, ProblemaMaquina>
		implements ProblemaMaquinaRepository {

	public ProblemaMaquinaRepositoryImpl(EntityManager entityManager) {
		super(ProblemaMaquina.class, entityManager);
	}
}
