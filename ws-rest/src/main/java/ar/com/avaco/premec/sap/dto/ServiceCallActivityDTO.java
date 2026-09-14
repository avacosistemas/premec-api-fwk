package ar.com.avaco.premec.sap.dto;

public class ServiceCallActivityDTO {

	private Integer serviceCallId;
	private Integer activityCode;
	private String resolucion;
	private String fecha;
	private String horaInicio;
	private String horaFin;
	private String empleadoAsignado;
	private String estado;
	private String valoracion;
	private String supervisor;

	public Integer getServiceCallId() {
		return serviceCallId;
	}

	public void setServiceCallId(Integer serviceCallId) {
		this.serviceCallId = serviceCallId;
	}

	public Integer getActivityCode() {
		return activityCode;
	}

	public void setActivityCode(Integer activityCode) {
		this.activityCode = activityCode;
	}

	public String getResolucion() {
		return resolucion;
	}

	public void setResolucion(String resolucion) {
		this.resolucion = resolucion;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(String horaInicio) {
		this.horaInicio = horaInicio;
	}

	public String getHoraFin() {
		return horaFin;
	}

	public void setHoraFin(String horaFin) {
		this.horaFin = horaFin;
	}

	public String getEmpleadoAsignado() {
		return empleadoAsignado;
	}

	public void setEmpleadoAsignado(String empleadoAsignado) {
		this.empleadoAsignado = empleadoAsignado;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getValoracion() {
		return valoracion;
	}

	public void setValoracion(String valoracion) {
		this.valoracion = valoracion;
	}

	public String getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(String supervisor) {
		this.supervisor = supervisor;
	}
}