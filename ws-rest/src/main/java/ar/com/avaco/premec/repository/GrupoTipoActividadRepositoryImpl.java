/**
 * 
 */
package ar.com.avaco.premec.repository;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import ar.com.avaco.fwk.core.component.repository.NJBaseRepository;
import ar.com.avaco.premec.domain.GrupoTipoActividad;

@Repository("grupoTipoActividadRepository")
public class GrupoTipoActividadRepositoryImpl extends NJBaseRepository<Long, GrupoTipoActividad>
		implements GrupoTipoActividadRepositoryCustom {

	public GrupoTipoActividadRepositoryImpl(EntityManager entityManager) {
		super(GrupoTipoActividad.class, entityManager);
	}
}
