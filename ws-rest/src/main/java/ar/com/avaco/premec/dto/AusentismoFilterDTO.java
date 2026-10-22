package ar.com.avaco.premec.dto;

import java.util.List;

import ar.com.avaco.fwk.core.component.dto.SortPageDTO;

public class AusentismoFilterDTO extends SortPageDTO {
	private String fechaDesde;
	private String fechaHasta;
	private List<Long> empleadosIds;
	private String tipoAusentismo;
	private String justificacion;

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

	public List<Long> getEmpleadosIds() {
		return empleadosIds;
	}

	public void setEmpleadosIds(List<Long> empleadosIds) {
		this.empleadosIds = empleadosIds;
	}

	public String getTipoAusentismo() {
		return tipoAusentismo;
	}

	public void setTipoAusentismo(String tipoAusentismo) {
		this.tipoAusentismo = tipoAusentismo;
	}

	public String getJustificacion() {
		return justificacion;
	}

	public void setJustificacion(String justificacion) {
		this.justificacion = justificacion;
	}

}
