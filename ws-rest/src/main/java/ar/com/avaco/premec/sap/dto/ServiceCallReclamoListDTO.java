package ar.com.avaco.premec.sap.dto;

import ar.com.avaco.fwk.core.component.dto.DTOEntity;

public class ServiceCallReclamoListDTO extends DTOEntity<Long> {

	private String customerName;
	private String customerCode;
	private Integer serviceCallID;
	private String asunto;
	private Integer estadoServiceCall;
	private String estadoReclamo;

	private String fechaCreacion;
	private Integer horaCreacion;

	private String fechaInicioActividad;
	private String fechaFinActividad;

	private Integer equipmentCardNum;
	private String manufacturerSerialNum;
	private String internalSN;
	private String itemCode;
	private String itemName;

	private String motivoRechazo;

	private Integer totalRegistros;

	public Integer getTotalRegistros() {
		return totalRegistros;
	}

	public void setTotalRegistros(Integer totalRegistros) {
		this.totalRegistros = totalRegistros;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	public Integer getServiceCallID() {
		return serviceCallID;
	}

	public void setServiceCallID(Integer serviceCallID) {
		this.serviceCallID = serviceCallID;
	}

	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}

	public Integer getEstadoServiceCall() {
		return estadoServiceCall;
	}

	public void setEstadoServiceCall(Integer estadoServiceCall) {
		this.estadoServiceCall = estadoServiceCall;
	}

	public String getEstadoReclamo() {
		return estadoReclamo;
	}

	public void setEstadoReclamo(String estadoReclamo) {
		this.estadoReclamo = estadoReclamo;
	}

	public Integer getHoraCreacion() {
		return horaCreacion;
	}

	public void setHoraCreacion(Integer horaCreacion) {
		this.horaCreacion = horaCreacion;
	}

	public Integer getEquipmentCardNum() {
		return equipmentCardNum;
	}

	public void setEquipmentCardNum(Integer equipmentCardNum) {
		this.equipmentCardNum = equipmentCardNum;
	}

	public String getManufacturerSerialNum() {
		return manufacturerSerialNum;
	}

	public void setManufacturerSerialNum(String manufacturerSerialNum) {
		this.manufacturerSerialNum = manufacturerSerialNum;
	}

	public String getInternalSN() {
		return internalSN;
	}

	public void setInternalSN(String internalSN) {
		this.internalSN = internalSN;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getFechaInicioActividad() {
		return fechaInicioActividad;
	}

	public void setFechaInicioActividad(String fechaInicioActividad) {
		this.fechaInicioActividad = fechaInicioActividad;
	}

	public String getFechaFinActividad() {
		return fechaFinActividad;
	}

	public void setFechaFinActividad(String fechaFinActividad) {
		this.fechaFinActividad = fechaFinActividad;
	}

	public String getMotivoRechazo() {
		return motivoRechazo;
	}

	public void setMotivoRechazo(String motivoRechazo) {
		this.motivoRechazo = motivoRechazo;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	@Override
	public void setId(Long id) {
	}

	@Override
	public Long getId() {
		return null;
	}

}
