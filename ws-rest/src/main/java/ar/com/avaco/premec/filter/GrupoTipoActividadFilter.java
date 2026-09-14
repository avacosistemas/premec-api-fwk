package ar.com.avaco.premec.filter;

import java.util.ArrayList;
import java.util.List;

import ar.com.avaco.fwk.core.domain.filter.AbstractFilter;
import ar.com.avaco.fwk.core.domain.filter.FilterData;
import ar.com.avaco.fwk.core.domain.filter.FilterDataType;
import ar.com.avaco.premec.domain.TipoActividad;

public class GrupoTipoActividadFilter extends AbstractFilter {

	private TipoActividad tipoActividad;

	@Override
	public List<FilterData> getFilterDatas() {
		List<FilterData> list = new ArrayList<FilterData>();
		list.add(new FilterData("tipo", tipoActividad, FilterDataType.EQUALS));
		return list;
	}

	public TipoActividad getTipoActividad() {
		return tipoActividad;
	}

	public void setTipoActividad(TipoActividad tipoActividad) {
		this.tipoActividad = tipoActividad;
	}

}
