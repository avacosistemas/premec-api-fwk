package ar.com.avaco.premec.ws.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import ar.com.avaco.premec.ws.dto.timesheet.ProjectManagementTimeSheetAttachDTO;
import ar.com.avaco.premec.ws.dto.timesheet.ProjectManagementTimeSheetGetDTO;

public interface TimeSheetService {

	Long generarTimeSheet(Long usuarioSap, String fechaDesdePeriodo, String fechaHastaPeriodo, BigDecimal neto, BigDecimal sueldoJornal);

	TimeSheetEntryAttach getTimeSheetEntries(Long usuarioSap, String from, String to);

	List<ProjectManagementTimeSheetAttachDTO> listTimeSheetByUsuarioSap(Long usuarioSAP);

	void updateTimeSheetLines(ProjectManagementTimeSheetGetDTO lineGroup);

	ProjectManagementTimeSheetGetDTO getTimeSheet(Long usuarioSap, String fechaDesde, String fechaHasta);

	List<ProjectManagementTimeSheetGetDTO> getTimeSheets(String from, String to);

	void updateTimeSheetAttachmentEntry(Long absEntry, Map<String, Object> values);

	Long generarTimeSheet(Long usuarioSap, String fechaDesdePeriodo, String fechaHastaPeriodo);

	void updateTimeSheet(Long absEntry, Long newAttachmentEntry, BigDecimal neto, BigDecimal sueldoJornal);

	void updateTimeSheet(Long absEntry, Long newAttachmentEntry);

}
