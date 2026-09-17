package ar.com.avaco.premec.ws.dto.timesheet;

import ar.com.avaco.fwk.core.component.dto.DTOEntity;

public class RegistroReciboPorUsuarioDTO extends DTOEntity<Long> {

	// Id del attachment
	private Long attachmentEntry;

	// Id de la linea del attachment
	private Long absEntry;

	private Integer year;

	private Integer month;

	private String monthString;

	private String tipo;

	private String descripcion;

	private String filePath;

	private Boolean firmado;

	private Long lineNum;

	private String empleado;

	public String getEmpleado() {
		return empleado;
	}

	public void setEmpleado(String empleado) {
		this.empleado = empleado;
	}

	public Long getLineNum() {
		return lineNum;
	}

	public void setLineNum(Long lineNum) {
		this.lineNum = lineNum;
	}

	public Boolean getFirmado() {
		return firmado;
	}

	public void setFirmado(Boolean firmado) {
		this.firmado = firmado;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Long getAbsEntry() {
		return absEntry;
	}

	public void setAbsEntry(Long absEntry) {
		this.absEntry = absEntry;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Long getAttachmentEntry() {
		return attachmentEntry;
	}

	public void setAttachmentEntry(Long attachmentEntry) {
		this.attachmentEntry = attachmentEntry;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public String getMonthString() {
		return monthString;
	}

	public void setMonthString(String monthString) {
		this.monthString = monthString;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	@Override
	public Long getId() {
		return null;
	}

	@Override
	public void setId(Long arg0) {
	}
	
}
