/**
 * 
 */
package ar.com.avaco.premec.repository;

import java.util.List;

import ar.com.avaco.fwk.core.component.repository.NJRepository;
import ar.com.avaco.premec.domain.UsuarioPremec;

/**
 * @author avaco
 *
 */
public interface UsuarioPremecRepository extends NJRepository<Long, UsuarioPremec>, UsuarioPremecRepositoryCustom {

	UsuarioPremec findByUsername(String username);

	UsuarioPremec findByLegajo(Long legajo);

	List<UsuarioPremec> findByIdIn(List<Long> lista);

}
