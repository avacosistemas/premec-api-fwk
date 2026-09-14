package ar.com.avaco.premec.sap.dto;

public class ServiceCallCustomerStatsDTO {

	private String cliente;
	private Integer anio;
	private Integer mes;
	private Integer cantidad;
	private Boolean totalGeneral;

	public String getCliente() {
		return cliente;
	}

	public void setCliente(String cliente) {
		this.cliente = cliente;
	}

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

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public Boolean getTotalGeneral() {
		return totalGeneral;
	}

	public void setTotalGeneral(Boolean totalGeneral) {
		this.totalGeneral = totalGeneral;
	}

}
