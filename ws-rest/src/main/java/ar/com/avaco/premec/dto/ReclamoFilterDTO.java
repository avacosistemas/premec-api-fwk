package ar.com.avaco.premec.dto;

import ar.com.avaco.fwk.core.component.dto.SortPageDTO;

public class ReclamoFilterDTO extends SortPageDTO {

	private Long serviceCallID;
	private String customerCode;
	private String internalSerialNum;
	private String fechaDesde;
	private String fechaHasta;
	private String tipoFecha;
	private String estado;

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getInternalSerialNum() {
		return internalSerialNum;
	}

	public void setInternalSerialNum(String internalSerialNum) {
		this.internalSerialNum = internalSerialNum;
	}

	public String getFechaDesde() {
		return fechaDesde;
	}

	public void setFechaDesde(String fechaDesde) {
		this.fechaDesde = fechaDesde;
	}

	public String getFechaHasta() {
		return fechaHasta;
	}

	public void setFechaHasta(String fechaHasta) {
		this.fechaHasta = fechaHasta;
	}

	public String getTipoFecha() {
		return tipoFecha;
	}

	public void setTipoFecha(String tipoFecha) {
		this.tipoFecha = tipoFecha;
	}

	public Long getServiceCallID() {
		return serviceCallID;
	}

	public void setServiceCallID(Long serviceCallID) {
		this.serviceCallID = serviceCallID;
	}

}
