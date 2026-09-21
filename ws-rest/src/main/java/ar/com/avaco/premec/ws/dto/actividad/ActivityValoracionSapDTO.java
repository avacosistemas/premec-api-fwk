package ar.com.avaco.premec.ws.dto.actividad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ar.com.avaco.premec.ws.dto.businesspartner.BusinessPartnerSapDTO;
import ar.com.avaco.premec.ws.dto.employee.EmployeesInfoReponseSapDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityValoracionSapDTO {

	@JsonProperty("Details")
	private String details;

	@JsonProperty("ActivityCode")
	private Long activityCode;

	@JsonProperty("StartDate")
	private String startDate;

	@JsonProperty("U_detallevaloracion")
	private String detalleValoracion;

	@JsonProperty("BusinessPartner")
	private BusinessPartnerSapDTO businessPartner;

	@JsonProperty("EmployeeInfo")
	private EmployeesInfoReponseSapDTO employeeInfo;

	public EmployeesInfoReponseSapDTO getEmployeeInfo() {
		return employeeInfo;
	}

	public void setEmployeeInfo(EmployeesInfoReponseSapDTO employeeInfo) {
		this.employeeInfo = employeeInfo;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public Long getActivityCode() {
		return activityCode;
	}

	public void setActivityCode(Long activityCode) {
		this.activityCode = activityCode;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getDetalleValoracion() {
		return detalleValoracion;
	}

	public void setDetalleValoracion(String detalleValoracion) {
		this.detalleValoracion = detalleValoracion;
	}

	public BusinessPartnerSapDTO getBusinessPartner() {
		return businessPartner;
	}

	public void setBusinessPartner(BusinessPartnerSapDTO businessPartner) {
		this.businessPartner = businessPartner;
	}
}