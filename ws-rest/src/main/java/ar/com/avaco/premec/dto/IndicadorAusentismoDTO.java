package ar.com.avaco.premec.dto;

import java.math.BigDecimal;

public class IndicadorAusentismoDTO {

	private BigDecimal horasTotales = BigDecimal.ZERO;
	private BigDecimal horasAusencia = BigDecimal.ZERO;
	private BigDecimal horasAusenciaJustificadas = BigDecimal.ZERO;
	private BigDecimal horasAusenciaInjustificadas = BigDecimal.ZERO;
	private BigDecimal porcentajeAusentismo = BigDecimal.ZERO;
	private BigDecimal porcentajeAusenciaJustificada = BigDecimal.ZERO;
	private BigDecimal porcentajeAusenciaInjustificada = BigDecimal.ZERO;

	public String getHorasTotales() {
		return formatearHoras(horasTotales);
	}

	public BigDecimal getHorasTotalesValor() { return horasTotales; }

	public void setHorasTotales(BigDecimal horasTotales) {
		this.horasTotales = horasTotales;
	}

	public String getHorasAusencia() {
		return formatearHoras(horasAusencia);
	}

	public BigDecimal getHorasAusenciaValor() { return horasAusencia; }

	public void setHorasAusencia(BigDecimal horasAusencia) {
		this.horasAusencia = horasAusencia;
	}

	public String getHorasAusenciaJustificadas() {
		return formatearHoras(horasAusenciaJustificadas);
	}

	public BigDecimal getHorasAusenciaJustificadasValor() { return horasAusenciaJustificadas; }

	public void setHorasAusenciaJustificadas(BigDecimal horasAusenciaJustificadas) {
		this.horasAusenciaJustificadas = horasAusenciaJustificadas;
	}

	public String getHorasAusenciaInjustificadas() {
		return formatearHoras(horasAusenciaInjustificadas);
	}

	public BigDecimal getHorasAusenciaInjustificadasValor() { return horasAusenciaInjustificadas; }

	private String formatearHoras(BigDecimal horas) {
		long segundosTotales = (horas == null ? BigDecimal.ZERO : horas)
				.multiply(BigDecimal.valueOf(3600)).setScale(0, java.math.RoundingMode.HALF_UP).longValue();
		long horasTotales = segundosTotales / 3600;
		long minutos = (segundosTotales % 3600) / 60;
		long segundos = segundosTotales % 60;
		return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", horasTotales, minutos, segundos);
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

	public BigDecimal getPorcentajeAusenciaJustificada() {
		return porcentajeAusenciaJustificada;
	}

	public void setPorcentajeAusenciaJustificada(BigDecimal porcentajeAusenciaJustificada) {
		this.porcentajeAusenciaJustificada = porcentajeAusenciaJustificada;
	}

	public BigDecimal getPorcentajeAusenciaInjustificada() {
		return porcentajeAusenciaInjustificada;
	}

	public void setPorcentajeAusenciaInjustificada(BigDecimal porcentajeAusenciaInjustificada) {
		this.porcentajeAusenciaInjustificada = porcentajeAusenciaInjustificada;
	}
}
