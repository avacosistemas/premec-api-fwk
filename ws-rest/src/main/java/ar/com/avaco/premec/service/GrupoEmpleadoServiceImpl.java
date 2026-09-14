/**
 * 
 */
package ar.com.avaco.premec.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.avaco.fwk.core.component.service.NJBaseService;
import ar.com.avaco.premec.domain.GrupoEmpleado;
import ar.com.avaco.premec.repository.GrupoEmpleadoRepository;

/**
 * @author avaco
 */

@Transactional
@Service("grupoEmpleadoService")
public class GrupoEmpleadoServiceImpl extends NJBaseService<Long, GrupoEmpleado, GrupoEmpleadoRepository>
		implements GrupoEmpleadoService {

	@Resource(name = "grupoEmpleadoRepository")
	public void setRepository(GrupoEmpleadoRepository grupoEmpleadoRepository) {
		repository = grupoEmpleadoRepository;
	}

}
