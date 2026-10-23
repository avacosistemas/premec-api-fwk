package ar.com.avaco.premec.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.ibm.icu.util.Calendar;

import ar.com.avaco.fwk.core.utils.DateUtils;
import ar.com.avaco.premec.domain.GrupoEmpleado;
import ar.com.avaco.premec.domain.UsuarioPremec;
import ar.com.avaco.premec.dto.IndicadorAusentismoDTO;
import ar.com.avaco.premec.dto.AusentismoDTO;
import ar.com.avaco.premec.dto.AusentismoAgrupadoDTO;
import ar.com.avaco.premec.dto.AusentismoFilterDTO;
import ar.com.avaco.premec.dto.TipoAusentismoDTO;
import ar.com.avaco.premec.dto.RegistroInformeMensualEmpleadoDTO;
import ar.com.avaco.premec.dto.RegistroInformeMensualEmpleadoIndividualDTO;
import ar.com.avaco.premec.dto.RegistroInformeMensualGeneralDTO;
import ar.com.avaco.premec.ws.dto.actividad.ActivityValoracionSapDTO;
import ar.com.avaco.premec.ws.dto.businesspartner.BusinessPartnerSapDTO;
import ar.com.avaco.premec.ws.dto.employee.EmployeesInfoReponseSapDTO;
import ar.com.avaco.premec.ws.service.AbstractSapService;
import ar.com.avaco.premec.ws.service.SQLServerConnection;

@Service("indicadorMensualService")
public class IndicadorMensualServiceImpl extends AbstractSapService implements IndicadorMensualService {

