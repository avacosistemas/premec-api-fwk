package ar.com.avaco.premec.filter;

import java.util.ArrayList;
import java.util.List;

import ar.com.avaco.fwk.core.domain.filter.AbstractFilter;
import ar.com.avaco.fwk.core.domain.filter.FilterData;
import ar.com.avaco.fwk.core.domain.filter.FilterDataType;

public class ItemChecklistGrupoFilter extends AbstractFilter {

	private Long idGrupo;

	@Override
	public List<FilterData> getFilterDatas() {
		List<FilterData> list = new ArrayList<FilterData>();
		list.add(new FilterData("grupo.id", idGrupo, FilterDataType.EQUALS));
		return list;
	}

	public Long getIdGrupo() {
		return idGrupo;
	}

	public void setIdGrupo(Long idGrupo) {
		this.idGrupo = idGrupo;
	}

}
