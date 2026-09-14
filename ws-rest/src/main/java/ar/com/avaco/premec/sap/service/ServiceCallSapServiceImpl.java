package ar.com.avaco.premec.sap.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.core.utils.DateUtils;
import ar.com.avaco.premec.dto.ReclamoFilterDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallActivityDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallCustomerStatsDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallMachineStatsDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallReclamoListDTO;
import ar.com.avaco.premec.ws.service.AbstractSapService;
import ar.com.avaco.premec.ws.service.SQLServerConnection;

@Service
public class ServiceCallSapServiceImpl extends AbstractSapService implements ServiceCallSapService {

	@Autowired
	private SQLServerConnection sqlcon;

	@Override
	public List<ServiceCallActivityDTO> getActivitiesByServiceCall(Long serviceCallId) {

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT ").append("ServiceCallId, ").append("ActivityCode, ").append("Resolucion, ")
				.append("Fecha, ").append("HoraInicio, ").append("HoraFin, ").append("EmpleadoAsignado, ")
				.append("Estado, ").append("Valoracion, ").append("Supervisor ")
				.append("FROM VW_ServiceCallActivitiesReclamos ").append("WHERE ServiceCallId = ? ")
				.append("ORDER BY Fecha DESC, HoraInicio DESC");

		List<ServiceCallActivityDTO> result = new ArrayList<>();

		try (Connection conn = sqlcon.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			stmt.setLong(1, serviceCallId);

			try (ResultSet rs = stmt.executeQuery()) {

				while (rs.next()) {

					ServiceCallActivityDTO dto = new ServiceCallActivityDTO();

					dto.setServiceCallId(rs.getInt("ServiceCallId"));
					dto.setActivityCode(rs.getInt("ActivityCode"));
					dto.setResolucion(rs.getString("Resolucion"));
					dto.setFecha(rs.getString("Fecha"));
					dto.setHoraInicio(rs.getString("HoraInicio"));
					dto.setHoraFin(rs.getString("HoraFin"));
					dto.setEmpleadoAsignado(rs.getString("EmpleadoAsignado"));
					dto.setEstado(rs.getString("Estado"));
					dto.setValoracion(rs.getString("Valoracion"));
					dto.setSupervisor(rs.getString("Supervisor"));

					result.add(dto);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public List<ServiceCallReclamoListDTO> getServiceCalls(ReclamoFilterDTO filter) {

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT CustomerName,CustomerCode,ServiceCallID,Asunto,EstadoServiceCall,estadoReclamo, ")
				.append(" FechaCreacion,HoraCreacion,FechaInicioActividad,FechaFinActividad, ")
				.append(" EquipmentCardNum,ManufacturerSerialNum,InternalSN,ItemCode,ItemName, DetalleReclamoRechazado, ")
				.append(" COUNT(*) OVER() AS TotalRegistros ").append(" FROM VW_ServiceCalls_Reclamos WHERE 1 = 1 ");

		List<Object> params = new ArrayList<>();

		if (filter.getServiceCallID() != null) {
			sql.append("AND ServiceCallID = ?  ");
			params.add(filter.getServiceCallID());
		}

		if (filter.getCustomerCode() != null) {
			sql.append("AND CustomerCode like ?  ");
			params.add("%" + filter.getCustomerCode() + "%");
		}

		// --- Filtro por internal serial ---
		if (filter.getInternalSerialNum() != null && !filter.getInternalSerialNum().isEmpty()) {
			sql.append("AND InternalSN = ? ");
			params.add(filter.getInternalSerialNum());
		}

		// --- Filtro por fechas ---
		String campoFecha = null;

		if (filter.getTipoFecha() != null) {
			switch (filter.getTipoFecha()) {
			case "CREACION":
				campoFecha = "FechaCreacion";
				break;
			case "INICIO":
				campoFecha = "FechaInicioActividad";
				break;
			case "FIN":
				campoFecha = "FechaFinActividad";
				break;
			}
		}

		if (campoFecha != null) {

			if (filter.getFechaDesde() != null && !filter.getFechaDesde().isEmpty()
					&& parseDate(filter.getFechaDesde()) != null) {
				sql.append("AND ").append(campoFecha).append(" >= ? ");
				params.add(parseDate(filter.getFechaDesde()));
			}

			if (filter.getFechaHasta() != null && !filter.getFechaHasta().isEmpty()
					&& parseDate(filter.getFechaHasta()) != null) {
				sql.append("AND ").append(campoFecha).append(" <= ? ");
				params.add(parseDate(filter.getFechaHasta()));
			}
		}

		if (StringUtils.isNotBlank(filter.getEstado())) {
			sql.append(" AND estadoReclamo = '" + filter.getEstado() + "'");
		}

		int pageSize = filter.getPageSize() != null ? filter.getPageSize() : 20;

		int page = filter.getPage() == null || filter.getPage() == 0 ? 1 : filter.getPage();

		int offset = (page - 1) * pageSize;

		// --- Orden ---

		if (filter.getIdx() != null) {
			String orderDirection = Boolean.TRUE.equals(filter.getAsc()) ? "ASC" : "DESC";
			sql.append(" ORDER BY ").append(filter.getIdx()).append(" ").append(orderDirection);
		} else {
			sql.append("ORDER BY FechaCreacion DESC");
		}

		sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");

		params.add(offset);
		params.add(pageSize);

		List<ServiceCallReclamoListDTO> result = new ArrayList<>();

		try (Connection conn = sqlcon.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			// Set dinámico de parámetros
			for (int i = 0; i < params.size(); i++) {
				stmt.setObject(i + 1, params.get(i));
			}

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					result.add(mapRow(rs));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private ServiceCallReclamoListDTO mapRow(ResultSet rs) throws SQLException {

		ServiceCallReclamoListDTO dto = new ServiceCallReclamoListDTO();

		dto.setCustomerName(rs.getString("CustomerName"));
		dto.setCustomerCode(rs.getString("CustomerCode"));
		dto.setServiceCallID(rs.getInt("ServiceCallID"));
		dto.setAsunto(rs.getString("Asunto"));
		dto.setEstadoServiceCall(rs.getInt("EstadoServiceCall"));
		dto.setEstadoReclamo(rs.getString("estadoReclamo"));

		dto.setFechaCreacion(DateUtils.toString(rs.getDate("FechaCreacion"), "dd/MM/yyyy"));
		dto.setHoraCreacion(rs.getInt("HoraCreacion"));

		dto.setFechaInicioActividad(rs.getDate("FechaInicioActividad") != null
				? DateUtils.toString(rs.getDate("FechaInicioActividad"), "dd/MM/yyyy")
				: null);
		dto.setFechaFinActividad(rs.getDate("FechaFinActividad") != null
				? DateUtils.toString(rs.getDate("FechaFinActividad"), "dd/MM/yyyy")
				: null);

		dto.setEquipmentCardNum(rs.getInt("EquipmentCardNum"));
		dto.setManufacturerSerialNum(rs.getString("ManufacturerSerialNum"));
		dto.setInternalSN(rs.getString("InternalSN"));
		dto.setItemCode(rs.getString("ItemCode"));
		dto.setItemName(rs.getString("ItemName"));

		dto.setMotivoRechazo(rs.getString("DetalleReclamoRechazado"));

		dto.setTotalRegistros(rs.getInt("TotalRegistros"));

		return dto;
	}

	private java.sql.Date parseDate(String fecha) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			java.util.Date utilDate = sdf.parse(fecha);
			return new java.sql.Date(utilDate.getTime());
		} catch (Exception e) {

		}
		return null;
	}

	@Override
	public List<ServiceCallMachineStatsDTO> getEstadisticasMaquinaParada(String maquinasJson, String periodosJson) {

		StringBuilder sql = new StringBuilder();

		sql.append("EXEC SP_GetMultipeMachineReclamoStats ?, ?");

		List<ServiceCallMachineStatsDTO> result = new ArrayList<>();

		try (Connection conn = sqlcon.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			stmt.setString(1, maquinasJson);
			stmt.setString(2, periodosJson);

			try (ResultSet rs = stmt.executeQuery()) {

				while (rs.next()) {

					ServiceCallMachineStatsDTO dto = new ServiceCallMachineStatsDTO();

					dto.setMaquina(rs.getString("Maquina"));

					Object anioObj = rs.getObject("Anio");
					if (anioObj != null) {
						dto.setAnio(rs.getInt("Anio"));
					}

					Object mesObj = rs.getObject("Mes");
					if (mesObj != null) {
						dto.setMes(rs.getInt("Mes"));
					}

					dto.setCantidadReclamos(rs.getInt("CantidadReclamos"));

					dto.setDiasParadaTotal(rs.getInt("DiasParadaTotal"));

					dto.setTotalGeneral(rs.getBoolean("TotalGeneral"));

					result.add(dto);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	@Override
	public List<ServiceCallCustomerStatsDTO> getEstadisticasCliente(String clientesJson, String periodosJson) {

	    StringBuilder sql = new StringBuilder();

	    sql.append("EXEC SP_GetCustomerReclamoStats ?, ?");

	    List<ServiceCallCustomerStatsDTO> result = new ArrayList<>();

	    try (Connection conn = sqlcon.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

	        stmt.setString(1, clientesJson);
	        stmt.setString(2, periodosJson);

	        try (ResultSet rs = stmt.executeQuery()) {

	            while (rs.next()) {

	                ServiceCallCustomerStatsDTO dto = new ServiceCallCustomerStatsDTO();

	                dto.setCliente(rs.getString("Cliente"));

	                Object anioObj = rs.getObject("Anio");
	                if (anioObj != null) {
	                    dto.setAnio(rs.getInt("Anio"));
	                }

	                Object mesObj = rs.getObject("Mes");
	                if (mesObj != null) {
	                    dto.setMes(rs.getInt("Mes"));
	                }

	                dto.setCantidad(rs.getInt("Cantidad"));

	                dto.setTotalGeneral(rs.getBoolean("TotalGeneral"));

	                result.add(dto);
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return result;
	}

}
