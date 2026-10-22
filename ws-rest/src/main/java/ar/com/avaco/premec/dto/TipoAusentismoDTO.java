package ar.com.avaco.premec.dto;

public class TipoAusentismoDTO {
	private String codigo;
	private String nombre;
	private Boolean justificado;

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Boolean getJustificado() {
		return justificado;
	}

	public void setJustificado(Boolean justificado) {
		this.justificado = justificado;
	}
}
