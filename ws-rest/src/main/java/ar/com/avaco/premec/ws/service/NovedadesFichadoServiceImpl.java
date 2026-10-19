package ar.com.avaco.premec.ws.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.core.exception.ErrorValidationException;
import ar.com.avaco.fwk.core.utils.DateUtils;
import ar.com.avaco.premec.domain.UsuarioPremec;
import ar.com.avaco.premec.dto.EmpleadoFichados;
import ar.com.avaco.premec.dto.EmpleadoSap;
import ar.com.avaco.premec.dto.RegistroFichadoDTO;
import ar.com.avaco.premec.repository.UsuarioPremecRepository;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.utils.FichadoUtils;
import ar.com.avaco.premec.ws.dto.actividad.HorasPorEmpleadoDTO;
import ar.com.avaco.premec.ws.dto.employee.EmployeesInfoReponseSapDTO;
import ar.com.avaco.premec.ws.dto.timesheet.ProjectManagementTimeSheetGetDTO;
import ar.com.avaco.premec.ws.dto.timesheet.ProjectManagementTimeSheetLineGetDTO;

@Service("novedadesFichadoService")
public class NovedadesFichadoServiceImpl extends AbstractSapService implements NovedadesFichadoService {

	@Autowired
	private UsuarioPremecRepository usuarioPremecRepository;

	@Autowired
	private TimeSheetService timeSheetService;

	@Autowired
	private EmployeeService employeeService;

	@Autowired
	private ActivityService activityService;

	@Value(value = "${exclusiones.actividades.calculo.horas.netas}")
	private String exclusionesActividadesCalculoHorasNetas;

	@Value(value = "${licencia.feriado}")
	private String licenciasFeriado;

	@Value(value = "${licencia.nojustificada}")
	private String licenciasNoJustificada;

	@Autowired
	private SQLServerConnection sqlConnection;

