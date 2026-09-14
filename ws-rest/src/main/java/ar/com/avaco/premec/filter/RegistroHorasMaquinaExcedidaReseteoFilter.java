package ar.com.avaco.premec.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ar.com.avaco.fwk.core.domain.filter.AbstractFilter;
import ar.com.avaco.fwk.core.domain.filter.FilterData;
import ar.com.avaco.fwk.core.domain.filter.FilterDataType;

public class RegistroHorasMaquinaExcedidaReseteoFilter extends AbstractFilter {

	private String internalSerialNum;

	private Long serviceCallId;

	private String tipo;

	private Date fechaDesde;

	private Date fechaHasta;

	public RegistroHorasMaquinaExcedidaReseteoFilter(RegistroHorasMaquinaExcedidaReseteoFilterDTO filter) {
		super(filter.getPageSize(), filter.getPage(), filter.getAsc(), filter.getIdx());
		this.internalSerialNum = filter.getInternalSerialNum();
		this.serviceCallId = filter.getServiceCallId();
		this.tipo = filter.getTipo();
		this.fechaDesde = filter.getFechaDesde();
		this.fechaHasta = filter.getFechaHasta();
	}

	@Override
	public List<FilterData> getFilterDatas() {
		List<FilterData> list = new ArrayList<FilterData>();
		if (StringUtils.isNotBlank(internalSerialNum)) {
			list.add(new FilterData("internalSerialNum", internalSerialNum, FilterDataType.EQUALS));
		}
		if (serviceCallId != null && serviceCallId > 0) {
			list.add(new FilterData("serviceCallId", serviceCallId, FilterDataType.EQUALS));
		}
		if (StringUtils.isNotBlank(tipo)) {
			list.add(new FilterData("tipo", tipo, FilterDataType.EQUALS));
		}
		
		if (fechaDesde != null) {
			list.add(new FilterData("fechaActual", fechaDesde, FilterDataType.EQUALS_MORE_THAN));
		}
		
		if (fechaHasta != null) {
			list.add(new FilterData("fechaActual", fechaHasta, FilterDataType.EQUALS_LESS_THAN));
		}
		
		return list;
	}

	public String getInternalSerialNum() {
		return internalSerialNum;
	}

	public void setInternalSerialNum(String internalSerialNum) {
		this.internalSerialNum = internalSerialNum;
	}

	public Long getServiceCallId() {
		return serviceCallId;
	}

	public void setServiceCallId(Long serviceCallId) {
		this.serviceCallId = serviceCallId;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Date getFechaDesde() {
		return fechaDesde;
	}

	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}

	public Date getFechaHasta() {
		return fechaHasta;
	}

	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}

}
