/**
 * 
 */
package ar.com.avaco.premec.repository;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import ar.com.avaco.fwk.core.component.repository.NJBaseRepository;
import ar.com.avaco.premec.domain.Cliente;

@Repository("clienteRepository")
public class ClienteRepositoryImpl extends NJBaseRepository<Long, Cliente>
		implements ClienteRepository {

	public ClienteRepositoryImpl(EntityManager entityManager) {
		super(Cliente.class, entityManager);
	}
}