	@Override
	public List<EmpleadoFichados> parsearExcelNovedadesFichado(byte[] archivoBytes) throws IOException {

		// Obtengo todos los empleados
		List<EmployeesInfoReponseSapDTO> allEmployees = employeeService.getAllEmployees();

		List<UsuarioPremec> usuariosPremec = usuarioPremecRepository.findAll();

		// Obtengo los codigos de ausentismo
		Set<String> codigosAusentismo = getCodigosAusentismo().keySet();

		// Armo mapa de errores con linea como clave y mensaje como valor
		Map<String, String> errores = new HashMap<String, String>();

		// Inicializo el listado
		List<EmpleadoFichados> empleadoFichadoList = new ArrayList<>();

		// Importo el archivo y lo convierto en libro de excel
		try (InputStream inputStream = new ByteArrayInputStream(archivoBytes)) {

			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheetAt(0);

			// Armo mapa de EmpleadoSAP como clave y su listado de registros por dia
			// asociado
			Map<EmpleadoSap, List<RegistroFichadoDTO>> empleados = new HashMap<EmpleadoSap, List<RegistroFichadoDTO>>();

			// Empiezo a recorrer las filas del excel
			for (int i = 0; i <= sheet.getLastRowNum();) {

				// Busco la primer aparicion de legajo
				Row fila = sheet.getRow(i);

				if (fila == null || !FichadoUtils.getCellValue(fila.getCell(0)).equalsIgnoreCase("legajo")) {
					i++;
					continue;
				}

				String is = String.valueOf(fila.getRowNum() + 1);

				// Extraer legajo y nombre
				String legajoNombre = FichadoUtils.getCellValue(fila.getCell(1));
				String[] partes = legajoNombre.split(" ");
				String legajo = partes[0].trim();
				
				String turno = FichadoUtils.getCellValue(fila.getCell(4));
				String sector = turno.split(":")[0].trim();
				
				boolean validarHorasExtras = sector.equalsIgnoreCase("TALLER");
				
				// Busco el usuario premec

				UsuarioPremec usuario = usuariosPremec.stream().filter(x -> x != null)
						.filter(x -> Long.valueOf(legajo).equals(x.getLegajo())).findFirst().orElse(null);

				EmpleadoSap empleado = new EmpleadoSap();

				EmployeesInfoReponseSapDTO employee = null;
				
				if (usuario == null) {
					errores.put(is,
							"El empleado " + legajoNombre + " no se encuentra dado de alta en el sistema con el "
									+ legajo + ". Agregarlo con el legajo y usuario sap correspondiente.");
				} else if (StringUtils.isBlank(usuario.getUsuariosap())) {
					errores.put(is, "El empleado " + legajoNombre + " con legajo " + usuario.getLegajo()
							+ " no tiene asignado usuario sap. Asignarlo para poder continuar.");
				} else {
					empleado.setApellido(usuario.getApellido());
					empleado.setNombre(usuario.getNombre());
					empleado.setUsername(usuario.getUsername());
					empleado.setLegajo(Long.parseLong(legajo));
					empleado.setUsuarioSap(usuario.getUsuariosap());

					System.out.print("Buscando usuario sap con id " + usuario.getUsuariosap());

					employee = allEmployees.stream().filter(x -> x != null)
							.filter(x -> usuario.getUsuariosap().equals(x.getIdEmpleado().toString())).findFirst()
							.orElse(null);

					String horarioEntradaEmpleado = employee.getHoraInicio();

					if (horarioEntradaEmpleado == null) {
						errores.put(is, "El empleado " + employee.getLastName() + " " + employee.getFirstName()
								+ " no tiene horario de inicio cargado en SAP");
					}

					if (employee.getHoraFin() == null) {
						errores.put(is, "El empleado " + employee.getLastName() + " " + employee.getFirstName()
								+ " no tiene horario de salida cargado en SAP");
					}

				}

				// Fila siguiente: cabecera de registros
				String[] encabezadosEsperados = { "Dia", "Fecha", "Entrada", "Salida", "Entrada", "Salida", "Total dia",
						"Descanso", "Normal", "Extra 50%", "Extra 100%", "Tarde", "Comentarios" };

				Row headerRegistros = sheet.getRow(i + 1);

				if (headerRegistros == null) {
					errores.put(String.valueOf(i + 2), "No se encontró el encabezado de registros.");
					throw new ErrorValidationException(errores);
				}

				for (int columna = 0; columna < encabezadosEsperados.length; columna++) {

					String valor = FichadoUtils.getCellValue(headerRegistros.getCell(columna)).trim();

					if (!encabezadosEsperados[columna].equalsIgnoreCase(valor)) {
						errores.put(String.valueOf(i + 2),
								"Encabezado inválido en columna " + (columna + 1) + ". Se esperaba '"
										+ encabezadosEsperados[columna] + "' y se encontró '" + valor + "'.");

						throw new ErrorValidationException(errores);
					}
				}

				// Encabezado correcto, comienzo a procesar registros
				i += 2;

				List<RegistroFichadoDTO> registros = new ArrayList<>();

				// Una vez que encontré la cabecera lo siguiente es

				while (i <= sheet.getLastRowNum()) {

					// Si lo siguiente es una fila vacia me pongo a buscar otra vez una cabecera
					fila = sheet.getRow(i);

					if (fila == null || FichadoUtils.isRowEmpty(fila)) {
						i++; // saltamos fila vacía
						break;
					}

					is = String.valueOf(fila.getRowNum() + 1);

					// Obtengo la columna comentarios
					String comentarios = FichadoUtils.getCellValue(fila.getCell(12)).trim();

					// Si esta vacia o no empieza con FRAN puedo continuar ya que tambien se
					// incluyen sabados y domingos
					if (comentarios == null || !comentarios.startsWith("FRAN")) {

						// Armo un nuevo registro de fichado
						RegistroFichadoDTO registro = new RegistroFichadoDTO();

						// Fecha del registro formato dd/MM/yy
						Cell cell = fila.getCell(0);
						String fecha = cell.toString().trim();
						try {
							LocalDate fechaDate = LocalDate.parse(fecha,
									DateTimeFormatter.ofPattern("dd/MM/uu").withResolverStyle(ResolverStyle.STRICT));
							registro.setDia(fechaDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
						} catch (DateTimeParseException e) {
							errores.put(is + " " + CellReference.convertNumToColString(0), "Fecha inválida: " + fecha);
						}

						// Dia de la semana (lunes, martes, etc...)
						registro.setFecha(FichadoUtils.getCellValue(fila.getCell(1)).trim());

						boolean par1Valido = false;
						boolean par2Valido = false;

						// ============================================================
						// ENTRADA 1 - Obligatoria
						// ============================================================

						String entrada1 = FichadoUtils.getCellValue(fila.getCell(2)).trim();
						boolean entrada1Valida = false;

						if (StringUtils.isBlank(entrada1)) {

							errores.put(is + " " + CellReference.convertNumToColString(2), "Primer hora entrada vacía");

						} else if (!FichadoUtils.esHoraValida(entrada1)) {

							errores.put(is + " " + CellReference.convertNumToColString(2),
									"Primer hora entrada inválida: " + entrada1);

						} else {

							registro.setEntrada1(entrada1);
							entrada1Valida = true;
						}

						// ============================================================
						// SALIDA 1 - Obligatoria
						// ============================================================

						String salida1 = FichadoUtils.getCellValue(fila.getCell(3)).trim();
						boolean salida1Valida = false;

						if (StringUtils.isBlank(salida1)) {

							errores.put(is + " " + CellReference.convertNumToColString(3), "Primer hora salida vacía");

						} else if (!FichadoUtils.esHoraValida(salida1)) {

							errores.put(is + " " + CellReference.convertNumToColString(3),
									"Primer hora salida inválida: " + salida1);

						} else {

							registro.setSalida1(salida1);
							salida1Valida = true;
						}

						// ============================================================
						// VALIDACIÓN DEL PRIMER PAR
						// ============================================================

						if (entrada1Valida && salida1Valida) {

							if (!FichadoUtils.isHorarioSiguienteCorrecto(entrada1, salida1)) {

								errores.put(is, "Primer hora salida no es posterior a primer hora entrada");

							} else {

								par1Valido = true;
							}
						}

						// ============================================================
						// ENTRADA 2 - Opcional
						// ============================================================

						String entrada2 = FichadoUtils.getCellValue(fila.getCell(4)).trim();
						boolean entrada2Valida = false;

						if (!StringUtils.isBlank(entrada2)) {

							if (!FichadoUtils.esHoraValida(entrada2)) {

								errores.put(is + " " + CellReference.convertNumToColString(4),
										"Segunda hora entrada inválida: " + entrada2);

							} else {

								entrada2Valida = true;

								if (salida1Valida && !FichadoUtils.isHorarioSiguienteCorrecto(salida1, entrada2)) {

									errores.put(is, "Segunda hora entrada no es posterior a primer hora salida");

								} else {

									registro.setEntrada2(entrada2);
								}
							}
						}

						// ============================================================
						// SALIDA 2 - Opcional
						// ============================================================

						String salida2 = FichadoUtils.getCellValue(fila.getCell(5)).trim();
						boolean salida2Valida = false;

						if (!StringUtils.isBlank(salida2)) {

							if (!FichadoUtils.esHoraValida(salida2)) {

								errores.put(is + " " + CellReference.convertNumToColString(5),
										"Segunda hora salida inválida: " + salida2);

							} else {

								salida2Valida = true;
								registro.setSalida2(salida2);
							}
						}

						// ============================================================
						// VALIDACIÓN DEL SEGUNDO PAR
						// ============================================================

						// Si hay entrada2, debe haber salida2
						if (entrada2Valida && !salida2Valida) {

							errores.put(is + " " + CellReference.convertNumToColString(5), "Segunda hora salida vacía");
						}

						// Si hay salida2, debe haber entrada2
						if (salida2Valida && !entrada2Valida) {

							errores.put(is + " " + CellReference.convertNumToColString(4),
									"Segunda hora entrada vacía");
						}

						// Si ambas son válidas, validar que salida2 sea posterior a entrada2
						if (entrada2Valida && salida2Valida) {

							if (!FichadoUtils.isHorarioSiguienteCorrecto(entrada2, salida2)) {

								errores.put(is, "Segunda hora salida no es posterior a segunda hora entrada");

							} else {

								par2Valido = true;
							}
						}

						// Total Dia
						registro.setTotalDia(FichadoUtils.getCellValue(fila.getCell(6)).trim());
						

						// Descanso
						registro.setDescanso(FichadoUtils.getCellValue(fila.getCell(7)).trim());

						// VALIDAR SI VIENE VACIA.
						// SI EL ULTIMO FICHADO (DEL PRIMERO O SEGUNDO PERIODO) ES ANTES DEL HORARIO DE
						// FIN DEL EMPLEADO -> MOSTRAR BIEN
						// SI EL ULTIMO FICHADO (DEL PRIMERO O SEGUNDO PERIODO) ES DESPUES DEL HORARIO
						// DE FIN -> VALIDAR QUE TENGA HS EXTRAS AL 50 O 100 -> SINO TIENE ERROR
						// normal

						boolean par2Informado = false;
						
						par2Informado = !StringUtils.isBlank(entrada2)
						        || !StringUtils.isBlank(salida2);
						
						boolean paresValidosYConsecutivos = false;

						if (par1Valido) {

						    // No se informó segundo par: está todo bien
						    if (!par2Informado) {

						        paresValidosYConsecutivos = true;

						    // Se informó segundo par: debe ser válido y consecutivo
						    } else if (par2Valido
						            && FichadoUtils.isHorarioSiguienteCorrecto(salida1, entrada2)) {

						        paresValidosYConsecutivos = true;
						    }
						}
						
						String salidaFinal = null;

						if (paresValidosYConsecutivos) {
						    salidaFinal = par2Valido ? salida2 : salida1;
						}
						
						boolean debeTenerHorasExtras = false;
						
						String normal = FichadoUtils.getCellValue(fila.getCell(8)).trim();
						if (StringUtils.isBlank(normal)) {
							
							boolean ingresoEnHorario = false;

							if (entrada1Valida
							        && FichadoUtils.estaDentroDelHorarioContrato(entrada1, employee.getHoraFin())) {
							    ingresoEnHorario = true;
							}

							if (!ingresoEnHorario && entrada2Valida
							        && FichadoUtils.estaDentroDelHorarioContrato(entrada2, employee.getHoraFin())) {
							    ingresoEnHorario = true;
							}
							
							if (ingresoEnHorario) {
								errores.put(is + " " + CellReference.convertNumToColString(8), "El empleado fichó ingreso dentro del rango horario del contrato. Por lo tanto debe tener horas normales cargadas.");
							}
							
							
						} else if (!FichadoUtils.esHoraValida(normal)) {
							errores.put(is + " " + CellReference.convertNumToColString(8),
									"Normal invalida: " + normal);
						} else {
							registro.setNormal(normal);
						}

						if (salidaFinal != null && employee != null && FichadoUtils.isHorarioSiguienteCorrecto(employee.getHoraFin(), salidaFinal)) {
							debeTenerHorasExtras = true;
						}
						
						boolean horaextra50valida = false;
						// extra al 50 puede estar vacia
						String extra50 = FichadoUtils.getCellValue(fila.getCell(9)).trim();
						if (StringUtils.isBlank(extra50)) {

						} else if (FichadoUtils.esHoraValida(extra50)) {
							registro.setExtra50(extra50);
							horaextra50valida = true;
						} else {
							errores.put(is + " " + CellReference.convertNumToColString(9),
									"Extra 50% invalida: " + extra50);
						}

						boolean horaextra100valida = false;
						// extra al 100 puede estar vacia
						String extra100 = FichadoUtils.getCellValue(fila.getCell(10)).trim();
						if (StringUtils.isBlank(extra100)) {

						} else if (FichadoUtils.esHoraValida(extra100)) {
							registro.setExtra100(extra100);
							horaextra100valida = true;
						} else {
							errores.put(is + " " + CellReference.convertNumToColString(10),
									"Extra 100% invalida: " + extra100);
						}
						
						if (validarHorasExtras && debeTenerHorasExtras && !horaextra50valida && !horaextra100valida) {
							errores.put(is + " " + CellReference.convertNumToColString(10), "El empleado fichó salida posterior al horario de salida por contrato. Por lo tanto, debe tener horas extras al 50% o al 100% cargadas.");
						}
						
						registro.setTarde(FichadoUtils.getCellValue(fila.getCell(11)).trim());

						if (StringUtils.isEmpty(comentarios)
								|| FichadoUtils.isComentarioValido(codigosAusentismo, comentarios)) {
							registro.setComentarios(comentarios);
						} else {
							errores.put(is + " " + CellReference.convertNumToColString(12),
									"Columna obsevación con valor invalido: " + comentarios);
						}

						registros.add(registro);

						
						// FXIME validar la hora normal que tenga cargada si el horario de fichado de ingreso del empleado
						// esta entre los horarios de ingreso/salida asignados.
						
					}
					i++;
				}

				empleados.put(empleado, registros);
				// Salto fila de totales si está presente
				i++;
			}

			workbook.close();
			// file.close();

			if (!errores.isEmpty()) {
				errores.forEach((x, y) -> {
					System.out.println("Fila " + x + " - " + y);
				});
				throw new ErrorValidationException(errores);
			}

			empleados.forEach((x, y) -> empleadoFichadoList.add(new EmpleadoFichados(x, y)));
		}

		Comparator<EmpleadoFichados> comparator = new Comparator<EmpleadoFichados>() {
			public int compare(EmpleadoFichados o1, EmpleadoFichados o2) {
				return o1.getEmpleado().getLegajo().compareTo(o2.getEmpleado().getLegajo());
			};
		};

		empleadoFichadoList.sort(comparator);

		if (empleadoFichadoList.isEmpty()) {
			errores.put("0", "No se encontraron legajos para procesar");
			throw new ErrorValidationException(errores);
		}

		return empleadoFichadoList;
	}

	@Override
	public void enviarFichados(List<EmpleadoFichados> registros) {

		// Busco la fecha desde y hasta en todos los registros
		// Tambien armo un listado de todos los empleados

		Date desdeRango = null;
		Date hastaRango = null;
		List<Long> employeeIds = new ArrayList<>();

		// Recorro cada empleado y cada registro para saber la fecha desde minima y
		// hasta maxima de rango.
		for (EmpleadoFichados registro : registros) {

			// Obtengo el usuario SAP
			String usuarioSap = registro.getEmpleado().getUsuarioSap();

			// Agrego el empleado
			employeeIds.add(Long.parseLong(usuarioSap));

			// Voy tomando la fecha desde/hasta de todos los fichados en general de todos
			// los empleados
			for (RegistroFichadoDTO fichado : registro.getFichados()) {
				Date fechaFichado = DateUtils.toDate(fichado.getDia(), "dd/MM/yyyy");
				if (desdeRango == null || fechaFichado.before(desdeRango)) {
					desdeRango = fechaFichado;
				}

				if (hastaRango == null || hastaRango.before(fechaFichado)) {
					hastaRango = fechaFichado;
				}
			}
		}

		// Pido la sumatoria de horas por dia por usuario de acuerdo a las actividades
		String desdeFormatedo = DateUtils.toString(desdeRango, "yyyyMMdd");
		String hastaFormateado = DateUtils.toString(hastaRango, "yyyyMMdd");

		List<HorasPorEmpleadoDTO> horasPorFechaEmpleado = this.activityService.obtenerHorasAgrupadasPorFechaEmpleado(
				employeeIds, desdeFormatedo, hastaFormateado, exclusionesActividadesCalculoHorasNetas);

		// Los meto en un mapa
		Map<String, HorasPorEmpleadoDTO> mapaSegundosEmpleadoDiaActividad = new HashMap<>();
		horasPorFechaEmpleado.stream().forEach(registro -> {
			// Key usuario + "-" + fecha (60-2025-05-22)
			String key = registro.getAttendEmpl() + "-" + DateUtils.toString(registro.getSeStartDat(), "dd/MM/yyyy");
			mapaSegundosEmpleadoDiaActividad.put(key, registro);
		});

		Map<String, ProjectManagementTimeSheetGetDTO> mapaEmpleadoPeriodo = new HashMap<>();

		Map<String, String> errores = new HashMap<>();

		// Por cada empleado
		for (EmpleadoFichados registroEmpleadoDTO : registros) {

			// Obtengo usuario Sap
			Long usuarioSap = Long.parseLong(registroEmpleadoDTO.getEmpleado().getUsuarioSap());

			// Busco el empleado en SAP
			// FIXME para optimizar armar un query que traiga todo y armar un mapa
			EmployeesInfoReponseSapDTO employee = employeeService.getById(usuarioSap);

			boolean isServicioTecnico = "SI".equals(employee.getServicioTecnico());

			// FIXME Comidas
			// Servicio Tecnico SI -> reviso comidas
			// Servicio Tecnico NO -> ver si en el día tiene al menos una actividad
			// que no sea de taller y que finalice despues de las 12

			// Por cada registro de horas por dia
			for (RegistroFichadoDTO fichadoDTO : registroEmpleadoDTO.getFichados()) {

				String key = usuarioSap + "-" + fichadoDTO.getDia();
				HorasPorEmpleadoDTO horasPorEmpleadoDTO = mapaSegundosEmpleadoDiaActividad.get(key);

				double pagoHorasProductivas = horasPorEmpleadoDTO != null
						? horasPorEmpleadoDTO.getPagoHorasProductivas()
						: 0;

				boolean isTieneAcNoTallerDespuesMediodia = horasPorEmpleadoDTO != null
						? "Y".equals(horasPorEmpleadoDTO.getTieneActNoTallerDespuesMediodia())
						: false;

				boolean aplicaComida = isTieneAcNoTallerDespuesMediodia || isServicioTecnico;

				boolean tieneEntrada1 = StringUtils.isNotBlank(fichadoDTO.getEntrada1());
				boolean tieneSalida1 = StringUtils.isNotBlank(fichadoDTO.getSalida1());
				boolean tienenES1 = tieneEntrada1 && tieneSalida1;

				boolean tieneEntrada2 = StringUtils.isNotBlank(fichadoDTO.getEntrada2());
				boolean tieneSalida2 = StringUtils.isNotBlank(fichadoDTO.getSalida2());
				boolean tienenES2 = tieneEntrada2 && tieneSalida2;

				// Calculo el periodo por cada registro porque puede haber fechas de varios
				// periodos
				String[] split = fichadoDTO.getDia().split("/");
				String month = split[1].trim();
				String year = split[2].trim();

				String fechaDesdePeriodo = year + month + "01";

				Calendar instance = Calendar.getInstance();
				instance.set(Integer.parseInt(year), Integer.parseInt(month) - 1, 1);
				instance.add(Calendar.MONTH, 1);
				instance.add(Calendar.DAY_OF_MONTH, -1);

				String fechaHastaPeriodo = DateUtils.toString(instance.getTime(), "yyyyMMdd");

				// Armo el entry del mapa
				String entryMapCabecera = usuarioSap + "-" + year + month;

				// Obtengo del mapa el registro
				ProjectManagementTimeSheetGetDTO timesheet = mapaEmpleadoPeriodo.get(entryMapCabecera);

				// Si no lo tengo en el mapa
				if (timesheet == null) {
					// Quiere decir que es el primer registro

					// Primero lo pido a sap
					timesheet = this.timeSheetService.getTimeSheet(usuarioSap, fechaDesdePeriodo, fechaHastaPeriodo);
				}

				// Si todavia no hay cabecera de periodo tengo que crearla
				if (timesheet == null) {
					// Genero el timesheet en sap y obtengo el entry
					timesheet = new ProjectManagementTimeSheetGetDTO();
					Long timesheetabsentry = this.timeSheetService.generarTimeSheet(usuarioSap, fechaDesdePeriodo,
							fechaHastaPeriodo);
					timesheet.setAbsEntry(timesheetabsentry);
					timesheet.setDateFrom(fechaDesdePeriodo);
					timesheet.setDateTo(fechaHastaPeriodo);
					timesheet.setUserId(usuarioSap.toString());
				}

				Date fechaFichado = DateUtils.toDate(fichadoDTO.getDia(), "dd/MM/yyyy");
				String horaFichadoEntrada = fichadoDTO.getEntrada1();

				// Busco el registro repetido.
				List<ProjectManagementTimeSheetLineGetDTO> collect = timesheet.getLineas().stream()
						.filter(x -> FichadoUtils.buscarFechaHoraRepetida(fechaFichado, horaFichadoEntrada, x))
						.collect(Collectors.toList());

				// Si no hay registros lo agrego
				// FIXME HAY QUE VALIDAR TAMBIEN LOS HORARIOS PORQUE PUEDEN VENIR 1 DIA EN 2
				// REGISTROS CON DOS FICHADOS DIFERENTES
				// ESTO ES PORQUE UNA PARTE DEL DIA FUE DE LICENCIA Y EL OTRO NO.
				if (collect.isEmpty()) {

					// Obtengo el cantidad de lineas existentes + 1
					Long lineNum = timesheet.getLineas().size() + 1L;

					String fechaFichadoyyyyMMdd = DateUtils.toString(fechaFichado, "yyyyMMdd");

					// Obtengo el horario de entrada determinado por SAP
					String horarioEntradaEmpleado = employee.getHoraInicio();

					if (tienenES1 && tienenES2) {
						// Si tengo dos pares de entrada/salida tengo que traer las horas de las
						// actividades por dia partidas en esos horarios y generar 2 registros

						////////////////
						// Armo el primer par de entrada/salida
						////////////////

						// El total de fichado lo obtengo de la diferencia entre la primera
						// entrada/salida

						// Primero determino la hora de entrada. Si es posterior a la del empleado tomo
						// la posterior. Si llego temprano tomo la asignada al empleado.

						String diferenciaHorariaEntrada = DateUtils.calcularDiferenciaHorario(fichadoDTO.getEntrada1(),
								horarioEntradaEmpleado);

						String entrada = horarioEntradaEmpleado;

						if (diferenciaHorariaEntrada == null) {
							entrada = fichadoDTO.getEntrada1();
						}

						String totalFichadoHoras1 = DateUtils.calcularDiferenciaHorario(entrada,
								fichadoDTO.getSalida1());
						Integer totalFichadoSegundos1 = DateUtils.convertirATotalSegundos(totalFichadoHoras1);

						// Obtengo el total de horas por actividades del día y el empleado pero usando
						// la consulta a sap y no el mapa
						Integer totalActividadesEnSegundos = 0;
						List<HorasPorEmpleadoDTO> totalHorasES1List = this.activityService
								.obtenerHorasAgrupadasPorFechaEmpleado(usuarioSap, fechaFichadoyyyyMMdd,
										fechaFichadoyyyyMMdd, fichadoDTO.getEntrada1(), fichadoDTO.getSalida1(),
										exclusionesActividadesCalculoHorasNetas);
						if (totalHorasES1List.size() == 1)
							totalActividadesEnSegundos = totalHorasES1List.get(0).getDuration();

						Integer nofacturable = totalFichadoSegundos1 - totalActividadesEnSegundos;

						// Armo el nuevo registro de la collection de dias
						ProjectManagementTimeSheetLineGetDTO timesheetline = generarTimeSheetLine(aplicaComida,
								fechaFichado, nofacturable, lineNum, entrada, fichadoDTO.getSalida1(),
								fichadoDTO.getNormal(), fichadoDTO.getExtra50(), fichadoDTO.getExtra100(),
								fichadoDTO.getComentarios(), fichadoDTO.getEntrada1(), fichadoDTO.getTarde(), false,
								pagoHorasProductivas);

						timesheet.getLineas().add(timesheetline);

						////////////////
						// Armo el segundo par de entrada/salida
						////////////////

						// El total de fichado lo obtengo de la diferencia entre la primera
						// entrada/salida
						String totalFichadoHoras2 = DateUtils.calcularDiferenciaHorario(fichadoDTO.getEntrada2(),
								fichadoDTO.getSalida2());
						Integer totalFichadoSegundos2 = DateUtils.convertirATotalSegundos(totalFichadoHoras2);

						Integer totalActividadesEnSegundos2 = 0;
						List<HorasPorEmpleadoDTO> totalHorasES2List = this.activityService
								.obtenerHorasAgrupadasPorFechaEmpleado(usuarioSap, fechaFichadoyyyyMMdd,
										fechaFichadoyyyyMMdd, fichadoDTO.getEntrada2(), fichadoDTO.getSalida2(),
										exclusionesActividadesCalculoHorasNetas);
						if (totalHorasES2List.size() == 1)
							totalActividadesEnSegundos2 = totalHorasES2List.get(0).getDuration();

						Integer nofacturable2 = totalFichadoSegundos2 - totalActividadesEnSegundos2;

						// Armo el nuevo registro de la collection de dias
						ProjectManagementTimeSheetLineGetDTO timesheetline2 = generarTimeSheetLine(aplicaComida,
								fechaFichado, nofacturable2, lineNum + 1, fichadoDTO.getEntrada2(),
								fichadoDTO.getSalida2(), null, null, null, null, fichadoDTO.getEntrada2(), null, true,
								pagoHorasProductivas);

						timesheet.getLineas().add(timesheetline2);

						// Despues de agregar la linea a la collecion pongo el objeto en el mapa
						mapaEmpleadoPeriodo.put(entryMapCabecera, timesheet);

					} else {

						String diferenciaHorariaEntrada = DateUtils.calcularDiferenciaHorario(fichadoDTO.getEntrada1(),
								horarioEntradaEmpleado);

						String entrada = horarioEntradaEmpleado;

						if (diferenciaHorariaEntrada == null) {
							entrada = fichadoDTO.getEntrada1();
						}

						// Si tengo un solo par de entrada/salida uso el mapa creado con la sumatoria
						// general
						String totalFichadoHoras = DateUtils.calcularDiferenciaHorario(entrada,
								fichadoDTO.getSalida1());
						Integer totalFichadoSegundos = DateUtils.convertirATotalSegundos(totalFichadoHoras);

						// Obtengo el total de horas por actividades del día y el empleado
						Integer totalActividadesEnSegundosD = horasPorEmpleadoDTO != null
								? horasPorEmpleadoDTO.getDuration()
								: null;
						Integer totalActividadesEnSegundos = 0;
						if (totalActividadesEnSegundosD != null)
							totalActividadesEnSegundos = totalActividadesEnSegundosD.intValue();

						Integer nofacturableSegundos = totalFichadoSegundos - totalActividadesEnSegundos;

						// Armo el nuevo registro de la collection de dias
						ProjectManagementTimeSheetLineGetDTO timesheetline = generarTimeSheetLine(aplicaComida,
								fechaFichado, nofacturableSegundos, lineNum, entrada, fichadoDTO.getSalida1(),
								fichadoDTO.getNormal(), fichadoDTO.getExtra50(), fichadoDTO.getExtra100(),
								fichadoDTO.getComentarios(), fichadoDTO.getEntrada1(), fichadoDTO.getTarde(), false,
								pagoHorasProductivas);

						timesheet.getLineas().add(timesheetline);

						// Despues de agregar la linea a la collecion pongo el objeto en el mapa
						mapaEmpleadoPeriodo.put(entryMapCabecera, timesheet);

					}
				}
			}
		}

		// Una vez que arme el mapa, envio a sap las lineas de cada uno usando un
		// request por empleado
		for (ProjectManagementTimeSheetGetDTO lineGroup : mapaEmpleadoPeriodo.values()) {
			try {
				this.timeSheetService.updateTimeSheetLines(lineGroup);
			} catch (ErrorValidationException e) {
				String error = "";
				for (Map.Entry<String, String> me : e.getErrors().entrySet()) {
					error += me.getKey() + ": " + me.getValue() + " ";
					;
				}
				errores.put("Error al enviar fichaje para usuario sap " + lineGroup.getUserId(), error);

			} catch (Exception e) {
				errores.put("Error al enviar fichaje para usuario sap " + lineGroup.getUserId(),
						SapBusinessException.generarStackTraceString(e));
			}
		}

		if (!errores.isEmpty()) {
			throw new ErrorValidationException(errores);
		}

	}

	private ProjectManagementTimeSheetLineGetDTO generarTimeSheetLine(boolean aplicaComida, Date fechaFichado,
			Integer nofacturableSegundos, Long lineNum, String entrada1, String salida1, String normal, String extra50,
			String extra100, String comentarios, String horaFichado, String tarde, boolean segundoRegistro,
			double pagarHorasProductivas) {
		ProjectManagementTimeSheetLineGetDTO timesheetline = new ProjectManagementTimeSheetLineGetDTO();

		timesheetline.setDate(DateUtils.toString(fechaFichado, "yyyy-MM-dd"));

		timesheetline.setStartTime(entrada1);
		timesheetline.setEndTime(salida1);

		// FIXME cuando se cambie el proceso para ejecutarse en el preview
		// Si nofacturableSegundos < 0 -> mostrar un alerta. Porque no peude ser que
		// tenga mas horas
		// de actividad que de fichado.

		if (nofacturableSegundos > 0)
			timesheetline.setNonBillableTime(DateUtils.convertirSegundosAFormato(nofacturableSegundos));

		timesheetline.setHoraFichado(horaFichado);

		// Nuevo campo horas productivas
		timesheetline.setPagarHorasProductivas(String.valueOf(pagarHorasProductivas));

		// Obtengo las claves de ferido e injustificado
		Set<String> feriados = new HashSet(Arrays.asList(licenciasFeriado.split(",")));
		Set<String> injustificados = new HashSet(Arrays.asList(licenciasNoJustificada.split(",")));

		boolean esFeriado = FichadoUtils.isComentarioValido(feriados, comentarios);
		boolean esInjustificado = FichadoUtils.isComentarioValido(injustificados, comentarios);

		if (!esFeriado && !esInjustificado) {
			// Si no es feriado ni tampoco injustificada, son normales y tambien cargo las
			// extras al 50 y al 100
			timesheetline.setHorasNormales(normal);
			timesheetline.setHorasExtras50(extra50);
			timesheetline.setHorasExtras100(extra100);
		} else {

			// Si es feriado o injustificada

			if (esFeriado) {
				// Si es feriado, cargo las horas de feriado
				timesheetline.setHorasFeriado(normal);
			} else if (esInjustificado) {
				// Si es injustificado, cargo las horas de injustificadas
				timesheetline.setHorasInjustificadas(normal);
			}

			// Si es feriado y trabajo horas extras, tomo las horas extras como horas extras
			// de feriado
			if (esFeriado && StringUtils.isNotBlank(extra100)) {
				// Marco las horas extras como feriado
				timesheetline.setHorasExtrasFeriados(extra100);
			}

		}

		timesheetline.setTipoAusentismo(comentarios);
		timesheetline.setTarde(tarde);

		// Comida va solo para el primer registro
		if (!segundoRegistro) {
			// Pongo la comida como default en no
			timesheetline.setComida("No");

			// Sino hay ausentismo, entonces reviso si va la comida o no.
			if (StringUtils.isBlank(comentarios) || (esFeriado && StringUtils.isNotBlank(extra100)))
				timesheetline.setComida(aplicaComida ? "Si" : "No");
		}

		timesheetline.setLineId(lineNum);
		return timesheetline;
	}

	private Map<String, Boolean> getCodigosAusentismo() {

		String sql = "select actType, chargeable from pmc5";

		Map<String, Boolean> codigos = new HashMap<String, Boolean>();

		try (Connection conn = sqlConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				String codigo = rs.getString("actType");
				Boolean justificado = rs.getString("chargeable").equals("Y");
				codigos.put(codigo, justificado);
			}

			sqlConnection.getConnection().close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return codigos;
	}

}
