package ar.com.avaco.premec.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import ar.com.avaco.fwk.security.domain.Usuario;

@Entity
@Table(name = "PREMEC_USUARIO")
@PrimaryKeyJoinColumn(name = "ID_SEG_USUARIO")
public class UsuarioPremec extends Usuario {

	private static final long serialVersionUID = -7539075530998277489L;

	@Column(name = "USUARIOSAP")
	private String usuariosap;

	@Column(name = "DEPOSITO")
	private String deposito;

	@Column(name = "LEGAJO")
	private Long legajo;

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