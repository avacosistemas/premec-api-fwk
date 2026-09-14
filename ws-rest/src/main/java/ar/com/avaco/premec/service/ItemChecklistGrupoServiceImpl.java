/**
 * 
 */
package ar.com.avaco.premec.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.avaco.fwk.core.component.service.NJBaseService;
import ar.com.avaco.premec.domain.ItemChecklistGrupo;
import ar.com.avaco.premec.repository.ItemChecklistGrupoRepository;

/**
 * @author avaco
 */

@Transactional
@Service("itemChecklistGrupoService")
public class ItemChecklistGrupoServiceImpl extends NJBaseService<Long, ItemChecklistGrupo, ItemChecklistGrupoRepository>
		implements ItemCheckListGrupoService {

	@Resource(name = "itemChecklistGrupoRepository")
	public void setRepository(ItemChecklistGrupoRepository itemChecklistGrupoRepository) {
		repository = itemChecklistGrupoRepository;
	}

}
