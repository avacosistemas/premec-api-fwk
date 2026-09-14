package ar.com.avaco.premec.dto;

import java.util.ArrayList;
import java.util.List;

public class EmpleadoFichadoDTO {

	private List<EmpleadoFichados> fichados = new ArrayList<>();

	public List<EmpleadoFichados> getFichados() {
		return fichados;
	}

	public void setFichados(List<EmpleadoFichados> fichados) {
		this.fichados = fichados;
	}

}
