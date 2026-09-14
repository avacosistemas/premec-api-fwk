package ar.com.avaco.premec.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.commons.service.mail.MailSenderSMTPService;
import ar.com.avaco.premec.dto.LogInicioActividadDTO;
import ar.com.avaco.premec.ws.service.SQLServerConnection;

@Service("notificacionReclamoService")
public class NotificacionReclamoServiceImpl implements NotificacionReclamoService {

	@Autowired
	private MailSenderSMTPService mailSenderSMTPService;

	@Autowired
	private SQLServerConnection sqlcon;

	@Value("${mail.notificacion.subject.asignacion.reclamo}")
	private String subjectAsignacionReclamo;

	@Value("${mail.notificacion.body.asignacion.reclamo}")
	private String bodyAsignacionReclamo;

	@Value("${mail.notificacion.subject.cierre.reclamo}")
	private String subjectCierreReclamo;

	@Value("${mail.notificacion.body.cierre.reclamo}")
	private String bodyCierreReclamo;

	@Value("${mail.notificacion.subject.rechazo.reclamo}")
	private String subjectRechazoReclamo;

	@Value("${mail.notificacion.body.rechazo.reclamo}")
	private String bodyRechazoReclamo;

	@Value("${reclamos.mail.creacion.subject}")
	private String subjectCreacionReclamo;

	@Value("${reclamos.mail.creacion.body}")
	private String bodyCreacionReclamo;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Value("${reclamos.mail.test}")
	private String mailTest;

	@Value("${email.test}")
	private boolean test;

	@Override
	public void enviarNotificaciones() {

		List<LogInicioActividadDTO> logsInicioActividad = getLogsInicioActividad();
		List<Integer> idsToDelete = new ArrayList<>();

		for (LogInicioActividadDTO item : logsInicioActividad) {

			// Templates
			String subjectTemplate;
			String bodyTemplate;

			if ("inicio actividad".equals(item.getTipoEvento())) {
				subjectTemplate = subjectAsignacionReclamo;
				bodyTemplate = bodyAsignacionReclamo;
			} else if ("rechazado".equals(item.getTipoEvento())) {
				subjectTemplate = subjectRechazoReclamo;
				bodyTemplate = bodyRechazoReclamo;
			} else if ("CREACION".equals(item.getTipoEvento())) {
				subjectTemplate = subjectCreacionReclamo;
				bodyTemplate = bodyCreacionReclamo;
			} else {
				subjectTemplate = subjectCierreReclamo;
				bodyTemplate = bodyCierreReclamo;
			}

			// Valores null-safe
			String nroReclamo = String.valueOf(item.getServiceCallId());
			String nroMaquina = Objects.toString(item.getInternalSerialNum(), "");
			String cliente = Objects.toString(item.getCustomerName(), "");
			String empleado = Objects.toString(item.getAttendEmplName(), "");
			String observaciones = Objects.toString(item.getObservaciones(), "");

			String fecha = item.getFechaEvento() != null ? item.getFechaEvento().format(FORMATTER) : "";

			// Reemplazos (una sola cadena base)
			String subject = subjectTemplate.replace("{nroReclamo}", nroReclamo).replace("{nroMaquina}", nroMaquina);

			String body = bodyTemplate.replace("{nroReclamo}", nroReclamo).replace("{nroMaquina}", nroMaquina)
					.replace("{cliente}", cliente).replace("{fecha}", fecha).replace("{empleado}", empleado)
					.replace("{observaciones}", observaciones);
			// Envío
			String to = item.getCustomerEmail();

			if (StringUtils.isBlank(to)) {
				to = "reportesservicios@premecsa.com.ar";
				String warning = "<strong style='color: red;'>Contrato sin email asignado</strong><br><br>";
				body = warning + body;
			}
			
			if (test)
				to = mailTest;

			this.mailSenderSMTPService.sendMail("reportesservicios@premecsa.com.ar", to, "servicios@premecsa.com.ar", subject, body, null);

			idsToDelete.add(item.getId());
		}

		deleteEventosBatch(idsToDelete);
	}

	public void deleteEventosBatch(List<Integer> ids) {

		if (ids == null || ids.isEmpty())
			return;

		StringBuilder sql = new StringBuilder("DELETE FROM EVENTOS_RECLAMOS WHERE ID IN (");

		for (int i = 0; i < ids.size(); i++) {
			sql.append("?");
			if (i < ids.size() - 1)
				sql.append(",");
		}
		sql.append(")");

		try (Connection conn = sqlcon.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < ids.size(); i++) {
				stmt.setInt(i + 1, ids.get(i));
			}

			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<LogInicioActividadDTO> getLogsInicioActividad() {

		List<LogInicioActividadDTO> result = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		List<Object> params = new ArrayList<>();

		sql.append("SELECT ");
		sql.append(" ID, ");
		sql.append(" ActivityId, ");
		sql.append(" ServiceCallId, ");
		sql.append(" CustomerCode, ");
		sql.append(" CustomerName, ");
		sql.append(" InternalSerialNum, ");
		sql.append(" ItemCode, ");
		sql.append(" ManufacturerSerialNum, ");
		sql.append(" CustomerEmail, ");
		sql.append(" FechaEvento, ");
		sql.append(" AttendEmpl, ");
		sql.append(" TipoEvento, ");
		sql.append(" AttendEmplName, ");
		sql.append(" Observaciones ");
		sql.append("FROM EVENTOS_RECLAMOS ");
		sql.append("WHERE 1=1 ");
		sql.append(" ORDER BY FechaEvento DESC ");

		try (Connection conn = sqlcon.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			for (int i = 0; i < params.size(); i++) {
				stmt.setObject(i + 1, params.get(i));
			}

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					result.add(mapRow(rs)); // reutilizás tu mapper
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private LogInicioActividadDTO mapRow(ResultSet rs) throws SQLException {

		LogInicioActividadDTO dto = new LogInicioActividadDTO();

		dto.setId(rs.getInt("ID"));
		dto.setActivityId((Integer) rs.getObject("ActivityId"));
		dto.setServiceCallId((Integer) rs.getObject("ServiceCallId"));
		dto.setCustomerCode(rs.getString("CustomerCode"));
		dto.setCustomerName(rs.getString("CustomerName"));
		dto.setInternalSerialNum(rs.getString("InternalSerialNum"));
		dto.setItemCode(rs.getString("ItemCode"));
		dto.setManufacturerSerialNum(rs.getString("ManufacturerSerialNum"));
		dto.setCustomerEmail(rs.getString("CustomerEmail"));

		if (rs.getTimestamp("FechaEvento") != null) {
			dto.setFechaEvento(rs.getTimestamp("FechaEvento").toLocalDateTime());
		}

		dto.setAttendEmpl((Integer) rs.getObject("AttendEmpl"));
		dto.setTipoEvento(rs.getString("TipoEvento"));
		dto.setAttendEmplName(rs.getString("AttendEmplName"));
		dto.setObservaciones(rs.getString("Observaciones"));

		return dto;
	}

}
