package ar.com.avaco.premec.dto;

import java.math.BigDecimal;

public class IndicadorAusentismoDTO {

	private BigDecimal horasTotales = BigDecimal.ZERO;
	private BigDecimal horasAusencia = BigDecimal.ZERO;
	private BigDecimal horasAusenciaJustificadas = BigDecimal.ZERO;
	private BigDecimal horasAusenciaInjustificadas = BigDecimal.ZERO;
	private BigDecimal porcentajeAusentismo = BigDecimal.ZERO;

	public BigDecimal getHorasTotales() {
		return horasTotales;
	}

	public void setHorasTotales(BigDecimal horasTotales) {
		this.horasTotales = horasTotales;
	}

	public BigDecimal getHorasAusencia() {
		return horasAusencia;
	}

	public void setHorasAusencia(BigDecimal horasAusencia) {
		this.horasAusencia = horasAusencia;
	}

	public BigDecimal getHorasAusenciaJustificadas() {
		return horasAusenciaJustificadas;
	}

	public void setHorasAusenciaJustificadas(BigDecimal horasAusenciaJustificadas) {
		this.horasAusenciaJustificadas = horasAusenciaJustificadas;
	}

	public BigDecimal getHorasAusenciaInjustificadas() {
		return horasAusenciaInjustificadas;
	}

	public void setHorasAusenciaInjustificadas(BigDecimal horasAusenciaInjustificadas) {
		this.horasAusenciaInjustificadas = horasAusenciaInjustificadas;
	}

	public BigDecimal getPorcentajeAusentismo() {
		return porcentajeAusentismo;
	}

	public void setPorcentajeAusentismo(BigDecimal porcentajeAusentismo) {
		this.porcentajeAusentismo = porcentajeAusentismo;
	}
}
