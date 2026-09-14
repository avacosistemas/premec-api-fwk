/**
 * 
 */
package ar.com.avaco.premec.repository;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import ar.com.avaco.fwk.core.component.repository.NJBaseRepository;
import ar.com.avaco.premec.domain.GrupoEmpleado;

@Repository("grupoEmpleadoRepository")
public class GrupoEmpleadoRepositoryImpl extends NJBaseRepository<Long, GrupoEmpleado>
		implements GrupoEmpleadoRepository {

	public GrupoEmpleadoRepositoryImpl(EntityManager entityManager) {
		super(GrupoEmpleado.class, entityManager);
	}
}
