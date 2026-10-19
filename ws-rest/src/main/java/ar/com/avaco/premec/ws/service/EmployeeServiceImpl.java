package ar.com.avaco.premec.ws.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.core.exception.ErrorValidationException;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.ws.dto.employee.EmployeesInfoReponseSapDTO;

@Service("employeeService")
public class EmployeeServiceImpl extends AbstractSapService implements EmployeeService {

	@Autowired
	private SQLServerConnection sqlConnection;

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
			getRestTemplate().doExchange(urlEmployee, HttpMethod.PATCH, httpEntityAttach, Object.class);
		} catch (SapBusinessException e) {
			String fullStackTrace = SapBusinessException.generarStackTraceString(e);
			Map<String, String> errors = new HashMap<String, String>();
			errors.put("url", urlEmployee);
			errors.put("error", fullStackTrace);
			e.printStackTrace();
			throw new ErrorValidationException("Error al ejecutar el siguiente WS", errors);
		}

	}

	@Override
	public List<EmployeesInfoReponseSapDTO> getAllEmployees() {

		List<EmployeesInfoReponseSapDTO> empleados = new ArrayList<EmployeesInfoReponseSapDTO>();
		
		String sql = "SELECT " + "empId, lastName, " + "firstName, " + "U_serviciotecnico, " + "U_objetivo, "
				+ "U_horainicio, " + "U_horafin, " + "atcEntry " + "FROM OHEM ";

		try (Connection conn = sqlConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			try (ResultSet rs = stmt.executeQuery()) {

				while (rs.next()) {

					EmployeesInfoReponseSapDTO employee = new EmployeesInfoReponseSapDTO();

					employee.setIdEmpleado(rs.getLong("empId"));
					employee.setLastName(rs.getString("lastName"));
					employee.setFirstName(rs.getString("firstName"));
					employee.setServicioTecnico(rs.getString("U_serviciotecnico"));
					employee.setObjetivo(rs.getString("U_objetivo"));
					employee.setHoraInicio(rs.getString("U_horainicio"));
					employee.setHoraFin(rs.getString("U_horafin"));

					long attachmentEntry = rs.getLong("atcEntry");
					if (!rs.wasNull()) {
						employee.setAttachmentEntry(attachmentEntry);
					}
					
					empleados.add(employee);
					
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Map<String, String> errors = new HashMap<String, String>();
			errors.put("error", SapBusinessException.generarStackTraceString(e));
			throw new ErrorValidationException("Error al consultar el empleado en SAP", errors);
		}

		return empleados;
	}

}
