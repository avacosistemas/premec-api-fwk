package ar.com.avaco.premec.utils;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import ar.com.avaco.fwk.core.utils.DateUtils;
import ar.com.avaco.premec.ws.dto.timesheet.ProjectManagementTimeSheetLineGetDTO;

public class FichadoUtils {

	public static boolean isComentarioValido(Set<String> codigosAusentismoInjustificado, String comentarios) {
		if (comentarios == null) {
			comentarios = "";
		}
		return codigosAusentismoInjustificado.stream().anyMatch(comentarios::startsWith);
	}

	public static boolean isHorarioSiguienteCorrecto(String desde, String hasta) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("H:mm").optionalStart()
				.appendPattern(":ss").optionalEnd().toFormatter().withResolverStyle(ResolverStyle.STRICT);

		LocalTime desdeHora = LocalTime.parse(desde, formatter);
		LocalTime hastaHora = LocalTime.parse(hasta, formatter);

		return desdeHora.isBefore(hastaHora);
	}

	public static boolean esHoraValida(String hora) {
		try {
			DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("H:mm").optionalStart()
					.appendPattern(":ss").optionalEnd().toFormatter().withResolverStyle(ResolverStyle.STRICT);

			LocalTime.parse(hora, formatter);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	public static String getCellValue(Cell cell) {
		if (cell == null)
			return "";
		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
			}
			return String.valueOf((int) cell.getNumericCellValue());
		default:
			return "";
		}
	}

	public static boolean isRowEmpty(Row row) {
		for (Cell cell : row) {
			if (cell != null && cell.getCellType() != CellType.BLANK && !cell.getStringCellValue().trim().equals(""))
				return false;
		}
		return true;
	}

	public static boolean buscarFechaHoraRepetida(Date fechaFichado, String horaFichadoEntrada,
			ProjectManagementTimeSheetLineGetDTO x) {
		boolean coincideFecha = DateUtils.convertirSinTimeZome(x.getDate())
				.equals(DateUtils.toString(fechaFichado, "dd/MM/yyyy"));
		boolean coincideHorario = x.getHoraFichado().equals(horaFichadoEntrada);
		return coincideFecha && coincideHorario;
	}

	public static boolean estaDentroDelHorarioContrato(String fichadoIngreso, String horarioSalidaContrato) {

		return FichadoUtils.isHorarioSiguienteCorrecto(fichadoIngreso, horarioSalidaContrato)
				|| fichadoIngreso.equals(horarioSalidaContrato);
	}

}
