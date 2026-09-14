package ar.com.avaco.premec.sap.dto;

import java.util.List;

public class MachineReclamoStatsRequestDTO {

	private String cuit;
	private List<String> maquinas;
	private List<PeriodoDTO> periodos;

	public String getCuit() {
		return cuit;
	}

	public void setCuit(String cuit) {
		this.cuit = cuit;
	}

	public List<String> getMaquinas() {
		return maquinas;
	}

	public void setMaquinas(List<String> maquinas) {
		this.maquinas = maquinas;
	}

	public List<PeriodoDTO> getPeriodos() {
		return periodos;
	}

	public void setPeriodos(List<PeriodoDTO> periodos) {
		this.periodos = periodos;
	}
}
