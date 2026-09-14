package ar.com.avaco.premec.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.commons.service.mail.MailSenderSMTPService;
import ar.com.avaco.fwk.core.utils.DateUtils;
import ar.com.avaco.premec.ws.service.SQLServerConnection;

@Service("notificacionVencimientoService")
public class NotificacionVencimientoServiceImpl implements NotificacionVencimientoService {

	@Autowired
	private MailSenderSMTPService mailSenderSMTPService;

	@Autowired
	private SQLServerConnection sqlcon;

	@Value("${mail.notificacion.subject.vencimiento.proximo}")
	private String subjectVencimientoProximo;

	@Value("${mail.notificacion.body.vencimiento.proximo}")
	private String bodyVencimientoProximo;

	@Value("${mail.notificacion.subject.vencimiento.hoy}")
	private String subjectVencimientoHoy;

	@Value("${mail.notificacion.body.vencimiento.hoy}")
	private String bodyVencimientoHoy;

	@Value("${mail.notificacion.subject.vencimiento.pasado}")
	private String subjectVencimientoPasado;

	@Value("${mail.notificacion.body.vencimiento.pasado}")
	private String bodyVencimientoPasado;

	@Value("${reclamos.mail.test}")
	private String mailTest;

	@Value("${dias.aviso.vencimiento.cliente}")
	private String diasAviso;

	@Value("${email.test}")
	private boolean test;

	@Override
	public void enviarNotificaciones() {

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT ");
		sql.append("    CardCode AS CUIT, ");
		sql.append("    CardName AS Cliente, ");
		sql.append("    ValidTo AS Fecha, ");
		sql.append("    U_correoreclamos AS Correo, ");
		sql.append("    DATEDIFF(DAY, GETDATE(), ValidTo) AS DiasParaVencimiento, ");
		sql.append("    CASE ");
		sql.append("        WHEN ValidTo < CAST(GETDATE() AS DATE) THEN 'VENCIDO' ");
		sql.append("        WHEN ValidTo = CAST(GETDATE() AS DATE) THEN 'VENCE HOY' ");
		sql.append("        ELSE 'PROXIMO A VENCER' ");
		sql.append("    END AS Estado ");
		sql.append("FROM OCRD ");
		sql.append("WHERE ValidTo IS NOT NULL ");
		sql.append("AND ( ");
		sql.append("    ValidTo <= CAST(GETDATE() AS DATE) ");
		sql.append("    OR DATEDIFF(DAY, GETDATE(), ValidTo) IN (");
		sql.append(diasAviso);
		sql.append(") ");
		sql.append(") ");
		sql.append("ORDER BY ValidTo ");

		try (Connection conn = sqlcon.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql.toString());
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {

				String cuit = rs.getString("CUIT");
				String cliente = rs.getString("Cliente");
				String correo = rs.getString("Correo");
				Date fecha = rs.getDate("Fecha");
				Integer diasParaVencimiento = rs.getInt("DiasParaVencimiento");
				String estado = rs.getString("Estado");

				String subject;
				String body;

				switch (estado) {

				case "VENCIDO":
					subject = subjectVencimientoPasado;
					body = bodyVencimientoPasado;
					break;
				case "VENCE HOY":
					subject = subjectVencimientoHoy;
					body = bodyVencimientoHoy;
					break;
				case "PROXIMO A VENCER":
					subject = subjectVencimientoProximo;
					body = bodyVencimientoProximo;
					break;
				default:
					continue;
				}

				body = body.replace("{CUIT}", cuit).replace("{cliente}", cliente)
						.replace("{fecha}", DateUtils.toString(fecha, DateUtils.PATTERN_ddMMyyyy))
						.replace("{diasParaVencimiento}", diasParaVencimiento.toString());

				if (test)
					correo = mailTest;

				this.mailSenderSMTPService.sendMail("reportesservicios@premecsa.com.ar", correo,
						"servicios@premecsa.com.ar", subject, body, null);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
