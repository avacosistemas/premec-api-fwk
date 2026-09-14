package ar.com.avaco.premec.sap.dto;

import java.util.ArrayList;
import java.util.List;

public class EstadisticaClienteDTO {

	private Integer total = 0;

	private List<ClientePeriodoDTO> periodos = new ArrayList<>();

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public List<ClientePeriodoDTO> getPeriodos() {
		return periodos;
	}

	public void setPeriodos(List<ClientePeriodoDTO> periodos) {
		this.periodos = periodos;
	}

}
