/**
 * 
 */
package ar.com.avaco.premec.repository;

import ar.com.avaco.fwk.core.component.repository.NJRepository;
import ar.com.avaco.premec.domain.Cliente;

/**
 * @author avaco
 *
 */
public interface ClienteRepository
		extends NJRepository<Long, Cliente>, ClienteRepositoryCustom {

}
