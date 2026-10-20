package ar.com.avaco.premec.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
	public List<ActivityValoracionSapDTO> getActividadesValoracionPorEmpleados(
			List<Long> idsEmpleados, Integer mes, Integer anio) {

		List<UsuarioPremec> byIds = usuarioPremecService.getByIds(idsEmpleados);

		List<String> idsUsuariosSap = byIds.stream()
				.filter(x -> StringUtils.isNotBlank(x.getUsuariosap()))
				.map(UsuarioPremec::getUsuariosap)
				.map(String::trim)
				.collect(Collectors.toList());

		LocalDate fechaDesde = LocalDate.of(anio, mes, 1);
		LocalDate fechaHasta = fechaDesde.plusMonths(1);

		String fechaDesdeSql = fechaDesde.toString().replace("-", "");
		String fechaHastaSql = fechaHasta.toString().replace("-", "");

		StringBuilder sql = new StringBuilder();

		sql.append(" SELECT ")
				.append(" O.Details AS details, ")
				.append(" O.ClgCode AS activityCode, ")
				.append(" O.recontact AS startDate, ")
				.append(" O.U_detallevaloracion AS detalleValoracion, ")
				.append(" BP.CardName AS cardName, ")
				.append(" E.firstName AS firstName, ")
				.append(" E.lastName AS lastName ")
				.append(" FROM OCLG O ")
				.append(" LEFT JOIN OCRD BP ON BP.CardCode = O.CardCode ")
				.append(" LEFT JOIN OHEM E ON E.empID = O.AttendEmpl ")
				.append(" WHERE O.U_Valoracion = 'M' ")
				.append(" AND CAST(O.U_categoria AS NVARCHAR(MAX)) = 'Mantenimiento-reparacion' ")
				.append(" AND CAST(O.U_estado AS NVARCHAR(MAX)) IS NOT NULL ")
				.append(" AND CAST(O.U_estado AS NVARCHAR(MAX)) <> 'Pendiente' ")
				.append(" AND O.recontact >= '{fechaDesde}' ")
				.append(" AND O.recontact < '{fechaHasta}' ");

		if (idsUsuariosSap != null && !idsUsuariosSap.isEmpty()) {
			sql.append(" AND O.AttendEmpl IN ({idUsuarioSap}) ");
		}

		sql.append(" ORDER BY O.CntctDate, O.ClgCode ");

		String sqlString = sql.toString()
				.replace("{fechaDesde}", fechaDesdeSql)
				.replace("{fechaHasta}", fechaHastaSql)
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

	        .append("     ( ")
	        .append("         SUM(T.fichado) - SUM(T.facturables) ")
	        .append("     ) AS ociosas, ")

	        .append("     dbo.fnFormatHoras( ")
	        .append("         SUM(T.fichado) - SUM(T.facturables) ")
	        .append("     ) AS ociosasHora, ")

	        .append("     SUM(T.fichado) AS fichado, ")

	        .append("     dbo.fnFormatHoras(SUM(T.fichado)) AS fichadoHora, ")

	        .append("     CAST( ")
	        .append("         CAST( ")
	        .append("             (SUM(T.facturables) * 100.0) / ")
	        .append("             NULLIF(SUM(T.fichado), 0) ")
	        .append("             AS DECIMAL(10,2) ")
	        .append("         ) AS VARCHAR(10) ")
	        .append("     ) + '%' AS efectividad, ")

	        // ============================================================
	        // ACTIVIDADES VALORADAS
	        // ============================================================
	        .append("     SUM(ISNULL(O.cantMB, 0)) AS cantMB, ")
	        .append("     SUM(ISNULL(O.cantB, 0)) AS cantB, ")
	        .append("     SUM(ISNULL(O.cantR, 0)) AS cantR, ")
	        .append("     SUM(ISNULL(O.cantM, 0)) AS cantM, ")
	        .append("     SUM(ISNULL(O.cantSV, 0)) AS cantSV, ")
	        .append("     SUM(ISNULL(O.cantidadActividades, 0)) AS cantidadActividades, ")

	        // ============================================================
	        // PORCENTAJE DE VALORACION
	        // MB = 1
	        // B  = 1
	        // R  = 5
	        // M  = 15
	        // ============================================================
	        .append("     FORMAT( ")
	        .append("         CAST( ")
	        .append("             NULLIF( ")
	        .append("                 SUM(ISNULL(O.cantMB, 0)) + ")
	        .append("                 SUM(ISNULL(O.cantB, 0)), ")
	        .append("                 0 ")
	        .append("             ) AS DECIMAL(10,2) ")
	        .append("         ) / ")
	        .append("         NULLIF( ")
	        .append("             CAST( ")
	        .append("                 (SUM(ISNULL(O.cantMB, 0)) * 1) + ")
	        .append("                 (SUM(ISNULL(O.cantB, 0)) * 1) + ")
	        .append("                 (SUM(ISNULL(O.cantR, 0)) * 5) + ")
	        .append("                 (SUM(ISNULL(O.cantM, 0)) * 15) ")
	        .append("             AS DECIMAL(10,2)), ")
	        .append("             0 ")
	        .append("         ), ")
	        .append("         'P2' ")
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
	        .append("     SELECT ")
	        .append("         cab.UserID, ")

	        .append("         ISNULL(SUM(det.BillableHr), 0) AS facturables, ")

	        .append("         ISNULL(SUM(CASE ")
	        .append("             WHEN det.u_tipoausentismo IS NULL ")
	        .append("                  OR CAST(det.u_tipoausentismo AS NVARCHAR(MAX)) = '' ")
	        .append("             THEN det.EffectHr ")
	        .append("             ELSE 0 ")
	        .append("         END), 0) AS fichado ")

	        .append("     FROM OTSH cab ")

	        .append("     LEFT JOIN TSH1 det ")
	        .append("         ON cab.AbsEntry = det.AbsEntry ")

	        .append("     WHERE cab.DateFrom = '{fechaDesde}' ")

	        .append("     GROUP BY cab.UserID ")

	        .append(" ) T ")

	        .append(" INNER JOIN OHEM E ")
	        .append("     ON T.UserID = E.empID ")

	        // ============================================================
	        // ACTIVIDADES OCLG
	        // Una sola fila por empleado.
	        // ============================================================
	        .append(" LEFT JOIN ( ")

	        .append("     SELECT ")
	        .append("         AttendEmpl, ")
	        .append("         SUM(CASE WHEN U_Valoracion IS NOT NULL THEN 1 ELSE 0 END) AS cantidadActividades, ")

	        .append("         SUM(CASE ")
	        .append("             WHEN U_Valoracion = 'MB' THEN 1 ")
	        .append("             ELSE 0 ")
	        .append("         END) AS cantMB, ")

	        .append("         SUM(CASE ")
	        .append("             WHEN U_Valoracion = 'B' THEN 1 ")
	        .append("             ELSE 0 ")
	        .append("         END) AS cantB, ")

	        .append("         SUM(CASE ")
	        .append("             WHEN U_Valoracion = 'R' THEN 1 ")
	        .append("             ELSE 0 ")
	        .append("         END) AS cantR, ")

	        .append("         SUM(CASE ")
	        .append("             WHEN U_Valoracion = 'M' THEN 1 ")
	        .append("             ELSE 0 ")
	        .append("         END) AS cantM, ")

	        .append("         SUM(CASE ")
	        .append("             WHEN U_Valoracion IS NULL THEN 1 ")
	        .append("             ELSE 0 ")
	        .append("         END) AS cantSV ")

	        .append("     FROM OCLG ")

	        .append("     WHERE Recontact BETWEEN '{fechaDesde}' AND '{fechaHasta}' ")
	        .append("     AND CAST(U_categoria AS NVARCHAR(MAX)) = 'Mantenimiento-reparacion' ")
	        .append("     AND CAST(U_estado AS NVARCHAR(MAX)) IS NOT NULL ")
	        .append("     AND CAST(U_estado AS NVARCHAR(MAX)) <> 'Pendiente' ");

	    List<String> exclusionesList = Lists.newArrayList(exclusiones.split(","));

	    for (String exclusion : exclusionesList) {
	        sql.append(" AND Details NOT LIKE '%{exclusion}%' "
	                .replace("{exclusion}", exclusion));
	    }

	    sql.append("     GROUP BY AttendEmpl ")

	        .append(" ) O ")

	        .append("     ON O.AttendEmpl = E.empID ");

	    String sqlString = sql.toString()
	        .replace("{fechaDesde}", fechaDesde)
	        .replace("{fechaHasta}", fechaHasta);

	    RegistroInformeMensualGeneralDTO preview = null;

	    try (Connection conn = sqlcon.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sqlString);
	         ResultSet rs = stmt.executeQuery()) {

	        if (rs.next()) {

	            preview = new RegistroInformeMensualGeneralDTO();

	            BigDecimal facturables = rs.getBigDecimal("facturables");
	            preview.setFacturables(
	                facturables != null ? facturables.toString() : ""
	            );

	            preview.setFacturablesHora(
	                rs.getString("facturablesHora")
	            );

	            BigDecimal ociosas = rs.getBigDecimal("ociosas");
	            preview.setOciosas(
	                ociosas != null ? ociosas.toString() : ""
	            );

	            preview.setOciosasHora(
	                rs.getString("ociosasHora")
	            );

	            BigDecimal fichado = rs.getBigDecimal("fichado");
	            preview.setFichado(
	                fichado != null ? fichado.toString() : ""
	            );

	            preview.setFichadoHora(
	                rs.getString("fichadoHora")
	            );

	            preview.setEfectividad(
	                rs.getString("efectividad")
	            );

	            preview.setCantMB(
	                rs.getInt("cantMB")
	            );

	            preview.setCantB(
	                rs.getInt("cantB")
	            );

	            preview.setCantR(
	                rs.getInt("cantR")
	            );

	            preview.setCantM(
	                rs.getInt("cantM")
	            );

	            preview.setCantSV(
	                rs.getInt("cantSV")
	            );

	            preview.setCantidadActividades(
	                rs.getInt("cantidadActividades")
	            );

	            preview.setPorcentajeValoracion(
	                rs.getString("porcentajeValoracion")
	            );
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
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
	        .append("     ISNULL(SUM(det.BillableHr), 0) AS facturables, ")

	        .append("     dbo.fnFormatHoras(ISNULL(SUM(det.BillableHr), 0)) AS facturablesHora, ")

	        .append("     ( ")
	        .append("         ISNULL(SUM(CASE ")
	        .append("             WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
	        .append("             THEN det.EffectHr ELSE 0 ")
	        .append("         END), 0) ")
	        .append("         - ISNULL(SUM(det.BillableHr), 0) ")
	        .append("     ) AS ociosas, ")

	        .append("     dbo.fnFormatHoras( ")
	        .append("         ( ")
	        .append("             ISNULL(SUM(CASE ")
	        .append("                 WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
	        .append("                 THEN det.EffectHr ELSE 0 ")
	        .append("             END), 0) ")
	        .append("             - ISNULL(SUM(det.BillableHr), 0) ")
	        .append("         ) ")
	        .append("     ) AS ociosasHora, ")

	        .append("     ISNULL(SUM(CASE ")
	        .append("         WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
	        .append("         THEN det.EffectHr ELSE 0 ")
	        .append("     END), 0) AS fichado, ")

	        .append("     dbo.fnFormatHoras(ISNULL(SUM(CASE ")
	        .append("         WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
	        .append("         THEN det.EffectHr ELSE 0 ")
	        .append("     END), 0)) AS fichadoHora, ")

	        // EFECTIVIDAD
	        .append("     CAST( ")
	        .append("         CAST( ")
	        .append("             (ISNULL(SUM(det.BillableHr), 0) * 100.0) / ")
	        .append("             NULLIF( ")
	        .append("                 ISNULL(SUM(CASE ")
	        .append("                     WHEN det.u_tipoausentismo IS NULL OR det.u_tipoausentismo = '' ")
	        .append("                     THEN det.EffectHr ELSE 0 ")
	        .append("                 END), 0), ")
	        .append("                 0 ")
	        .append("             ) AS DECIMAL(10,2) ")
	        .append("         ) AS VARCHAR(10) ")
	        .append("     ) + '%' AS efectividad, ")

	        // VALORACIONES
	        .append("     MAX(ISNULL(O.cantMB, 0)) AS cantMB, ")
	        .append("     MAX(ISNULL(O.cantB, 0)) AS cantB, ")
	        .append("     MAX(ISNULL(O.cantR, 0)) AS cantR, ")
	        .append("     MAX(ISNULL(O.cantM, 0)) AS cantM, ")
	        .append("     MAX(ISNULL(O.cantSV, 0)) AS cantSV, ")
	        .append("     MAX(ISNULL(O.cantidadActividades, 0)) AS cantidadActividades, ")

	        // PORCENTAJE VALORACION
	        // Se calcula sobre los valores agregados de OCLG,
	        // sin multiplicarlos por la cantidad de registros de TSH1.
	        .append("     FORMAT( ")
	        .append("         CAST( ")
	        .append("             NULLIF( ")
	        .append("                 MAX(ISNULL(O.cantMB, 0)) + MAX(ISNULL(O.cantB, 0)), ")
	        .append("                 0 ")
	        .append("             ) AS DECIMAL(10,2) ")
	        .append("         ) / ")
	        .append("         NULLIF( ")
	        .append("             CAST( ")
	        .append("                 (MAX(ISNULL(O.cantMB, 0)) * 1) + ")
	        .append("                 (MAX(ISNULL(O.cantB, 0)) * 1) + ")
	        .append("                 (MAX(ISNULL(O.cantR, 0)) * 5) + ")
	        .append("                 (MAX(ISNULL(O.cantM, 0)) * 15) ")
	        .append("             AS DECIMAL(10,2)), ")
	        .append("             0 ")
	        .append("         ), ")
	        .append("         'P2' ")
	        .append("     ) AS porcentajeValoracion, ")

	        // DATOS ADICIONALES
	        .append("     MAX(E.U_Objetivo) AS objetivoActividades, ")
	        .append("     MAX(ISNULL(cab.U_cumplimientoobjetivo, '')) AS cumplimientoObjetivo, ")
	        .append("     MAX(ISNULL(cab.U_salario, 0)) AS salario, ")
	        .append("     MAX(ISNULL(E.salaryunit, '')) AS unidadSalario, ")
	        .append("     MAX(cab.u_viaticos) AS viaticos, ")
	        .append("     MAX(cab.U_adelanto) AS adelanto, ")
	        .append("     MAX(cab.U_prestamo) AS prestamo, ")
	        .append("     MAX(cab.u_aumento) AS aumento, ")
	        .append("     MAX(cab.u_premio) AS premio, ")
	        .append("     MAX(ISNULL(cab.U_sueldoneto, 0)) AS sueldoNeto ")

	        // EMPLEADO COMO TABLA PRINCIPAL
	        .append(" FROM OHEM E ")

	        // CABECERA DEL TIMESHEET DEL MES, SI EXISTE
	        .append(" LEFT JOIN OTSH cab ")
	        .append("     ON cab.UserID = E.empID ")
	        .append("     AND cab.DateFrom = '{fechaDesde}' ")

	        // DETALLE DEL TIMESHEET
	        .append(" LEFT JOIN TSH1 det ")
	        .append("     ON cab.AbsEntry = det.AbsEntry ")

	        // ACTIVIDADES DEL MES
	        .append(" LEFT JOIN ( ")
	        .append("     SELECT ")
	        .append("         AttendEmpl, ")
	        .append("         SUM(CASE WHEN U_Valoracion IS NOT NULL THEN 1 ELSE 0 END) AS cantidadActividades, ")
	        .append("         SUM(CASE WHEN U_Valoracion = 'MB' THEN 1 ELSE 0 END) AS cantMB, ")
	        .append("         SUM(CASE WHEN U_Valoracion = 'B' THEN 1 ELSE 0 END) AS cantB, ")
	        .append("         SUM(CASE WHEN U_Valoracion = 'R' THEN 1 ELSE 0 END) AS cantR, ")
	        .append("         SUM(CASE WHEN U_Valoracion = 'M' THEN 1 ELSE 0 END) AS cantM, ")
	        .append("         SUM(CASE WHEN U_Valoracion IS NULL THEN 1 ELSE 0 END) AS cantSV ")
	        .append("     FROM OCLG ")
	        .append("     WHERE Recontact BETWEEN '{fechaDesde}' AND '{fechaHasta}' ")
	        .append("     AND CAST(U_categoria AS NVARCHAR(MAX)) = 'Mantenimiento-reparacion' ")
	        .append("     AND CAST(U_estado AS NVARCHAR(MAX)) IS NOT NULL ")
	        .append("     AND CAST(U_estado AS NVARCHAR(MAX)) <> 'Pendiente' ");

	    List<String> exclusionesList = Lists.newArrayList(exclusiones.split(","));

	    for (String exclusion : exclusionesList) {
	        sql.append(" AND Details NOT LIKE '%{exclusion}%' "
	                .replace("{exclusion}", exclusion));
	    }

	    sql.append("     GROUP BY AttendEmpl ")
	        .append(" ) O ")
	        .append("     ON O.AttendEmpl = E.empID ")

	        // FILTRO SOLAMENTE SOBRE EL EMPLEADO
	        .append(" WHERE E.empID = {idUsuarioSap} ");

	    String sqlString = sql.toString()
	        .replace("{fechaDesde}", fechaDesde)
	        .replace("{fechaHasta}", fechaHasta)
	        .replace("{idUsuarioSap}", idUsuarioSap);

	    RegistroInformeMensualEmpleadoDTO preview = null;

	    try (Connection conn = sqlcon.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sqlString);
	         ResultSet rs = stmt.executeQuery()) {

	        if (rs.next()) {

	            preview = new RegistroInformeMensualEmpleadoDTO();

	            preview.setUsuarioSap(rs.getLong("usuarioSap"));
	            preview.setNombre(rs.getString("nombre"));

	            BigDecimal facturables = rs.getBigDecimal("facturables");
	            preview.setFacturables(
	                facturables != null ? facturables.toString() : "0"
	            );

	            preview.setFacturablesHora(rs.getString("facturablesHora"));

	            BigDecimal ociosas = rs.getBigDecimal("ociosas");
	            preview.setOciosas(
	                ociosas != null ? ociosas.toString() : "0"
	            );

	            preview.setOciosasHora(rs.getString("ociosasHora"));

	            BigDecimal fichado = rs.getBigDecimal("fichado");
	            preview.setFichado(
	                fichado != null ? fichado.toString() : "0"
	            );

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

	            preview.setCantidadActividades(
	                rs.getInt("cantidadActividades")
	            );

	            preview.setPorcentajeValoracion(
	                rs.getString("porcentajeValoracion")
	            );

	            preview.setObjetivoActividades(
	                rs.getInt("objetivoActividades")
	            );

	            preview.setCumplimientoObjetivo(
	                rs.getString("cumplimientoObjetivo")
	            );

	            BigDecimal salariobd = rs.getBigDecimal("salario");

	            preview.setSalario(
	                salariobd != null ? salariobd.toString() : ""
	            );

	            preview.setUnidadSalario(
	                rs.getString("unidadSalario")
	            );

	            BigDecimal sueldoNeto = rs.getBigDecimal("sueldoNeto");

	            preview.setSueldoNeto(
	                sueldoNeto != null
	                    ? sueldoNeto.setScale(2, RoundingMode.HALF_UP).toString()
	                    : ""
	            );

	            String usuarioSap = preview.getUsuarioSap().toString();

	            Optional<UsuarioPremec> usuario = usuarios.stream()
	                .filter(x -> x.getUsuariosap().equals(usuarioSap))
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

	    return preview;
	}
	
	@Override
	public List<RegistroInformeMensualEmpleadoIndividualDTO> getIndicadoresGrupoEmpleado(String mes, String anio,
			Long idGrupoEmpleado) {

		GrupoEmpleado grupoEmpleado = grupoEmpleadoService.get(idGrupoEmpleado);

		return getIndicadoresByUsuarios(mes, anio, new ArrayList<UsuarioPremec>(grupoEmpleado.getUsuarios()));

	}

	@Override
	public List<RegistroInformeMensualEmpleadoIndividualDTO> getIndicadoresEmpleados(String mes, String anio,
			String idsEmpleados) {

		List<Long> listaIds = Arrays.stream(idsEmpleados.split(",")).map(String::trim).filter(s -> !s.isEmpty())
				.map(Long::valueOf).collect(Collectors.toList());

		List<UsuarioPremec> byIds = usuarioPremecService.getByIds(listaIds);

		return getIndicadoresByUsuarios(mes, anio, byIds);

	}

	@Override
	public RegistroInformeMensualEmpleadoIndividualDTO getIndicadoresGrupoEmpleadoAgrupado(String mes, String anio,
			Long idGrupoEmpleado) {

		GrupoEmpleado grupoEmpleado = grupoEmpleadoService.get(idGrupoEmpleado);

		return getIndicadoresByUsuariosAgrupado(mes, anio, new ArrayList<UsuarioPremec>(grupoEmpleado.getUsuarios()));

	}

	@Override
	public RegistroInformeMensualEmpleadoIndividualDTO getIndicadoresEmpleadosAgrupado(String mes, String anio,
			String idsEmpleados) {

		List<Long> listaIds = Arrays.stream(idsEmpleados.split(",")).map(String::trim).filter(s -> !s.isEmpty())
				.map(Long::valueOf).collect(Collectors.toList());

		List<UsuarioPremec> byIds = usuarioPremecService.getByIds(listaIds);

		return getIndicadoresByUsuariosAgrupado(mes, anio, byIds);

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
				.append(" INNER JOIN OHEM E ON cab.UserID = E.empID ")
				.append(" LEFT JOIN ( ")
				.append("   SELECT AttendEmpl, ")
				.append("          SUM(CASE WHEN U_Valoracion IS NOT NULL THEN 1 ELSE 0 END) AS cantidadActividades, ")
				.append("          SUM(CASE WHEN U_Valoracion = 'MB' THEN 1 ELSE 0 END) AS cantMB, ")
				.append("          SUM(CASE WHEN U_Valoracion = 'B' THEN 1 ELSE 0 END) AS cantB, ")
				.append("          SUM(CASE WHEN U_Valoracion = 'R' THEN 1 ELSE 0 END) AS cantR, ")
				.append("          SUM(CASE WHEN U_Valoracion = 'M' THEN 1 ELSE 0 END) AS cantM, ")
				.append("          SUM(CASE WHEN U_Valoracion IS NULL THEN 1 ELSE 0 END) AS cantSV ")
				.append("   FROM OCLG ")
				.append("   WHERE Recontact BETWEEN '{fechaDesde}' AND '{fechaHasta}' ")
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

}
