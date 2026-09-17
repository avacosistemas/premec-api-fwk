package ar.com.avaco.premec.ws.dto;

import java.util.List;

import ar.com.avaco.fwk.core.component.dto.SortPageDTO;

public class ReciboFilterDTO extends SortPageDTO {

	private String tipoRecibo;

	private String referencia;

	private List<Long> empleadosIds;

	private Integer anio;

	private List<Integer> meses;

	private Boolean firmado;

	public List<Long> getEmpleadosIds() {
		return empleadosIds;
	}

	public void setEmpleadosIds(List<Long> empleadosIds) {
		this.empleadosIds = empleadosIds;
	}

	public Integer getAnio() {
		return anio;
	}

	public void setAnio(Integer anio) {
		this.anio = anio;
	}

	public List<Integer> getMeses() {
		return meses;
	}

	public void setMeses(List<Integer> meses) {
		this.meses = meses;
	}

	public Boolean getFirmado() {
		return firmado;
	}

	public void setFirmado(Boolean firmado) {
		this.firmado = firmado;
	}

	public String getTipoRecibo() {
		return tipoRecibo;
	}

	public void setTipoRecibo(String tipoRecibo) {
		this.tipoRecibo = tipoRecibo;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

}
