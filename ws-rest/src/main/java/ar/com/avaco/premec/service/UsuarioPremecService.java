/**
 * 
 */
package ar.com.avaco.premec.service;

import java.util.List;

import ar.com.avaco.fwk.core.component.service.NJService;
import ar.com.avaco.premec.domain.UsuarioPremec;

public interface UsuarioPremecService extends NJService<Long, UsuarioPremec> {

	String getUsuarioSAP(String username);

	String getDeposito(String username);

	String getUsuarioSAPByLegajo(Long legajo);

	UsuarioPremec findByUsername(String name);
	
	List<UsuarioPremec> getByIds(List<Long> lista);

	void updatePassword(UsuarioPremec user, String password, String newPassword);

	void sendMissingPasswordById(Long id);

	void sendMissingPassword(String username);
}
