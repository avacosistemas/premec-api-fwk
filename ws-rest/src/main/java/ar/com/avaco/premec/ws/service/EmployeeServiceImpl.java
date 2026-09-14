package ar.com.avaco.premec.ws.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.core.exception.ErrorValidationException;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.ws.dto.employee.EmployeesInfoReponseSapDTO;

@Service("employeeService")
public class EmployeeServiceImpl extends AbstractSapService implements EmployeeService {

	@Override
	public EmployeesInfoReponseSapDTO getById(Long id) {

		String urlEmployee = urlSAP + "/EmployeesInfo(" + id + ")";

		ResponseEntity<EmployeesInfoReponseSapDTO> employee = null;

		try {
			employee = getRestTemplate().doExchange(urlEmployee, HttpMethod.GET, null,
					EmployeesInfoReponseSapDTO.class);
		} catch (SapBusinessException e) {
		    String fullStackTrace = SapBusinessException.generarStackTraceString(e);
			Map<String, String> errors = new HashMap<String, String>();
			errors.put("url", urlEmployee);
			errors.put("error", fullStackTrace);
			e.printStackTrace();
			throw new ErrorValidationException("Error al ejecutar el siguiente WS", errors);
		}

		return employee.getBody();
	}

	@Override
	public void updateNetoSueldoJornal(Long employeeId, BigDecimal neto, BigDecimal sueldoJornal) {
		
		String urlEmployee = urlSAP + "/EmployeesInfo(" + employeeId + ")";
		
		Map<String, Object> updateMap = new HashMap<String, Object>();
		updateMap.put("Salary", sueldoJornal);
		updateMap.put("EmployeeCosts", neto);
		
		HttpEntity<Map<String, Object>> httpEntityAttach = new HttpEntity<>(updateMap);
		
		try {
			getRestTemplate().doExchange(urlEmployee, HttpMethod.PATCH, httpEntityAttach,
					Object.class);
		} catch (SapBusinessException e) {
			String fullStackTrace = SapBusinessException.generarStackTraceString(e);
			Map<String, String> errors = new HashMap<String, String>();
			errors.put("url", urlEmployee);
			errors.put("error", fullStackTrace);
			e.printStackTrace();
			throw new ErrorValidationException("Error al ejecutar el siguiente WS", errors);
		}
		
	}

	

}
