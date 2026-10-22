package ar.com.avaco.premec.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AusentismoAgrupadoDTO {
	private Long idEmpleado;
	private String empleado;
	private String horasTotales;
	private String horasAusentes;
	private String horasJustificadas;
	private String horasInjustificadas;
	private BigDecimal porcentajeAusentismo;
	private BigDecimal porcentajeJustificado;
	private BigDecimal porcentajeInjustificado;
	private Integer totalRegistros;
	private List<AusentismoDTO> detalle = new ArrayList<>();

	public Long getIdEmpleado() {
		return idEmpleado;
	}

	public void setIdEmpleado(Long idEmpleado) {
		this.idEmpleado = idEmpleado;
	}

	public String getEmpleado() {
		return empleado;
	}

	public void setEmpleado(String empleado) {
		this.empleado = empleado;
	}

	public String getHorasTotales() {
		return horasTotales;
	}

	public void setHorasTotales(String horasTotales) {
		this.horasTotales = horasTotales;
	}

	public String getHorasAusentes() {
		return horasAusentes;
	}

	public void setHorasAusentes(String horasAusentes) {
		this.horasAusentes = horasAusentes;
	}

	public String getHorasJustificadas() {
		return horasJustificadas;
	}

	public void setHorasJustificadas(String horasJustificadas) {
		this.horasJustificadas = horasJustificadas;
	}

	public String getHorasInjustificadas() {
		return horasInjustificadas;
	}

	public void setHorasInjustificadas(String horasInjustificadas) {
		this.horasInjustificadas = horasInjustificadas;
	}

	public BigDecimal getPorcentajeAusentismo() {
		return porcentajeAusentismo;
	}

	public void setPorcentajeAusentismo(BigDecimal porcentajeAusentismo) {
		this.porcentajeAusentismo = porcentajeAusentismo;
	}

	public BigDecimal getPorcentajeJustificado() {
		return porcentajeJustificado;
	}

	public void setPorcentajeJustificado(BigDecimal porcentajeJustificado) {
		this.porcentajeJustificado = porcentajeJustificado;
	}

	public BigDecimal getPorcentajeInjustificado() {
		return porcentajeInjustificado;
	}

	public void setPorcentajeInjustificado(BigDecimal porcentajeInjustificado) {
		this.porcentajeInjustificado = porcentajeInjustificado;
	}

	public Integer getTotalRegistros() {
		return totalRegistros;
	}

	public void setTotalRegistros(Integer totalRegistros) {
		this.totalRegistros = totalRegistros;
	}

	public List<AusentismoDTO> getDetalle() { return detalle; }
	public void setDetalle(List<AusentismoDTO> detalle) { this.detalle = detalle; }
}
