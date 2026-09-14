/**
 * 
 */
package ar.com.avaco.premec.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.avaco.fwk.core.component.service.NJBaseService;
import ar.com.avaco.premec.domain.GrupoTipoActividad;
import ar.com.avaco.premec.repository.GrupoTipoActividadRepository;

/**
 * @author avaco
 */

@Transactional
@Service("grupoTipoActividadService")
public class GrupoTipoActividadServiceImpl extends NJBaseService<Long, GrupoTipoActividad, GrupoTipoActividadRepository>
		implements GrupoTipoActividadService {

	@Resource(name = "grupoTipoActividadRepository")
	public void setRepository(GrupoTipoActividadRepository grupoTipoActividadRepository) {
		repository = grupoTipoActividadRepository;
	}

}
