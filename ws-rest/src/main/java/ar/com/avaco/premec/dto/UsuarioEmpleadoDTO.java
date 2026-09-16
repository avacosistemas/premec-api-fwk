package ar.com.avaco.premec.dto;

import ar.com.avaco.fwk.core.component.dto.DTOEntity;

public class UsuarioEmpleadoDTO extends DTOEntity<Long> {

	private Long id;

	private String usuario;

	public UsuarioEmpleadoDTO() {
		// TODO Auto-generated constructor stub
	}

	public UsuarioEmpleadoDTO(Long idUsr, String nombreApellidoUsername) {
		this.id = idUsr;
		this.usuario = nombreApellidoUsername;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

}
