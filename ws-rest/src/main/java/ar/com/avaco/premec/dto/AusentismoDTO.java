package ar.com.avaco.premec.dto;

public class AusentismoDTO {
	
	private String fecha;

	private String tipoAusentismo;
	
	private Boolean justificado;
	
	private String horasAusentes;

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getTipoAusentismo() {
		return tipoAusentismo;
	}

	public void setTipoAusentismo(String tipoAusentismo) {
		this.tipoAusentismo = tipoAusentismo;
	}

	public Boolean getJustificado() {
		return justificado;
	}

	public void setJustificado(Boolean justificado) {
		this.justificado = justificado;
	}

	public String getHorasAusentes() {
		return horasAusentes;
	}

	public void setHorasAusentes(String horasAusentes) {
		this.horasAusentes = horasAusentes;
	}

}