	@Override
	public List<TipoAusentismoDTO> getTiposAusentismo() {
		List<TipoAusentismoDTO> resultado = new ArrayList<>();
		String sql = "SELECT DISTINCT LTRIM(RTRIM(ActType)) AS codigo, LaborItem AS nombre, Chargeable "
				+ "FROM PMC5 WHERE NULLIF(LTRIM(RTRIM(ActType)), '') IS NOT NULL " + "ORDER BY LaborItem ASC";
		try (Connection conn = sqlcon.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				TipoAusentismoDTO tipo = new TipoAusentismoDTO();
				tipo.setCodigo(rs.getString("codigo"));
				tipo.setNombre(rs.getString("nombre"));
				String chargeable = rs.getString("Chargeable");
				tipo.setJustificado(chargeable == null ? null : "Y".equalsIgnoreCase(chargeable.trim()));
				resultado.add(tipo);
			}
		} catch (Exception e) {
			throw new RuntimeException("No se pudieron consultar los tipos de ausentismo", e);
		}
		return resultado;
	}

	@Override
	public List<AusentismoAgrupadoDTO> getAusentismos(AusentismoFilterDTO filter) {
		Map<Long, AusentismoAgrupadoDTO> porEmpleado = new LinkedHashMap<>();
		List<String> tiposAusentismo = StringUtils.isBlank(filter.getTipoAusentismo()) ? new ArrayList<>()
				: Arrays.stream(filter.getTipoAusentismo().split(",")).map(String::trim)
						.filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
		List<Long> idsSap = null;
		if (filter.getEmpleadosIds() != null && !filter.getEmpleadosIds().isEmpty()) {
			idsSap = usuarioPremecService.getByIds(filter.getEmpleadosIds()).stream().filter(Objects::nonNull)
					.map(u -> parseUsuarioSap(u.getUsuariosap())).filter(Objects::nonNull).distinct()
					.collect(Collectors.toList());
			if (idsSap.isEmpty())
				return new ArrayList<>();
		}
		StringBuilder sql = new StringBuilder("WITH base AS (SELECT cab.UserID AS idEmpleado, "
				+ "LTRIM(RTRIM(CONCAT(E.firstName, ' ', E.lastName))) AS empleado, det.[Date] AS fecha, "
				+ "tipo.LaborItem AS tipoAusentismo, tipo.Chargeable AS chargeable, "
				+ "CASE WHEN aus.TipoAusentismo LIKE 'FERI%' THEN 1 ELSE 0 END AS esFeriado, "
				+ "ISNULL(det.EffectHr, 0) AS horas, ISNULL(dbo.fnHorasDecimal(det.U_tarde), 0) AS llegadasTarde, "
				+ "CASE WHEN tipo.LaborItem IS NOT NULL AND aus.TipoAusentismo NOT LIKE 'FERI%' THEN 1 ELSE 0 END AS esAusencia, "
				+ "CASE WHEN tipo.LaborItem IS NOT NULL ");
		if (!tiposAusentismo.isEmpty())
			sql.append("AND LTRIM(RTRIM(tipo.ActType)) IN (")
					.append(tiposAusentismo.stream().map(tipo -> "?").collect(Collectors.joining(","))).append(") ");
		if ("true".equalsIgnoreCase(filter.getJustificacion()))
			sql.append("AND tipo.Chargeable = 'Y' ");
		if ("false".equalsIgnoreCase(filter.getJustificacion()))
			sql.append("AND tipo.Chargeable = 'N' ");
		sql.append("THEN 1 ELSE 0 END AS incluirDetalle FROM OTSH cab "
				+ "INNER JOIN TSH1 det ON det.AbsEntry = cab.AbsEntry " + "INNER JOIN OHEM E ON cab.UserID = E.empID "
				+ "CROSS APPLY (SELECT NULLIF(LTRIM(RTRIM(CAST(det.U_tipoausentismo AS NVARCHAR(MAX)))), '') AS TipoAusentismo) aus "
				+ "OUTER APPLY (SELECT TOP (1) cfg.ActType, cfg.LaborItem, cfg.Chargeable FROM PMC5 cfg "
				+ "WHERE aus.TipoAusentismo IS NOT NULL "
				+ "AND NULLIF(LTRIM(RTRIM(cfg.ActType)), '') IS NOT NULL "
				+ "AND LEFT(aus.TipoAusentismo, LEN(LTRIM(RTRIM(cfg.ActType)))) COLLATE SQL_Latin1_General_CP850_CI_AS = LTRIM(RTRIM(cfg.ActType)) COLLATE SQL_Latin1_General_CP850_CI_AS "
				+ "ORDER BY LEN(LTRIM(RTRIM(cfg.ActType))) DESC) tipo WHERE 1 = 1 ");
		if (StringUtils.isNotBlank(filter.getFechaDesde()))
			sql.append("AND det.[Date] >= ? ");
		if (StringUtils.isNotBlank(filter.getFechaHasta()))
			sql.append("AND det.[Date] < ? ");
		if (idsSap != null)
			sql.append("AND cab.UserID IN (").append(idsSap.stream().map(x -> "?").collect(Collectors.joining(",")))
					.append(") ");
		sql.append("), resumen AS (SELECT idEmpleado, empleado, SUM(CASE WHEN esFeriado = 0 THEN horas ELSE 0 END) AS horasTotales, "
				+ "SUM(CASE WHEN esFeriado = 0 THEN llegadasTarde ELSE 0 END) AS llegadasTarde, "
				+ "SUM(CASE WHEN esAusencia = 1 THEN horas ELSE 0 END) AS horasAusentes, "
				+ "SUM(CASE WHEN esAusencia = 1 AND chargeable = 'Y' THEN horas ELSE 0 END) AS horasJustificadas, "
				+ "SUM(CASE WHEN esAusencia = 1 AND chargeable = 'N' THEN horas ELSE 0 END) AS horasInjustificadas "
				+ "FROM base GROUP BY idEmpleado, empleado "
				+ "HAVING SUM(CASE WHEN esAusencia = 1 THEN horas ELSE 0 END) > 0), "
				+ "empleados_pagina AS (SELECT resumen.*, COUNT(*) OVER() AS totalRegistros, "
				+ "ROW_NUMBER() OVER (ORDER BY ");
		String orderBy;
		if (filter.getIdx() == null)
			orderBy = "empleado ASC, idEmpleado ASC";
		else {
			switch (filter.getIdx()) {
			case "idEmpleado":
				orderBy = "idEmpleado";
				break;
			case "empleado":
				orderBy = "empleado";
				break;
			case "horasTotales":
				orderBy = "horasTotales";
				break;
			case "horasAusentes":
				orderBy = "horasAusentes";
				break;
			default:
				orderBy = "empleado";
			}
			orderBy += Boolean.TRUE.equals(filter.getAsc()) ? " ASC" : " DESC";
			if (!"idEmpleado".equals(filter.getIdx()))
				orderBy += ", idEmpleado ASC";
		}
		int page = filter.getPage() == null || filter.getPage() < 1 ? 1 : filter.getPage();
		int pageSize = filter.getPageSize() == null || filter.getPageSize() < 1 ? 20 : filter.getPageSize();
		sql.append(orderBy).append(") AS rn FROM resumen) "
				+ "SELECT ep.totalRegistros, ep.idEmpleado, ep.empleado, ep.horasTotales, ep.horasAusentes, "
				+ "ep.horasJustificadas, ep.horasInjustificadas, ep.llegadasTarde, b.fecha, b.tipoAusentismo, b.chargeable, b.horas "
				+ "FROM empleados_pagina ep INNER JOIN base b ON b.idEmpleado = ep.idEmpleado AND (b.incluirDetalle = 1"
				+ (StringUtils.isBlank(filter.getJustificacion()) && tiposAusentismo.isEmpty() ? " OR b.esFeriado = 1" : "") + ") "
				+ "WHERE ep.rn > ? AND ep.rn <= ? ORDER BY ep.rn, b.fecha ASC");
		try (Connection conn = sqlcon.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
			int p = 1;
			for (String tipoAusentismo : tiposAusentismo)
				stmt.setString(p++, tipoAusentismo);
			if (StringUtils.isNotBlank(filter.getFechaDesde()))
				stmt.setDate(p++, java.sql.Date.valueOf(parseFechaAusentismo(filter.getFechaDesde())));
			if (StringUtils.isNotBlank(filter.getFechaHasta()))
				stmt.setDate(p++, java.sql.Date.valueOf(parseFechaAusentismo(filter.getFechaHasta()).plusDays(1)));
			if (idsSap != null)
				for (Long id : idsSap)
					stmt.setLong(p++, id);
			stmt.setInt(p++, (page - 1) * pageSize);
			stmt.setInt(p++, page * pageSize);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Long idEmpleado = rs.getLong("idEmpleado");
					AusentismoAgrupadoDTO dto = porEmpleado.get(idEmpleado);
					if (dto == null) {
						BigDecimal total = valorDecimal(rs.getBigDecimal("horasTotales"));
						BigDecimal ausentes = valorDecimal(rs.getBigDecimal("horasAusentes"));
						BigDecimal justificadas = valorDecimal(rs.getBigDecimal("horasJustificadas"));
						BigDecimal injustificadas = valorDecimal(rs.getBigDecimal("horasInjustificadas"));
						dto = new AusentismoAgrupadoDTO();
						dto.setIdEmpleado(idEmpleado);
						dto.setEmpleado(rs.getString("empleado"));
						dto.setHorasTotales(formatearHorasAusentes(total));
						dto.setHorasAusentes(formatearHorasAusentes(ausentes));
						dto.setHorasJustificadas(formatearHorasAusentes(justificadas));
						dto.setHorasInjustificadas(formatearHorasAusentes(injustificadas));
						dto.setLlegadasTarde(formatearHorasAusentes(valorDecimal(rs.getBigDecimal("llegadasTarde"))));
						dto.setPorcentajeAusentismo(porcentajeAusentismo(ausentes, total));
						dto.setPorcentajeJustificado(porcentajeAusentismo(justificadas, ausentes));
						dto.setPorcentajeInjustificado(porcentajeAusentismo(injustificadas, ausentes));
						dto.setTotalRegistros(rs.getInt("totalRegistros"));
						porEmpleado.put(idEmpleado, dto);
					}
					AusentismoDTO detalle = new AusentismoDTO();
					detalle.setFecha(rs.getDate("fecha").toLocalDate()
							.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
					detalle.setTipoAusentismo(rs.getString("tipoAusentismo"));
					String chargeable = rs.getString("chargeable");
					detalle.setJustificado(chargeable == null ? null : "Y".equalsIgnoreCase(chargeable.trim()));
					detalle.setHorasAusentes(formatearHorasAusentes(rs.getBigDecimal("horas")));
					dto.getDetalle().add(detalle);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("No se pudo consultar el listado de ausentismos", e);
		}
		return new ArrayList<>(porEmpleado.values());
	}

	private BigDecimal valorDecimal(BigDecimal valor) {
		return valor == null ? BigDecimal.ZERO : valor;
	}

	private Long parseUsuarioSap(String usuarioSap) {
		if (StringUtils.isBlank(usuarioSap))
			return null;
		try {
			return Long.valueOf(usuarioSap.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private BigDecimal porcentajeAusentismo(BigDecimal horas, BigDecimal total) {
		return total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
				: horas.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
	}

	private LocalDate parseFechaAusentismo(String fecha) {
		try {
			return LocalDate.parse(fecha, java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu")
					.withResolverStyle(java.time.format.ResolverStyle.STRICT));
		} catch (java.time.format.DateTimeParseException e) {
			return LocalDate.parse(fecha);
		}
	}

	private String formatearHorasAusentes(BigDecimal horas) {
		long segundosTotales = (horas == null ? BigDecimal.ZERO : horas).multiply(BigDecimal.valueOf(3600))
				.setScale(0, RoundingMode.HALF_UP).longValue();
		long horasTotales = segundosTotales / 3600;
		long minutos = (segundosTotales % 3600) / 60;
		long segundos = segundosTotales % 60;
		return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", horasTotales, minutos, segundos);
	}

	@Autowired
	private SQLServerConnection sqlcon;

	@Autowired
	private UsuarioPremecService usuarioPremecService;

	@Autowired
	private GrupoEmpleadoService grupoEmpleadoService;

	@Value(value = "${exclusiones.actividades.calculo.horas.netas}")
	private String exclusiones;

	@Override
	public List<ActivityValoracionSapDTO> getActividadesValoracion(Integer mes, Integer anio) {
		return getActividadesValoracionPorEmpleados(new ArrayList<Long>(), mes, anio);
	}

	@Override
	public List<ActivityValoracionSapDTO> getActividadesValoracionPorGrupoEmpleado(Long idGrupoEmpleado, Integer mes,
			Integer anio) {
		GrupoEmpleado grupoEmpleado = grupoEmpleadoService.get(idGrupoEmpleado);
		List<Long> ids = grupoEmpleado.getUsuarios().stream().map(UsuarioPremec::getId).filter(Objects::nonNull)
				.map(Long::valueOf).collect(Collectors.toList());
		return getActividadesValoracionPorEmpleados(ids, mes, anio);
	}

	@Override
	public List<ActivityValoracionSapDTO> getActividadesValoracion(String username, Integer mes, Integer anio) {

		UsuarioPremec usuario = usuarioPremecService.getByUsername(username);
		List<Long> ids = Arrays.asList(usuario.getId());
		return getActividadesValoracionPorEmpleados(ids, mes, anio);
	}

	@Override
	public List<ActivityValoracionSapDTO> getActividadesValoracionPorEmpleados(List<Long> idsEmpleados, Integer mes,
			Integer anio) {

		List<UsuarioPremec> byIds = usuarioPremecService.getByIds(idsEmpleados);

		List<String> idsUsuariosSap = byIds.stream().filter(x -> StringUtils.isNotBlank(x.getUsuariosap()))
				.map(UsuarioPremec::getUsuariosap).map(String::trim).collect(Collectors.toList());

		LocalDate fechaDesde = LocalDate.of(anio, mes, 1);
		LocalDate fechaHasta = fechaDesde.plusMonths(1);

		String fechaDesdeSql = fechaDesde.toString().replace("-", "");
		String fechaHastaSql = fechaHasta.toString().replace("-", "");

		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT ").append(" O.Details AS details, ").append(" O.ClgCode AS activityCode, ")
				.append(" O.recontact AS startDate, ").append(" O.U_detallevaloracion AS detalleValoracion, ")
				.append(" BP.CardName AS cardName, ").append(" E.firstName AS firstName, ")
				.append(" E.lastName AS lastName ").append(" FROM OCLG O ")
				.append(" LEFT JOIN OCRD BP ON BP.CardCode = O.CardCode ")
				.append(" LEFT JOIN OHEM E ON E.empID = O.AttendEmpl ").append(" WHERE O.U_Valoracion = 'M' ")
				.append(" AND CAST(O.U_categoria AS NVARCHAR(MAX)) = 'Mantenimiento-reparacion' ")
				.append(" AND CAST(O.U_estado AS NVARCHAR(MAX)) IS NOT NULL ")
				.append(" AND CAST(O.U_estado AS NVARCHAR(MAX)) <> 'Pendiente' ")
				.append(" AND O.recontact >= '{fechaDesde}' ").append(" AND O.recontact < '{fechaHasta}' ");

		if (idsUsuariosSap != null && !idsUsuariosSap.isEmpty()) {
			sql.append(" AND O.AttendEmpl IN ({idUsuarioSap}) ");
		}

		sql.append(" ORDER BY O.CntctDate, O.ClgCode ");

		String sqlString = sql.toString().replace("{fechaDesde}", fechaDesdeSql).replace("{fechaHasta}", fechaHastaSql)
				.replace("{idUsuarioSap}", String.join(",", idsUsuariosSap));

		List<ActivityValoracionSapDTO> list = new ArrayList<ActivityValoracionSapDTO>();

		try (Connection conn = sqlcon.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sqlString);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {

				ActivityValoracionSapDTO activity = new ActivityValoracionSapDTO();

				activity.setDetails(rs.getString("details"));
				activity.setActivityCode(rs.getLong("activityCode"));
				activity.setStartDate(rs.getString("startDate"));
				activity.setDetalleValoracion(rs.getString("detalleValoracion"));

				BusinessPartnerSapDTO businessPartner = new BusinessPartnerSapDTO();
				businessPartner.setCardName(rs.getString("cardName"));
				activity.setBusinessPartner(businessPartner);

				EmployeesInfoReponseSapDTO employeeInfo = new EmployeesInfoReponseSapDTO();
				employeeInfo.setFirstName(rs.getString("firstName"));
				employeeInfo.setLastName(rs.getString("lastName"));
				activity.setEmployeeInfo(employeeInfo);

				list.add(activity);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	/**
	 * Indicador general mensual agrupado por todos los empleados para un periodo en
	 * particular.
	 */
	@Override
	public RegistroInformeMensualGeneralDTO getIndicadorMensualGeneral(String mes, String anio) {

		String fechaDesde = anio + StringUtils.leftPad(mes, 2, "0") + "01";

		Calendar fecha = Calendar.getInstance();
		fecha.setTime(DateUtils.toDate(fechaDesde, "yyyyMMdd"));
		fecha.add(Calendar.MONTH, 1);
		fecha.add(Calendar.DAY_OF_MONTH, -1);

		String fechaHasta = DateUtils.toString(fecha.getTime(), "yyyyMMdd");

		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT ")
				// ============================================================
				// HORAS DEL TIMESHEET
				// ============================================================
				.append("     SUM(T.facturables) AS facturables, ")
				.append("     dbo.fnFormatHoras(SUM(T.facturables)) AS facturablesHora, ")

				.append("     ( ").append("         SUM(T.fichado) - SUM(T.facturables) ").append("     ) AS ociosas, ")

				.append("     dbo.fnFormatHoras( ").append("         SUM(T.fichado) - SUM(T.facturables) ")
				.append("     ) AS ociosasHora, ")

				.append("     SUM(T.fichado) AS fichado, ")

				.append("     dbo.fnFormatHoras(SUM(T.fichado)) AS fichadoHora, ")

				.append("     CAST( ").append("         CAST( ").append("             (SUM(T.facturables) * 100.0) / ")
				.append("             NULLIF(SUM(T.fichado), 0) ").append("             AS DECIMAL(10,2) ")
				.append("         ) AS VARCHAR(10) ").append("     ) + '%' AS efectividad, ")

				// ============================================================
				// ACTIVIDADES VALORADAS
				// ============================================================
				.append("     SUM(ISNULL(O.cantMB, 0)) AS cantMB, ").append("     SUM(ISNULL(O.cantB, 0)) AS cantB, ")
				.append("     SUM(ISNULL(O.cantR, 0)) AS cantR, ").append("     SUM(ISNULL(O.cantM, 0)) AS cantM, ")
				.append("     SUM(ISNULL(O.cantSV, 0)) AS cantSV, ")
				.append("     SUM(ISNULL(O.cantidadActividades, 0)) AS cantidadActividades, ")

				// ============================================================
				// PORCENTAJE DE VALORACION
				// MB = 1
				// B = 1
				// R = 5
				// M = 15
				// ============================================================
				.append("     FORMAT( ").append("         CAST( ").append("             NULLIF( ")
				.append("                 SUM(ISNULL(O.cantMB, 0)) + ")
				.append("                 SUM(ISNULL(O.cantB, 0)), ").append("                 0 ")
				.append("             ) AS DECIMAL(10,2) ").append("         ) / ").append("         NULLIF( ")
				.append("             CAST( ").append("                 (SUM(ISNULL(O.cantMB, 0)) * 1) + ")
				.append("                 (SUM(ISNULL(O.cantB, 0)) * 1) + ")
				.append("                 (SUM(ISNULL(O.cantR, 0)) * 5) + ")
				.append("                 (SUM(ISNULL(O.cantM, 0)) * 15) ").append("             AS DECIMAL(10,2)), ")
				.append("             0 ").append("         ), ").append("         'P2' ")
				.append("     ) AS porcentajeValoracion ")

				// ============================================================
				// EMPLEADOS
				// ============================================================
				.append(" FROM ( ")

				// ============================================================
				// AGRUPAMOS OTSH + TSH1 POR EMPLEADO
				// De esta forma queda una sola fila por empleado y no se
				// multiplican las actividades de OCLG.
				// ============================================================
				.append("     SELECT ").append("         cab.UserID, ")

				.append("         ISNULL(SUM(det.BillableHr), 0) AS facturables, ")

				.append("         ISNULL(SUM(CASE ").append("             WHEN det.u_tipoausentismo IS NULL ")
				.append("                  OR CAST(det.u_tipoausentismo AS NVARCHAR(MAX)) = '' ")
				.append("             THEN det.EffectHr ").append("             ELSE 0 ")
				.append("         END), 0) AS fichado ")

				.append("     FROM OTSH cab ")

				.append("     LEFT JOIN TSH1 det ").append("         ON cab.AbsEntry = det.AbsEntry ")
				.append("        AND (det.u_tipoausentismo IS NULL OR LTRIM(RTRIM(CAST(det.u_tipoausentismo AS NVARCHAR(MAX)))) NOT LIKE 'FERI%') ")

				.append("     WHERE cab.DateFrom = '{fechaDesde}' ")

				.append("     GROUP BY cab.UserID ")

				.append(" ) T ")

				.append(" INNER JOIN OHEM E ").append("     ON T.UserID = E.empID ")

				// ============================================================
				// ACTIVIDADES OCLG
				// Una sola fila por empleado.
				// ============================================================
				.append(" LEFT JOIN ( ")

				.append("     SELECT ").append("         AttendEmpl, ")
				.append("         SUM(CASE WHEN U_Valoracion IS NOT NULL THEN 1 ELSE 0 END) AS cantidadActividades, ")

				.append("         SUM(CASE ").append("             WHEN U_Valoracion = 'MB' THEN 1 ")
				.append("             ELSE 0 ").append("         END) AS cantMB, ")

				.append("         SUM(CASE ").append("             WHEN U_Valoracion = 'B' THEN 1 ")
				.append("             ELSE 0 ").append("         END) AS cantB, ")

				.append("         SUM(CASE ").append("             WHEN U_Valoracion = 'R' THEN 1 ")
				.append("             ELSE 0 ").append("         END) AS cantR, ")

				.append("         SUM(CASE ").append("             WHEN U_Valoracion = 'M' THEN 1 ")
				.append("             ELSE 0 ").append("         END) AS cantM, ")

				.append("         SUM(CASE ").append("             WHEN U_Valoracion IS NULL THEN 1 ")
				.append("             ELSE 0 ").append("         END) AS cantSV ")

				.append("     FROM OCLG ")

				.append("     WHERE Recontact BETWEEN '{fechaDesde}' AND '{fechaHasta}' ")
				.append("     AND CAST(U_categoria AS NVARCHAR(MAX)) = 'Mantenimiento-reparacion' ")
				.append("     AND CAST(U_estado AS NVARCHAR(MAX)) IS NOT NULL ")
				.append("     AND CAST(U_estado AS NVARCHAR(MAX)) <> 'Pendiente' ");

		List<String> exclusionesList = Lists.newArrayList(exclusiones.split(","));

		for (String exclusion : exclusionesList) {
			sql.append(" AND Details NOT LIKE '%{exclusion}%' ".replace("{exclusion}", exclusion));
		}

		sql.append("     GROUP BY AttendEmpl ")

				.append(" ) O ")

				.append("     ON O.AttendEmpl = E.empID ");

		String sqlString = sql.toString().replace("{fechaDesde}", fechaDesde).replace("{fechaHasta}", fechaHasta);

		RegistroInformeMensualGeneralDTO preview = null;

		try (Connection conn = sqlcon.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sqlString);
				ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {

				preview = new RegistroInformeMensualGeneralDTO();

				BigDecimal facturables = rs.getBigDecimal("facturables");
				preview.setFacturables(facturables != null ? facturables.toString() : "");

				preview.setFacturablesHora(rs.getString("facturablesHora"));

				BigDecimal ociosas = rs.getBigDecimal("ociosas");
				preview.setOciosas(ociosas != null ? ociosas.toString() : "");

				preview.setOciosasHora(rs.getString("ociosasHora"));

				BigDecimal fichado = rs.getBigDecimal("fichado");
				preview.setFichado(fichado != null ? fichado.toString() : "");

				preview.setFichadoHora(rs.getString("fichadoHora"));

				preview.setEfectividad(rs.getString("efectividad"));

				preview.setCantMB(rs.getInt("cantMB"));

				preview.setCantB(rs.getInt("cantB"));

				preview.setCantR(rs.getInt("cantR"));

				preview.setCantM(rs.getInt("cantM"));

				preview.setCantSV(rs.getInt("cantSV"));

				preview.setCantidadActividades(rs.getInt("cantidadActividades"));

				preview.setPorcentajeValoracion(rs.getString("porcentajeValoracion"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (preview != null) {
			preview.setAusentismo(agregarAusentismo(obtenerAusentismo(mes, anio, null).values()));
		}

		return preview;
	}

	@Override
	public RegistroInformeMensualEmpleadoDTO getIndicadorMensualIndividual(String mes, String anio, String username) {

		String idUsuarioSap = usuarioPremecService.getUsuarioSAP(username);

		String fechaDesde = anio + StringUtils.leftPad(mes, 2, "0") + "01";

		Calendar fecha = Calendar.getInstance();
		fecha.setTime(DateUtils.toDate(fechaDesde, "yyyyMMdd"));
		fecha.add(Calendar.MONTH, 1);
		fecha.add(Calendar.DAY_OF_MONTH, -1);

		String fechaHasta = DateUtils.toString(fecha.getTime(), "yyyyMMdd");

		List<UsuarioPremec> usuarios = usuarioPremecService.list();

		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT ")

				// DATOS EMPLEADO
				.append("     MAX(E.empID) AS usuarioSap, ")
				.append("     MAX(E.firstName + ' ' + E.lastName) AS nombre, ")

				// HORAS
				.append("     ISNULL(SUM(dbo.fnHorasDecimal(det.U_tarde)), 0) AS llegadasTarde, ")
				.append("     ISNULL(SUM(det.BillableHr), 0) AS facturables, ")

				.append("     dbo.fnFormatHoras(ISNULL(SUM(det.BillableHr), 0)) AS facturablesHora, ")

				.append("     ( ").append("         ISNULL(SUM(CASE ")
				.append("             WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
				.append("             THEN det.EffectHr ELSE 0 ").append("         END), 0) ")
				.append("         - ISNULL(SUM(det.BillableHr), 0) ").append("     ) AS ociosas, ")

				.append("     dbo.fnFormatHoras( ").append("         ( ").append("             ISNULL(SUM(CASE ")
				.append("                 WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
				.append("                 THEN det.EffectHr ELSE 0 ").append("             END), 0) ")
				.append("             - ISNULL(SUM(det.BillableHr), 0) ").append("         ) ")
				.append("     ) AS ociosasHora, ")

				.append("     ISNULL(SUM(CASE ")
				.append("         WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
				.append("         THEN det.EffectHr ELSE 0 ").append("     END), 0) AS fichado, ")

				.append("     dbo.fnFormatHoras(ISNULL(SUM(CASE ")
				.append("         WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
				.append("         THEN det.EffectHr ELSE 0 ").append("     END), 0)) AS fichadoHora, ")

				// EFECTIVIDAD
				.append("     CAST( ").append("         CAST( ")
				.append("             (ISNULL(SUM(det.BillableHr), 0) * 100.0) / ").append("             NULLIF( ")
				.append("                 ISNULL(SUM(CASE ")
				.append("                     WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
				.append("                     THEN det.EffectHr ELSE 0 ").append("                 END), 0), ")
				.append("                 0 ").append("             ) AS DECIMAL(10,2) ")
				.append("         ) AS VARCHAR(10) ").append("     ) + '%' AS efectividad, ")

				// VALORACIONES
				.append("     MAX(ISNULL(O.cantMB, 0)) AS cantMB, ").append("     MAX(ISNULL(O.cantB, 0)) AS cantB, ")
				.append("     MAX(ISNULL(O.cantR, 0)) AS cantR, ").append("     MAX(ISNULL(O.cantM, 0)) AS cantM, ")
				.append("     MAX(ISNULL(O.cantSV, 0)) AS cantSV, ")
				.append("     MAX(ISNULL(O.cantidadActividades, 0)) AS cantidadActividades, ")

				// PORCENTAJE VALORACION
				// Se calcula sobre los valores agregados de OCLG,
				// sin multiplicarlos por la cantidad de registros de TSH1.
				.append("     FORMAT( ").append("         CAST( ").append("             NULLIF( ")
				.append("                 MAX(ISNULL(O.cantMB, 0)) + MAX(ISNULL(O.cantB, 0)), ")
				.append("                 0 ").append("             ) AS DECIMAL(10,2) ").append("         ) / ")
				.append("         NULLIF( ").append("             CAST( ")
				.append("                 (MAX(ISNULL(O.cantMB, 0)) * 1) + ")
				.append("                 (MAX(ISNULL(O.cantB, 0)) * 1) + ")
				.append("                 (MAX(ISNULL(O.cantR, 0)) * 5) + ")
				.append("                 (MAX(ISNULL(O.cantM, 0)) * 15) ").append("             AS DECIMAL(10,2)), ")
				.append("             0 ").append("         ), ").append("         'P2' ")
				.append("     ) AS porcentajeValoracion, ")

				// DATOS ADICIONALES
				.append("     MAX(E.U_Objetivo) AS objetivoActividades, ")
				.append("     MAX(ISNULL(cab.U_cumplimientoobjetivo, '')) AS cumplimientoObjetivo, ")
				.append("     MAX(ISNULL(cab.U_salario, 0)) AS salario, ")
				.append("     MAX(ISNULL(E.salaryunit, '')) AS unidadSalario, ")
				.append("     MAX(cab.u_viaticos) AS viaticos, ").append("     MAX(cab.U_adelanto) AS adelanto, ")
				.append("     MAX(cab.U_prestamo) AS prestamo, ").append("     MAX(cab.u_aumento) AS aumento, ")
				.append("     MAX(cab.u_premio) AS premio, ")
				.append("     MAX(ISNULL(cab.U_sueldoneto, 0)) AS sueldoNeto ")

				// EMPLEADO COMO TABLA PRINCIPAL
				.append(" FROM OHEM E ")

				// CABECERA DEL TIMESHEET DEL MES, SI EXISTE
				.append(" LEFT JOIN OTSH cab ").append("     ON cab.UserID = E.empID ")
				.append("     AND cab.DateFrom = '{fechaDesde}' ")

				// DETALLE DEL TIMESHEET
				.append(" LEFT JOIN TSH1 det ").append("     ON cab.AbsEntry = det.AbsEntry ")
				.append("    AND (det.u_tipoausentismo IS NULL OR LTRIM(RTRIM(CAST(det.u_tipoausentismo AS NVARCHAR(MAX)))) NOT LIKE 'FERI%') ")

				// ACTIVIDADES DEL MES
				.append(" LEFT JOIN ( ").append("     SELECT ").append("         AttendEmpl, ")
				.append("         SUM(CASE WHEN U_Valoracion IS NOT NULL THEN 1 ELSE 0 END) AS cantidadActividades, ")
				.append("         SUM(CASE WHEN U_Valoracion = 'MB' THEN 1 ELSE 0 END) AS cantMB, ")
				.append("         SUM(CASE WHEN U_Valoracion = 'B' THEN 1 ELSE 0 END) AS cantB, ")
				.append("         SUM(CASE WHEN U_Valoracion = 'R' THEN 1 ELSE 0 END) AS cantR, ")
				.append("         SUM(CASE WHEN U_Valoracion = 'M' THEN 1 ELSE 0 END) AS cantM, ")
				.append("         SUM(CASE WHEN U_Valoracion IS NULL THEN 1 ELSE 0 END) AS cantSV ")
				.append("     FROM OCLG ").append("     WHERE Recontact BETWEEN '{fechaDesde}' AND '{fechaHasta}' ")
				.append("     AND CAST(U_categoria AS NVARCHAR(MAX)) = 'Mantenimiento-reparacion' ")
				.append("     AND CAST(U_estado AS NVARCHAR(MAX)) IS NOT NULL ")
				.append("     AND CAST(U_estado AS NVARCHAR(MAX)) <> 'Pendiente' ");

		List<String> exclusionesList = Lists.newArrayList(exclusiones.split(","));

		for (String exclusion : exclusionesList) {
			sql.append(" AND Details NOT LIKE '%{exclusion}%' ".replace("{exclusion}", exclusion));
		}

		sql.append("     GROUP BY AttendEmpl ").append(" ) O ").append("     ON O.AttendEmpl = E.empID ")

				// FILTRO SOLAMENTE SOBRE EL EMPLEADO
				.append(" WHERE E.empID = {idUsuarioSap} ");

		String sqlString = sql.toString().replace("{fechaDesde}", fechaDesde).replace("{fechaHasta}", fechaHasta)
				.replace("{idUsuarioSap}", idUsuarioSap);

		RegistroInformeMensualEmpleadoDTO preview = null;

		try (Connection conn = sqlcon.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sqlString);
				ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {

				preview = new RegistroInformeMensualEmpleadoDTO();

				preview.setUsuarioSap(rs.getLong("usuarioSap"));
				preview.setNombre(rs.getString("nombre"));
				preview.setLlegadasTarde(formatearHorasAusentes(rs.getBigDecimal("llegadasTarde")));

				BigDecimal facturables = rs.getBigDecimal("facturables");
				preview.setFacturables(facturables != null ? facturables.toString() : "0");

				preview.setFacturablesHora(rs.getString("facturablesHora"));

				BigDecimal ociosas = rs.getBigDecimal("ociosas");
				preview.setOciosas(ociosas != null ? ociosas.toString() : "0");

				preview.setOciosasHora(rs.getString("ociosasHora"));

				BigDecimal fichado = rs.getBigDecimal("fichado");
				preview.setFichado(fichado != null ? fichado.toString() : "0");

				preview.setFichadoHora(rs.getString("fichadoHora"));

				preview.setEfectividad(rs.getString("efectividad"));

				preview.setCantMB(rs.getInt("cantMB"));
				preview.setCantB(rs.getInt("cantB"));
				preview.setCantR(rs.getInt("cantR"));
				preview.setCantM(rs.getInt("cantM"));
				preview.setCantSV(rs.getInt("cantSV"));

				preview.setViaticos(rs.getString("viaticos"));
				preview.setAdelanto(rs.getString("adelanto"));
				preview.setPrestamo(rs.getString("prestamo"));
				preview.setGratificacionesAumentos(rs.getString("aumento"));
				preview.setPremioAsistencia(rs.getString("premio"));

				preview.setCantidadActividades(rs.getInt("cantidadActividades"));

				preview.setPorcentajeValoracion(rs.getString("porcentajeValoracion"));

				preview.setObjetivoActividades(rs.getInt("objetivoActividades"));

				preview.setCumplimientoObjetivo(rs.getString("cumplimientoObjetivo"));

				BigDecimal salariobd = rs.getBigDecimal("salario");

				preview.setSalario(salariobd != null ? salariobd.toString() : "");

				preview.setUnidadSalario(rs.getString("unidadSalario"));

				BigDecimal sueldoNeto = rs.getBigDecimal("sueldoNeto");

				preview.setSueldoNeto(
						sueldoNeto != null ? sueldoNeto.setScale(2, RoundingMode.HALF_UP).toString() : "");

				String usuarioSap = preview.getUsuarioSap().toString();

				Optional<UsuarioPremec> usuario = usuarios.stream().filter(x -> x.getUsuariosap().equals(usuarioSap))
						.findFirst();

				if (usuario.isPresent()) {
					preview.setLegajo(usuario.get().getLegajo());
				} else {
					preview.setLegajo(-1L);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (preview != null && StringUtils.isNotBlank(idUsuarioSap)) {
			UsuarioPremec usuarioIndicador = new UsuarioPremec();
			usuarioIndicador.setUsuariosap(idUsuarioSap);
			Map<Long, IndicadorAusentismoDTO> ausentismo = obtenerAusentismo(mes, anio,
					java.util.Collections.singletonList(usuarioIndicador));
			preview.setAusentismo(ausentismo.getOrDefault(Long.valueOf(idUsuarioSap),
					crearAusentismo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));
		}

		return preview;
	}

	@Override
	public List<RegistroInformeMensualEmpleadoIndividualDTO> getIndicadoresGrupoEmpleado(String mes, String anio,
			Long idGrupoEmpleado) {

		GrupoEmpleado grupoEmpleado = grupoEmpleadoService.get(idGrupoEmpleado);
		List<UsuarioPremec> usuarios = new ArrayList<UsuarioPremec>(grupoEmpleado.getUsuarios());
		List<RegistroInformeMensualEmpleadoIndividualDTO> indicadores = getIndicadoresByUsuarios(mes, anio, usuarios);
		completarAusentismo(indicadores, obtenerAusentismo(mes, anio, usuarios));
		return indicadores;

	}

	@Override
	public List<RegistroInformeMensualEmpleadoIndividualDTO> getIndicadoresEmpleados(String mes, String anio,
			String idsEmpleados) {

		List<Long> listaIds = Arrays.stream(idsEmpleados.split(",")).map(String::trim).filter(s -> !s.isEmpty())
				.map(Long::valueOf).collect(Collectors.toList());

		List<UsuarioPremec> byIds = usuarioPremecService.getByIds(listaIds);
		List<RegistroInformeMensualEmpleadoIndividualDTO> indicadores = getIndicadoresByUsuarios(mes, anio, byIds);
		completarAusentismo(indicadores, obtenerAusentismo(mes, anio, byIds));
		return indicadores;

	}

	@Override
	public RegistroInformeMensualEmpleadoIndividualDTO getIndicadoresGrupoEmpleadoAgrupado(String mes, String anio,
			Long idGrupoEmpleado) {

		GrupoEmpleado grupoEmpleado = grupoEmpleadoService.get(idGrupoEmpleado);
		List<UsuarioPremec> usuarios = new ArrayList<UsuarioPremec>(grupoEmpleado.getUsuarios());
		RegistroInformeMensualEmpleadoIndividualDTO indicador = getIndicadoresByUsuariosAgrupado(mes, anio, usuarios);
		if (indicador != null) {
			indicador.setAusentismo(agregarAusentismo(obtenerAusentismo(mes, anio, usuarios).values()));
		}
		return indicador;

	}

	@Override
	public RegistroInformeMensualEmpleadoIndividualDTO getIndicadoresEmpleadosAgrupado(String mes, String anio,
			String idsEmpleados) {

		List<Long> listaIds = Arrays.stream(idsEmpleados.split(",")).map(String::trim).filter(s -> !s.isEmpty())
				.map(Long::valueOf).collect(Collectors.toList());

		List<UsuarioPremec> byIds = usuarioPremecService.getByIds(listaIds);
		RegistroInformeMensualEmpleadoIndividualDTO indicador = getIndicadoresByUsuariosAgrupado(mes, anio, byIds);
		if (indicador != null) {
			indicador.setAusentismo(agregarAusentismo(obtenerAusentismo(mes, anio, byIds).values()));
		}
		return indicador;

	}

	private List<RegistroInformeMensualEmpleadoIndividualDTO> getIndicadoresByUsuarios(String mes, String anio,
			List<UsuarioPremec> users) {

		List<UsuarioPremec> usuarios = usuarioPremecService.list();

		String idsUsuarioSap = users.stream().filter(x -> StringUtils.isNotBlank(x.getUsuariosap()))
				.map((UsuarioPremec::getUsuariosap)).map(String::trim).collect(Collectors.joining(","));

		String fechaDesde = anio + StringUtils.leftPad(mes, 2, "0") + "01";
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(DateUtils.toDate(fechaDesde, "yyyyMMdd"));
		fecha.add(Calendar.MONTH, 1);
		fecha.add(Calendar.DAY_OF_MONTH, -1);
		String fechaHasta = DateUtils.toString(fecha.getTime(), "yyyyMMdd");

		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT ").append(" cab.UserID AS usuarioSap, ")
				.append(" E.firstName + ' ' + E.lastName AS nombre, ").append(" SUM(det.BillableHr) AS facturables, ")
				.append(" dbo.fnFormatHoras(SUM(det.BillableHr)) AS facturablesHora, ")

				// .append(" SUM(det.NonBillHr) AS ociosas, ")
				// .append(" dbo.fnFormatHoras(SUM(det.NonBillHr)) AS ociosasHora, ")

				.append("     ( ").append("         SUM(CASE ")
				.append("             WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
				.append("             THEN det.EffectHr ELSE 0 ").append("         END) ")
				.append("         - SUM(det.BillableHr) ").append("     ) AS ociosas, ")

				.append("     dbo.fnFormatHoras( ").append("         ( ").append("             SUM(CASE ")
				.append("                 WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
				.append("                 THEN det.EffectHr ELSE 0 ").append("             END) ")
				.append("             - SUM(det.BillableHr) ").append("         ) ").append("     ) AS ociosasHora, ")

				.append(" SUM(CASE WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
				.append("      THEN det.EffectHr ELSE 0 END) AS fichado, ")
				.append(" dbo.fnFormatHoras(SUM(CASE WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
				.append("      THEN det.EffectHr ELSE 0 END)) AS fichadoHora, ")
				.append(" CAST(CAST((SUM(det.BillableHr) * 100.0) / ")
				.append("      NULLIF(SUM(CASE WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
				.append("      THEN det.EffectHr ELSE 0 END), 0) ")
				.append(" AS DECIMAL(10,2)) AS VARCHAR(10)) + '%' AS efectividad, ")
				.append(" ISNULL(O.cantMB, 0) AS cantMB, ").append(" ISNULL(O.cantB, 0) AS cantB, ")
				.append(" ISNULL(O.cantR, 0) AS cantR, ").append(" ISNULL(O.cantM, 0) AS cantM, ")
				.append(" ISNULL(O.cantSV, 0) AS cantSV, ")
				.append(" ISNULL(O.cantidadActividades, 0) AS cantidadActividades, ").append(" FORMAT( ")
				.append("   CAST(NULLIF(ISNULL(O.cantMB,0) + ISNULL(O.cantB,0), 0) AS DECIMAL(10,2)) / ")
				.append("   CAST(((ISNULL(O.cantMB,0) * 1) + (ISNULL(O.cantB,0) * 1) + ")
				.append("         (ISNULL(O.cantR,0) * 5) + (ISNULL(O.cantM,0) * 15)) AS DECIMAL(10,2)), ")
				.append("   'P2' ").append(" ) AS porcentajeValoracion, ")

				.append("     MAX(E.U_Objetivo) AS objetivoActividades, ")
				.append("     MAX(ISNULL(cab.U_cumplimientoobjetivo,'')) AS cumplimientoObjetivo ")

				.append(" FROM OTSH cab ").append(" LEFT JOIN TSH1 det ON cab.AbsEntry = det.AbsEntry ")
				.append(" AND (det.u_tipoausentismo IS NULL OR LTRIM(RTRIM(CAST(det.u_tipoausentismo AS NVARCHAR(MAX)))) NOT LIKE 'FERI%') ")
				.append(" INNER JOIN OHEM E ON cab.UserID = E.empID ").append(" LEFT JOIN ( ")
				.append("   SELECT AttendEmpl, ")
				.append("          SUM(CASE WHEN U_Valoracion IS NOT NULL THEN 1 ELSE 0 END) AS cantidadActividades, ")
				.append("          SUM(CASE WHEN U_Valoracion = 'MB' THEN 1 ELSE 0 END) AS cantMB, ")
				.append("          SUM(CASE WHEN U_Valoracion = 'B' THEN 1 ELSE 0 END) AS cantB, ")
				.append("          SUM(CASE WHEN U_Valoracion = 'R' THEN 1 ELSE 0 END) AS cantR, ")
				.append("          SUM(CASE WHEN U_Valoracion = 'M' THEN 1 ELSE 0 END) AS cantM, ")
				.append("          SUM(CASE WHEN U_Valoracion IS NULL THEN 1 ELSE 0 END) AS cantSV ")
				.append("   FROM OCLG ").append("   WHERE Recontact BETWEEN '{fechaDesde}' AND '{fechaHasta}' ")
				.append("     AND CAST(U_categoria AS NVARCHAR(MAX)) = 'Mantenimiento-reparacion' ")
				.append("     AND CAST(U_estado AS NVARCHAR(MAX)) IS NOT NULL ")
				.append("     AND CAST(U_estado AS NVARCHAR(MAX)) <> 'Pendiente' ");

		List<String> exclusionesList = Lists.newArrayList(exclusiones.split(","));
		for (String exclusion : exclusionesList) {
			sql.append(" and Details not like '%{exclusion}%' ".replace("{exclusion}", exclusion));
		}

		sql.append("   GROUP BY AttendEmpl ").append(" ) O ON O.AttendEmpl = E.empID ")
				.append(" WHERE cab.DateFrom = '{fechaDesde}' ").append("   AND E.empID IN ({idUsuarioSap}) ")
				.append(" GROUP BY cab.DateFrom, cab.UserID, E.firstName, E.lastName, ")
				.append("          E.U_Objetivo, O.cantidadActividades, O.cantMB, O.cantB, ")
				.append("          O.cantR, O.cantM, O.cantSV ").append(" ORDER BY E.firstName, E.lastName ");

		String sqlString = sql.toString().replace("{fechaDesde}", fechaDesde).replace("{fechaHasta}", fechaHasta)
				.replace("{idUsuarioSap}", idsUsuarioSap);

		List<RegistroInformeMensualEmpleadoIndividualDTO> list = new ArrayList<RegistroInformeMensualEmpleadoIndividualDTO>();

		try (Connection conn = sqlcon.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sqlString);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				RegistroInformeMensualEmpleadoIndividualDTO preview = new RegistroInformeMensualEmpleadoIndividualDTO();
				preview.setUsuarioSap(rs.getLong("usuarioSap"));
				preview.setNombre(rs.getString("nombre"));

				BigDecimal facturables = rs.getBigDecimal("facturables");
				preview.setFacturables(facturables != null ? facturables.toString() : "");
				preview.setFacturablesHora(rs.getString("facturablesHora"));

				BigDecimal ociosas = rs.getBigDecimal("ociosas");
				preview.setOciosas(ociosas != null ? ociosas.toString() : "");
				preview.setOciosasHora(rs.getString("ociosasHora"));

				BigDecimal fichado = rs.getBigDecimal("fichado");
				preview.setFichado(fichado != null ? fichado.toString() : "");
				preview.setFichadoHora(rs.getString("fichadoHora"));

				preview.setEfectividad(rs.getString("efectividad"));

				preview.setCantMB(rs.getInt("cantMB"));
				preview.setCantB(rs.getInt("cantB"));
				preview.setCantR(rs.getInt("cantR"));
				preview.setCantM(rs.getInt("cantM"));
				preview.setCantSV(rs.getInt("cantSV"));

				preview.setCantidadActividades(rs.getInt("cantidadActividades"));
				preview.setPorcentajeValoracion(rs.getString("porcentajeValoracion"));

				preview.setObjetivoActividades(rs.getInt("objetivoActividades"));
				preview.setCumplimientoObjetivo(rs.getString("cumplimientoObjetivo"));

				String usuarioSap = preview.getUsuarioSap().toString();

				Optional<UsuarioPremec> usuario = usuarios.stream().filter(x -> x.getUsuariosap().equals(usuarioSap))
						.findFirst();
				if (usuario.isPresent()) {
					preview.setLegajo(usuario.get().getLegajo());
				} else {
					// Error
					preview.setLegajo(-1L);
				}

				list.add(preview);

			}

			sqlcon.getConnection().close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;

	}

	private RegistroInformeMensualEmpleadoIndividualDTO getIndicadoresByUsuariosAgrupado(String mes, String anio,
			List<UsuarioPremec> users) {

		String idsUsuarioSap = users.stream().filter(x -> StringUtils.isNotBlank(x.getUsuariosap()))
				.map((UsuarioPremec::getUsuariosap)).map(String::trim).collect(Collectors.joining(","));

		String fechaDesde = anio + StringUtils.leftPad(mes, 2, "0") + "01";
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(DateUtils.toDate(fechaDesde, "yyyyMMdd"));
		fecha.add(Calendar.MONTH, 1);
		fecha.add(Calendar.DAY_OF_MONTH, -1);
		String fechaHasta = DateUtils.toString(fecha.getTime(), "yyyyMMdd");

		StringBuilder sql = new StringBuilder();

		sql.append("WITH detalle AS ( ");
		sql.append("    SELECT ");
		sql.append("        cab.UserID, ");
		sql.append("        SUM(det.BillableHr) AS facturables, ");

		sql.append("     ( ");

		sql.append("         SUM(CASE ");
		sql.append("             WHEN det.u_tipoausentismo IS NULL ");
		sql.append("                  OR CAST(det.u_tipoausentismo AS NVARCHAR(MAX)) = '' ");
		sql.append("             THEN det.EffectHr ELSE 0 ");
		sql.append("         END) ");

		sql.append("         - SUM(det.BillableHr) ");

		sql.append("     ) AS ociosas, ");

		sql.append("        SUM(CASE ");
		sql.append("            WHEN det.u_tipoausentismo IS NULL ");
		sql.append("              OR CAST(det.u_tipoausentismo AS NVARCHAR(MAX)) = '' ");
		sql.append("            THEN det.EffectHr ELSE 0 END) AS fichado, ");

		sql.append("        ISNULL(O.cantMB, 0) AS cantMB, ");
		sql.append("        ISNULL(O.cantB, 0) AS cantB, ");
		sql.append("        ISNULL(O.cantR, 0) AS cantR, ");
		sql.append("        ISNULL(O.cantM, 0) AS cantM, ");
		sql.append("        ISNULL(O.cantSV, 0) AS cantSV, ");
		sql.append("        ISNULL(O.cantidadActividades, 0) AS cantidadActividades ");
		sql.append("    FROM OTSH cab ");
		sql.append("    LEFT JOIN TSH1 det ON cab.AbsEntry = det.AbsEntry ");
		sql.append(
				"        AND (det.u_tipoausentismo IS NULL OR LTRIM(RTRIM(CAST(det.u_tipoausentismo AS NVARCHAR(MAX)))) NOT LIKE 'FERI%') ");
		sql.append("    INNER JOIN OHEM E ON cab.UserID = E.empID ");
		sql.append("    LEFT JOIN ( ");
		sql.append("        SELECT ");
		sql.append("            AttendEmpl, ");
		sql.append("            SUM(CASE WHEN U_Valoracion IS NOT NULL THEN 1 ELSE 0 END) AS cantidadActividades, ");
		sql.append("            SUM(CASE WHEN U_Valoracion = 'MB' THEN 1 ELSE 0 END) AS cantMB, ");
		sql.append("            SUM(CASE WHEN U_Valoracion = 'B'  THEN 1 ELSE 0 END) AS cantB, ");
		sql.append("            SUM(CASE WHEN U_Valoracion = 'R'  THEN 1 ELSE 0 END) AS cantR, ");
		sql.append("            SUM(CASE WHEN U_Valoracion = 'M'  THEN 1 ELSE 0 END) AS cantM, ");
		sql.append("            SUM(CASE WHEN U_Valoracion IS NULL THEN 1 ELSE 0 END) AS cantSV ");
		sql.append("        FROM OCLG ");
		sql.append("        WHERE Recontact BETWEEN '{fechaDesde}' AND '{fechaHasta}' ");
		sql.append("     AND CAST(U_categoria AS NVARCHAR(MAX)) = 'Mantenimiento-reparacion' ");
		sql.append("     AND CAST(U_estado AS NVARCHAR(MAX)) IS NOT NULL ");
		sql.append("     AND CAST(U_estado AS NVARCHAR(MAX)) <> 'Pendiente' ");

		List<String> exclusionesList = Lists.newArrayList(exclusiones.split(","));
		for (String exclusion : exclusionesList) {
			sql.append(" and Details not like '%{exclusion}%' ".replace("{exclusion}", exclusion));
		}

		sql.append("        GROUP BY AttendEmpl ");
		sql.append("    ) O ON O.AttendEmpl = E.empID ");
		sql.append("    WHERE cab.DateFrom = '{fechaDesde}' ");
		sql.append("      AND E.empID IN ({idUsuarioSap}) ");
		sql.append("    GROUP BY ");
		sql.append("        cab.UserID, ");
		sql.append("        O.cantidadActividades, ");
		sql.append("        O.cantMB, ");
		sql.append("        O.cantB, ");
		sql.append("        O.cantR, ");
		sql.append("        O.cantM, ");
		sql.append("        O.cantSV ");
		sql.append(") ");
		sql.append("SELECT ");
		sql.append("    SUM(facturables) AS facturables, ");
		sql.append("    dbo.fnFormatHoras(SUM(facturables)) AS facturablesHora, ");
		sql.append("    SUM(ociosas) AS ociosas, ");
		sql.append("    dbo.fnFormatHoras(SUM(ociosas)) AS ociosasHora, ");
		sql.append("    SUM(fichado) AS fichado, ");
		sql.append("    dbo.fnFormatHoras(SUM(fichado)) AS fichadoHora, ");
		sql.append("    CAST(CAST((SUM(facturables) * 100.0) / NULLIF(SUM(fichado), 0) ");
		sql.append("        AS DECIMAL(10,2)) AS VARCHAR(10)) + '%' AS efectividad, ");
		sql.append("    SUM(cantMB) AS cantMB, ");
		sql.append("    SUM(cantB) AS cantB, ");
		sql.append("    SUM(cantR) AS cantR, ");
		sql.append("    SUM(cantM) AS cantM, ");
		sql.append("    SUM(cantSV) AS cantSV, ");
		sql.append("    SUM(cantidadActividades) AS cantidadActividades, ");
		sql.append("    FORMAT( ");
		sql.append("        CAST(NULLIF(SUM(cantMB) + SUM(cantB), 0) AS DECIMAL(10,2)) / ");
		sql.append("        CAST( ");
		sql.append("            (SUM(cantMB) * 1) + ");
		sql.append("            (SUM(cantB)  * 1) + ");
		sql.append("            (SUM(cantR)  * 5) + ");
		sql.append("            (SUM(cantM)  * 15) ");
		sql.append("            AS DECIMAL(10,2) ");
		sql.append("        ), ");
		sql.append("        'P2' ");
		sql.append("    ) AS porcentajeValoracion ");
		sql.append("FROM detalle");

		String sqlString = sql.toString().replace("{fechaDesde}", fechaDesde).replace("{fechaHasta}", fechaHasta)
				.replace("{idUsuarioSap}", idsUsuarioSap);

		RegistroInformeMensualEmpleadoIndividualDTO preview = null;

		try (Connection conn = sqlcon.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sqlString);
				ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				preview = new RegistroInformeMensualEmpleadoIndividualDTO();

				BigDecimal facturables = rs.getBigDecimal("facturables");
				preview.setFacturables(facturables != null ? facturables.toString() : "");
				preview.setFacturablesHora(rs.getString("facturablesHora"));

				BigDecimal ociosas = rs.getBigDecimal("ociosas");
				preview.setOciosas(ociosas != null ? ociosas.toString() : "");
				preview.setOciosasHora(rs.getString("ociosasHora"));

				BigDecimal fichado = rs.getBigDecimal("fichado");
				preview.setFichado(fichado != null ? fichado.toString() : "");
				preview.setFichadoHora(rs.getString("fichadoHora"));

				preview.setEfectividad(rs.getString("efectividad"));

				preview.setCantMB(rs.getInt("cantMB"));
				preview.setCantB(rs.getInt("cantB"));
				preview.setCantR(rs.getInt("cantR"));
				preview.setCantM(rs.getInt("cantM"));
				preview.setCantSV(rs.getInt("cantSV"));

				preview.setCantidadActividades(rs.getInt("cantidadActividades"));
				preview.setPorcentajeValoracion(rs.getString("porcentajeValoracion"));

			}

			sqlcon.getConnection().close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return preview;

	}

	private Map<Long, IndicadorAusentismoDTO> obtenerAusentismo(String mes, String anio, List<UsuarioPremec> usuarios) {
		Map<Long, IndicadorAusentismoDTO> resultado = new HashMap<>();
		List<Long> idsSap = null;

		if (usuarios != null) {
			Set<Long> idsUnicos = new HashSet<>();
			for (UsuarioPremec usuario : usuarios) {
				if (StringUtils.isNotBlank(usuario.getUsuariosap())) {
					idsUnicos.add(Long.valueOf(usuario.getUsuariosap().trim()));
				}
			}
			idsSap = new ArrayList<>(idsUnicos);
			if (idsSap.isEmpty()) {
				return resultado;
			}
		}

		LocalDate desde = LocalDate.of(Integer.parseInt(anio), Integer.parseInt(mes), 1);
		LocalDate hastaExclusivo = desde.plusMonths(1);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT cab.UserID AS usuarioSap, ").append("ISNULL(SUM(det.EffectHr), 0) AS horasTotales, ").append(
				"ISNULL(SUM(CASE WHEN NULLIF(LTRIM(RTRIM(CAST(det.U_tipoausentismo AS NVARCHAR(MAX)))), '') IS NOT NULL ")
				.append("THEN ISNULL(det.EffectHr, 0) ELSE 0 END), 0) AS horasAusencia, ")
				.append("ISNULL(SUM(CASE WHEN tipo.Chargeable = 'Y' THEN ISNULL(det.EffectHr, 0) ELSE 0 END), 0) AS horasAusenciaJustificadas, ")
				.append("ISNULL(SUM(CASE WHEN tipo.Chargeable = 'N' THEN ISNULL(det.EffectHr, 0) ELSE 0 END), 0) AS horasAusenciaInjustificadas ")
				.append("FROM OTSH cab INNER JOIN TSH1 det ON det.AbsEntry = cab.AbsEntry ")
				.append("CROSS APPLY (SELECT NULLIF(LTRIM(RTRIM(CAST(det.U_tipoausentismo AS NVARCHAR(MAX)))), '') AS TipoAusentismo) aus ")
				.append("OUTER APPLY (SELECT TOP (1) cfg.Chargeable FROM PMC5 cfg ")
				.append("WHERE aus.TipoAusentismo IS NOT NULL AND aus.TipoAusentismo NOT LIKE 'FERI%' ")
				.append("AND NULLIF(LTRIM(RTRIM(cfg.ActType)), '') IS NOT NULL ")
				.append("AND LEFT(aus.TipoAusentismo, LEN(LTRIM(RTRIM(cfg.ActType)))) COLLATE SQL_Latin1_General_CP850_CI_AS = LTRIM(RTRIM(cfg.ActType)) COLLATE SQL_Latin1_General_CP850_CI_AS ")
				.append("ORDER BY LEN(LTRIM(RTRIM(cfg.ActType))) DESC) tipo ")
				.append("WHERE det.[Date] >= ? AND det.[Date] < ? AND (aus.TipoAusentismo IS NULL OR aus.TipoAusentismo NOT LIKE 'FERI%') ");

		if (idsSap != null) {
			sql.append("AND cab.UserID IN (").append(idsSap.stream().map(id -> "?").collect(Collectors.joining(",")))
					.append(") ");
		}
		sql.append("GROUP BY cab.UserID");

		try (Connection conn = sqlcon.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
			int parametro = 1;
			stmt.setDate(parametro++, java.sql.Date.valueOf(desde));
			stmt.setDate(parametro++, java.sql.Date.valueOf(hastaExclusivo));
			if (idsSap != null) {
				for (Long idSap : idsSap) {
					stmt.setLong(parametro++, idSap);
				}
			}

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					BigDecimal horasTotales = rs.getBigDecimal("horasTotales");
					BigDecimal horasAusencia = rs.getBigDecimal("horasAusencia");
					BigDecimal horasJustificadas = rs.getBigDecimal("horasAusenciaJustificadas");
					BigDecimal horasInjustificadas = rs.getBigDecimal("horasAusenciaInjustificadas");
					resultado.put(rs.getLong("usuarioSap"),
							crearAusentismo(horasTotales, horasAusencia, horasJustificadas, horasInjustificadas));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultado;
	}

	private void completarAusentismo(List<RegistroInformeMensualEmpleadoIndividualDTO> indicadores,
			Map<Long, IndicadorAusentismoDTO> ausentismoPorUsuario) {
		for (RegistroInformeMensualEmpleadoIndividualDTO indicador : indicadores) {
			IndicadorAusentismoDTO ausentismo = ausentismoPorUsuario.get(indicador.getUsuarioSap());
			indicador.setAusentismo(ausentismo != null ? ausentismo
					: crearAusentismo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
		}
	}

	private IndicadorAusentismoDTO agregarAusentismo(Iterable<IndicadorAusentismoDTO> ausentismos) {
		BigDecimal horasTotales = BigDecimal.ZERO;
		BigDecimal horasAusencia = BigDecimal.ZERO;
		BigDecimal horasJustificadas = BigDecimal.ZERO;
		BigDecimal horasInjustificadas = BigDecimal.ZERO;
		for (IndicadorAusentismoDTO ausentismo : ausentismos) {
			horasTotales = horasTotales.add(ausentismo.getHorasTotalesValor());
			horasAusencia = horasAusencia.add(ausentismo.getHorasAusenciaValor());
			horasJustificadas = horasJustificadas.add(ausentismo.getHorasAusenciaJustificadasValor());
			horasInjustificadas = horasInjustificadas.add(ausentismo.getHorasAusenciaInjustificadasValor());
		}
		return crearAusentismo(horasTotales, horasAusencia, horasJustificadas, horasInjustificadas);
	}

	private IndicadorAusentismoDTO crearAusentismo(BigDecimal horasTotales, BigDecimal horasAusencia,
			BigDecimal horasJustificadas, BigDecimal horasInjustificadas) {
		IndicadorAusentismoDTO ausentismo = new IndicadorAusentismoDTO();
		horasTotales = horasTotales != null ? horasTotales : BigDecimal.ZERO;
		horasAusencia = horasAusencia != null ? horasAusencia : BigDecimal.ZERO;
		horasJustificadas = horasJustificadas != null ? horasJustificadas : BigDecimal.ZERO;
		horasInjustificadas = horasInjustificadas != null ? horasInjustificadas : BigDecimal.ZERO;
		ausentismo.setHorasTotales(horasTotales.setScale(6, RoundingMode.HALF_UP));
		ausentismo.setHorasAusencia(horasAusencia.setScale(6, RoundingMode.HALF_UP));
		ausentismo.setHorasAusenciaJustificadas(horasJustificadas.setScale(6, RoundingMode.HALF_UP));
		ausentismo.setHorasAusenciaInjustificadas(horasInjustificadas.setScale(6, RoundingMode.HALF_UP));
		ausentismo.setPorcentajeAusenciaJustificada(porcentajeAusentismo(horasJustificadas, horasAusencia));
		ausentismo.setPorcentajeAusenciaInjustificada(porcentajeAusentismo(horasInjustificadas, horasAusencia));
		BigDecimal porcentaje = horasTotales.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
				: horasAusencia.multiply(BigDecimal.valueOf(100)).divide(horasTotales, 2, RoundingMode.HALF_UP);
		ausentismo.setPorcentajeAusentismo(porcentaje);
		return ausentismo;
	}

}
