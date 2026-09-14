package ar.com.avaco.premec.ws.dto;

import java.util.Date;

import ar.com.avaco.fwk.core.component.dto.DTOEntity;

public class RegistroHorasMaquinaExcedidaReseteoDTO extends DTOEntity<Long> {

	private Long id;

	private String internalSerialNum;

	private Integer maximoMensual;

	private Long serviceCallId;

	private Date fechaAnterior;

	private String fechaAnteriorString;

	private Double hsMaqAnterior;

	private Date fechaActual;

	private String fechaActualString;

	private Double horasMaquinaActual;

	private Double promedio;

	private String tipo;

	private Double horasMaquinaExcedidas;

	public Double getHorasMaquinaExcedidas() {
		return horasMaquinaExcedidas;
	}

	public void setHorasMaquinaExcedidas(Double horasMaquinaExcedidas) {
		this.horasMaquinaExcedidas = horasMaquinaExcedidas;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getInternalSerialNum() {
		return internalSerialNum;
	}

	public void setInternalSerialNum(String internalSerialNum) {
		this.internalSerialNum = internalSerialNum;
	}

	public Integer getMaximoMensual() {
		return maximoMensual;
	}

	public void setMaximoMensual(Integer maximoMensual) {
		this.maximoMensual = maximoMensual;
	}

	public Long getServiceCallId() {
		return serviceCallId;
	}

	public void setServiceCallId(Long serviceCallId) {
		this.serviceCallId = serviceCallId;
	}

	public Date getFechaAnterior() {
		return fechaAnterior;
	}

	public void setFechaAnterior(Date fechaAnterior) {
		this.fechaAnterior = fechaAnterior;
	}

	public Double getHsMaqAnterior() {
		return hsMaqAnterior;
	}

	public void setHsMaqAnterior(Double hsMaqAnterior) {
		this.hsMaqAnterior = hsMaqAnterior;
	}

	public Date getFechaActual() {
		return fechaActual;
	}

	public void setFechaActual(Date fechaActual) {
		this.fechaActual = fechaActual;
	}

	public Double getHorasMaquinaActual() {
		return horasMaquinaActual;
	}

	public void setHorasMaquinaActual(Double horasMaquinaActual) {
		this.horasMaquinaActual = horasMaquinaActual;
	}

	public Double getPromedio() {
		return promedio;
	}

	public void setPromedio(Double promedio) {
		this.promedio = promedio;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getFechaAnteriorString() {
		return fechaAnteriorString;
	}

	public void setFechaAnteriorString(String fechaAnteriorString) {
		this.fechaAnteriorString = fechaAnteriorString;
	}

	public String getFechaActualString() {
		return fechaActualString;
	}

	public void setFechaActualString(String fechaActualString) {
		this.fechaActualString = fechaActualString;
	}

}
