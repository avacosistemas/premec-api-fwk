package ar.com.avaco.premec.dto;

import java.util.HashSet;
import java.util.Set;

import ar.com.avaco.fwk.core.component.dto.DTOEntity;

public class GrupoEmpleadoDTO extends DTOEntity<Long> {

	private Long id;

	private String nombre;

	private Set<UsuarioEmpleadoDTO> usuarios = new HashSet<>();

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Set<UsuarioEmpleadoDTO> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(Set<UsuarioEmpleadoDTO> usuarios) {
		this.usuarios = usuarios;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
