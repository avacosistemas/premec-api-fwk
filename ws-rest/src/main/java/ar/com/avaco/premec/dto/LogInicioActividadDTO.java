package ar.com.avaco.premec.dto;

import java.time.LocalDateTime;

public class LogInicioActividadDTO {

	private Integer id;
	private Integer activityId;
	private Integer serviceCallId;
	private String customerCode;
	private String customerName;
	private String internalSerialNum;
	private String itemCode;
	private String manufacturerSerialNum;
	private String customerEmail;
	private LocalDateTime fechaEvento;
	private Integer attendEmpl;
	private String tipoEvento;
	private String attendEmplName;
	private String observaciones;

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public String getAttendEmplName() {
		return attendEmplName;
	}

	public void setAttendEmplName(String attendEmplName) {
		this.attendEmplName = attendEmplName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	public Integer getServiceCallId() {
		return serviceCallId;
	}

	public void setServiceCallId(Integer serviceCallId) {
		this.serviceCallId = serviceCallId;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getInternalSerialNum() {
		return internalSerialNum;
	}

	public void setInternalSerialNum(String internalSerialNum) {
		this.internalSerialNum = internalSerialNum;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getManufacturerSerialNum() {
		return manufacturerSerialNum;
	}

	public void setManufacturerSerialNum(String manufacturerSerialNum) {
		this.manufacturerSerialNum = manufacturerSerialNum;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public LocalDateTime getFechaEvento() {
		return fechaEvento;
	}

	public void setFechaEvento(LocalDateTime fechaEvento) {
		this.fechaEvento = fechaEvento;
	}

	public Integer getAttendEmpl() {
		return attendEmpl;
	}

	public void setAttendEmpl(Integer attendEmpl) {
		this.attendEmpl = attendEmpl;
	}

	public String getTipoEvento() {
		return tipoEvento;
	}

	public void setTipoEvento(String tipoEvento) {
		this.tipoEvento = tipoEvento;
	}
}