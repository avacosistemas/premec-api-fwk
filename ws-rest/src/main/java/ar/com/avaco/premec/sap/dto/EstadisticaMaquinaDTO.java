package ar.com.avaco.premec.sap.dto;

import java.util.ArrayList;
import java.util.List;

public class EstadisticaMaquinaDTO {

	private List<MaquinaParadaPeriodoDTO> periodos = new ArrayList<MaquinaParadaPeriodoDTO>();

	private Integer cantidadReclamosTotal;

	private Integer diasParadaTotalTotal;

	public List<MaquinaParadaPeriodoDTO> getPeriodos() {
		return periodos;
	}

	public void setPeriodos(List<MaquinaParadaPeriodoDTO> periodos) {
		this.periodos = periodos;
	}

	public Integer getCantidadReclamosTotal() {
		return cantidadReclamosTotal;
	}

	public void setCantidadReclamosTotal(Integer cantidadReclamosTotal) {
		this.cantidadReclamosTotal = cantidadReclamosTotal;
	}

	public Integer getDiasParadaTotalTotal() {
		return diasParadaTotalTotal;
	}

	public void setDiasParadaTotalTotal(Integer diasParadaTotalTotal) {
		this.diasParadaTotalTotal = diasParadaTotalTotal;
	}

}
