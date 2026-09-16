package ar.com.avaco.premec.dto;

import java.util.Set;

import ar.com.avaco.fwk.security.dto.PerfilDTO;
import ar.com.avaco.fwk.security.dto.UsuarioDTO;

public class UsuarioPremecDTO extends UsuarioDTO {

	private static final long serialVersionUID = 1L;

	private String usuariosap;
	private String deposito;
	private Long legajo;

	public UsuarioPremecDTO() {
	}
	
	public UsuarioPremecDTO(Long id, String username, String name, String lastname, Set<PerfilDTO> profiles,
			String email, String usuariosap, boolean enabled, Boolean admin, String deposito, Long legajo) {
		super(id, username, name, lastname, profiles, email, enabled, admin);
		this.deposito = deposito;
		this.usuariosap = usuariosap;
		this.legajo = legajo;
	}

	public String getUsuariosap() {
		return usuariosap;
	}

	public void setUsuariosap(String usuariosap) {
		this.usuariosap = usuariosap;
	}

	public String getDeposito() {
		return deposito;
	}

	public void setDeposito(String deposito) {
		this.deposito = deposito;
	}

	public Long getLegajo() {
		return legajo;
	}

	public void setLegajo(Long legajo) {
		this.legajo = legajo;
	}

}
