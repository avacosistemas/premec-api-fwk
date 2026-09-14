package ar.com.avaco.premec.dto;

import ar.com.avaco.fwk.core.component.dto.DTOEntity;

public class ProblemaMaquinaDTO extends DTOEntity<Long> {

	private Long id;

	private String nombre;

	private Long idTipoProblemaMaquina;

	private String tipoProblema;

	public String getTipoProblema() {
		return tipoProblema;
	}

	public void setTipoProblema(String tipoProblema) {
		this.tipoProblema = tipoProblema;
	}

	public Long getIdTipoProblemaMaquina() {
		return idTipoProblemaMaquina;
	}

	public void setIdTipoProblemaMaquina(Long idTipoProblemaMaquina) {
		this.idTipoProblemaMaquina = idTipoProblemaMaquina;
	}

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

}
