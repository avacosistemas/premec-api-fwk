package ar.com.avaco.premec.sap.dto;

public class MaquinaParadaPeriodoDTO {

	private Integer anio;
	private Integer mes;
	private Integer cantidadReclamos;
	private Integer diasParadaTotal;

	public Integer getAnio() {
		return anio;
	}

	public void setAnio(Integer anio) {
		this.anio = anio;
	}

	public Integer getMes() {
		return mes;
	}

	public void setMes(Integer mes) {
		this.mes = mes;
	}

	public Integer getCantidadReclamos() {
		return cantidadReclamos;
	}

	public void setCantidadReclamos(Integer cantidadReclamos) {
		this.cantidadReclamos = cantidadReclamos;
	}

	public Integer getDiasParadaTotal() {
		return diasParadaTotal;
	}

	public void setDiasParadaTotal(Integer diasParadaTotal) {
		this.diasParadaTotal = diasParadaTotal;
	}

}
