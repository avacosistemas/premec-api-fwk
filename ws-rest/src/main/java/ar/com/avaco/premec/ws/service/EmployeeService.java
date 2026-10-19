package ar.com.avaco.premec.ws.service;

import java.math.BigDecimal;
import java.util.List;

import ar.com.avaco.premec.ws.dto.employee.EmployeesInfoReponseSapDTO;

public interface EmployeeService {

	EmployeesInfoReponseSapDTO getById(Long id);

	void updateNetoSueldoJornal(Long employeeId, BigDecimal neto, BigDecimal sueldoJornal);

	List<EmployeesInfoReponseSapDTO> getAllEmployees();

}
