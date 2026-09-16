/**
 * 
 */
package ar.com.avaco.premec.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.avaco.fwk.core.component.service.NJBaseService;
import ar.com.avaco.fwk.security.exception.NuclearJSecurityException;
import ar.com.avaco.fwk.security.service.UsuarioService;
import ar.com.avaco.premec.domain.UsuarioPremec;
import ar.com.avaco.premec.repository.UsuarioPremecRepository;

/**
 * @author avaco
 */

@Transactional
@Service("usuarioPremecService")
public class UsuarioPremecServiceImpl extends NJBaseService<Long, UsuarioPremec, UsuarioPremecRepository>
		implements UsuarioPremecService {

	@Autowired
	private UsuarioService usuarioService;
	private static final Integer INICIO_REINTENTOS_LOGIN = 0;
	
	@Override
	public void updatePassword(UsuarioPremec user, String password, String newPassword) {
		usuarioService.updatePassword(user, password, newPassword);
	}
	
	@Override
	public UsuarioPremec save(UsuarioPremec usuario) throws NuclearJSecurityException {
		usuarioService.validarUsuario(usuario);

		// Por default el usuario se da de desbloqueado.
		usuario.setBloqueado(false);

		// La cantidad de intentos de login es cero.
		usuario.setIntentosFallidosLogin(INICIO_REINTENTOS_LOGIN);

		// Por mas que luego se reenviara el password, se requerira cambio de
		// password.
		usuario.setRequiereCambioPassword(false);

		String tmppass = usuarioService.generarPasswordAleatorio();
		usuario.setPassword(usuarioService.encodePassword(tmppass));

		usuario = getRepository().save(usuario);

		usuarioService.notifyPasswordNewUser(usuario, tmppass);

		return usuario;
	}
	
	@Override
	public void sendMissingPassword(String username) {
		this.usuarioService.sendMissingPassword(username);
	}

	@Override
	public void sendMissingPasswordById(Long id) {
		this.usuarioService.sendMissingPasswordById(id);
	}
	
	@Override
	public String getUsuarioSAP(String username) {
		UsuarioPremec usuario = this.getRepository().findByUsername(username);
		return usuario.getUsuariosap();
	}

	@Override
	public String getDeposito(String username) {
		UsuarioPremec usuario = this.getRepository().findByUsername(username);
		return usuario.getDeposito();
	}

	@Override
	public String getUsuarioSAPByLegajo(Long legajo) {
		String usuariosap = this.repository.findByLegajo(legajo).getUsuariosap();
		return usuariosap;
	}

	@Override
	public UsuarioPremec findByUsername(String name) {
		return this.repository.findByUsername(name);
	}

	@Override
	public List<UsuarioPremec> getByIds(List<Long> lista) {
		return this.repository.findByIdIn(lista);
	}

	@Resource(name = "usuarioPremecRepository")
	public void setRepository(UsuarioPremecRepository repository) {
		this.repository = repository;
	}
	
}
