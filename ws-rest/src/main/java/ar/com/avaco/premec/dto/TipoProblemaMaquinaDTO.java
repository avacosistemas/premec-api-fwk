package ar.com.avaco.premec.dto;

import ar.com.avaco.fwk.core.component.dto.DTOEntity;
import ar.com.avaco.premec.domain.TipoMaquina;

public class TipoProblemaMaquinaDTO extends DTOEntity<Long> {

	private Long id;

	private String nombre;

	private TipoMaquina tipoMaquina;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public TipoMaquina getTipoMaquina() {
		return tipoMaquina;
	}

	public void setTipoMaquina(TipoMaquina tipoMaquina) {
		this.tipoMaquina = tipoMaquina;
	}

}
