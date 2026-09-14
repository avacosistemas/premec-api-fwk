/**
 * 
 */
package ar.com.avaco.premec.repository;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import ar.com.avaco.fwk.core.component.repository.NJBaseRepository;
import ar.com.avaco.premec.domain.ItemChecklistGrupo;

@Repository("itemChecklistGrupoRepository")
public class ItemChecklistGrupoRepositoryImpl extends NJBaseRepository<Long, ItemChecklistGrupo>
		implements GrupoTipoActividadRepositoryCustom {

	public ItemChecklistGrupoRepositoryImpl(EntityManager entityManager) {
		super(ItemChecklistGrupo.class, entityManager);
	}
}
