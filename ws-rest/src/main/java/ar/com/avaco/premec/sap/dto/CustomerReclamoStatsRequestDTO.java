package ar.com.avaco.premec.sap.dto;

import java.util.List;

public class CustomerReclamoStatsRequestDTO {

	private List<String> clientes;

	private List<PeriodoDTO> periodos;

	public List<String> getClientes() {
		return clientes;
	}

	public void setClientes(List<String> clientes) {
		this.clientes = clientes;
	}

	public List<PeriodoDTO> getPeriodos() {
		return periodos;
	}

	public void setPeriodos(List<PeriodoDTO> periodos) {
		this.periodos = periodos;
	}

}
