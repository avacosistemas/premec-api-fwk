package ar.com.avaco.premec.ws.dto.timesheet;

import org.apache.pdfbox.pdmodel.PDDocument;

public class ReciboSueldoArchivoDTO {

	private ReciboSueldoDTO reciboSueldo;

	private PDDocument document;

	public ReciboSueldoArchivoDTO(ReciboSueldoDTO reciboSueldo, PDDocument document) {
		super();
		this.reciboSueldo = reciboSueldo;
		this.document = document;
	}

	public ReciboSueldoDTO getReciboSueldo() {
		return reciboSueldo;
	}

	public void setReciboSueldo(ReciboSueldoDTO reciboSueldo) {
		this.reciboSueldo = reciboSueldo;
	}

	public PDDocument getDocument() {
		return document;
	}

	public void setDocument(PDDocument document) {
		this.document = document;
	}

}
