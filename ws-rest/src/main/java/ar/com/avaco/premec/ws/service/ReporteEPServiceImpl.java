package ar.com.avaco.premec.ws.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.itextpdf.text.DocumentException;

import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.premec.reporte.InformeBuilder;
import ar.com.avaco.premec.reporte.InformeBuilderChecklist;
import ar.com.avaco.premec.reporte.InformeBuilderEntrega;
import ar.com.avaco.premec.reporte.InformeBuilderPiezasRepararMantenimientoMaquinaria;
import ar.com.avaco.premec.reporte.InformeBuilderReparaciones;
import ar.com.avaco.premec.sap.dto.ItemCodeDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.sap.service.ItemCodeService;
import ar.com.avaco.premec.service.ReporteService;
import ar.com.avaco.premec.ws.dto.actividad.ActividadReporteDTO;
import ar.com.avaco.premec.ws.dto.attachment.AttachmentLine;
import ar.com.avaco.premec.ws.dto.attachment.ResponseAttachmentGetPost;

@Service("reporteEPService")
public class ReporteEPServiceImpl implements ReporteEPService {

	@Value("${informe.path}")
	private String informePath;

	@Value("${email.bodyMail}")
	private String bodyMail;

	@Value("${email.subjectMail}")
	private String subjectMail;

	@Value("${email.bodyMailEntrega}")
	private String bodyMailEntrega;

	@Value("${email.subjectMailEntrega}")
	private String subjectMailEntrega;

	private String body;

	private String subject;

	private ReporteService reporteService;

	@Autowired
	private ItemCodeService itemCodeService;

	@Autowired
	private AttachmentService attachmentService;

	@Override
	public void enviarReporte(ActividadReporteDTO eldto)
			throws MalformedURLException, DocumentException, IOException, SapBusinessException {
		generarReporte(eldto);

		String path = null;

		// Si es un traslado
		if (eldto.getCodigoArticulo().equals("TRASLADO1") || eldto.getCodigoArticulo().equals("TRASLADO3")
				|| eldto.getCodigoArticulo().equals("TRASLADO4")) {

			String tipoTarea = "EC".equals(eldto.getTipoActividad()) ? "COMBUSTION"
					: "EE".equals(eldto.getTipoActividad()) ? "ELECTRICAS" : null;

			if (tipoTarea != null) {

				ItemCodeDTO byItemCode = itemCodeService.getByItemCode(eldto.getCodigoArticulo());
				ResponseAttachmentGetPost attachment = attachmentService.getAttachment(byItemCode.getAttachmentEntry());

				AttachmentLine attachmentLine = attachment.getAttachments2Lines().stream()
						.filter(x -> x.getFreeText().equals(tipoTarea)).findFirst().get();

				path = attachmentLine.getTargetPath() + "\\" + attachmentLine.getFileName() + "."
						+ attachmentLine.getFileExtension();

			}

		}

		reporteService.sendMail(eldto.getEmail(), eldto.getIdActividad().toString(), body, subject, path);
	}

	@Override
	public ResponseEntity<JSONResponse> generarReporte(ActividadReporteDTO eldto)
			throws FileNotFoundException, DocumentException, IOException {
		InformeBuilder ib = null;

		switch (eldto.getTipoActividad()) {
		case "EE":
		case "EC":
		case "EP":
			ib = new InformeBuilderEntrega(eldto, informePath);
			body = bodyMailEntrega;
			subject = subjectMailEntrega;
			break;
		case "R":
			ib = new InformeBuilderReparaciones(eldto, informePath);
			body = bodyMail;
			subject = subjectMail;
			break;
		case "C":
			ib = new InformeBuilderChecklist(eldto, informePath);
			body = bodyMail;
			subject = subjectMail;
			break;
		case "P":
			ib = new InformeBuilderPiezasRepararMantenimientoMaquinaria(eldto, informePath);
			body = bodyMail;
			subject = subjectMail;
			break;
		case "M":
			ib = new InformeBuilderPiezasRepararMantenimientoMaquinaria(eldto, informePath);
			body = bodyMail;
			subject = subjectMail;
			break;
		default:
			break;
		}

		return ib.generarReporte();
	}

	@Resource(name = "reporteService")
	public void setService(ReporteService reporteService) {
		this.reporteService = reporteService;
	}

}
