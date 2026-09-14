package ar.com.avaco.premec.ws.controller;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.premec.utils.BuscarTextoYStripper;
import ar.com.avaco.premec.ws.dto.ArchivoDTO;
import ar.com.avaco.premec.ws.dto.timesheet.ArchivoReciboDTO;
import ar.com.avaco.premec.ws.dto.timesheet.ReciboSueldoDTO;
import ar.com.avaco.premec.ws.dto.timesheet.RegistroReciboPorUsuarioDTO;
import ar.com.avaco.premec.ws.service.ReciboSueldoService;

@RestController
public class ReciboSueldoRestController {

	@Autowired
	private ReciboSueldoService reciboService;

	@RequestMapping(value = "/procesarRecibos", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> procesarRecibos(@RequestBody ArchivoReciboDTO archivo) {
		JSONResponse response = new JSONResponse();
		try {
			response.setData(this.reciboService.procesarRecibos(archivo.getTipo(), archivo.getArchivo()));
			response.setStatus(JSONResponse.OK);
		} catch (Exception e) {
			response.setStatus(JSONResponse.ERROR);
			response.setData(e);
			e.printStackTrace();
		}
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/aprobarRechazarRecibos", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> aprobarRecibos(@RequestBody List<ReciboSueldoDTO> recibos) {
		JSONResponse response = new JSONResponse();
		try {
			this.reciboService.aprobarRecibos(
					recibos.stream().filter(recibo -> recibo.getAprobado()).collect(Collectors.toList()));
			this.reciboService.rechazarRecibos(
					recibos.stream().filter(recibo -> !recibo.getAprobado()).collect(Collectors.toList()));
			response.setStatus(JSONResponse.OK);
		} catch (Exception e) {
			response.setStatus(JSONResponse.ERROR);
			response.setData(e);
			e.printStackTrace();
		}
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/listarRecibosPorUsuario", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> listarRecibosPorUsuario() {
		JSONResponse response = new JSONResponse();
		try {
			List<RegistroReciboPorUsuarioDTO> recibos = this.reciboService.listarRecibosPorUsuario();
			response.setData(recibos);
			response.setStatus(JSONResponse.OK);
		} catch (Exception e) {
			response.setStatus(JSONResponse.ERROR);
			response.setData(e);
			e.printStackTrace();
		}
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/descargarRecibo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> descargarRecibo(RegistroReciboPorUsuarioDTO recibo) {
		JSONResponse response = new JSONResponse();
		try {
			byte[] recibopdf = this.reciboService.obtenerReciboPDF(recibo);
			String fileName = recibo.getTipo() + "-" + recibo.getYear() + "-" + recibo.getMonthString() + ".pdf";
			ArchivoDTO arc = new ArchivoDTO();
			arc.setFile(recibopdf);
			arc.setFileName(fileName);
			response.setData(arc);
			response.setStatus(JSONResponse.OK);
		} catch (Exception e) {
			response.setStatus(JSONResponse.ERROR);
			response.setData(e);
			e.printStackTrace();
		}
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/firmarRecibo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONResponse> firmarRecibo(RegistroReciboPorUsuarioDTO recibo) {
		JSONResponse response = new JSONResponse();
		try {
			this.reciboService.firmarReciboPDF(recibo);
			response.setData(true);
			response.setStatus(JSONResponse.OK);
		} catch (Exception e) {
			response.setStatus(JSONResponse.ERROR);
			response.setData(e);
			e.printStackTrace();
		}
		return new ResponseEntity<JSONResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/procesarRecibos2", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public void procesarRecibos() throws IOException {

		try (PDDocument document = PDDocument.load(new File("D:/desarrollo/premec/recibo-nuevo-formato.pdf"))) {

			float pageHeight = document.getPage(0).getMediaBox().getHeight();

			// Rect para extracción (invertido)
			Rectangle rectPeriodo = new Rectangle(28, 134, 90, 12);
			Rectangle rectLegajo = new Rectangle(28, 165, 90, 12);
			Rectangle rectSueldoJornal = new Rectangle(490, 210, 100, 12);
			Rectangle rectDescripcion = new Rectangle(120, 134, 236, 12);

			Rectangle rectNombre = new Rectangle(120, 165, 236, 12);
			
			Rectangle rectFirma = new Rectangle(113, 733, 100, 29);

			PDFTextStripperByArea stripper = new PDFTextStripperByArea();
			stripper.setSortByPosition(true);
			stripper.addRegion("legajo", rectLegajo);
			stripper.addRegion("periodo", rectPeriodo);
			stripper.addRegion("descripcion", rectDescripcion);
			stripper.addRegion("nombre", rectNombre);
			
			stripper.addRegion("sueldoJornal", rectSueldoJornal);
			stripper.addRegion("firma", rectFirma);

			for (int i = 0; i < document.getNumberOfPages(); i++) {

				PDPage page = document.getPage(i);

				BuscarTextoYStripper buscador = new BuscarTextoYStripper("SUELDO NETO:");

				buscador.setStartPage(i + 1);
				buscador.setEndPage(i + 1);
				buscador.getText(document);

				try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
						PDPageContentStream.AppendMode.APPEND, true, true)) {

					dibujarRectangulo(contentStream, pageHeight, rectLegajo);
					dibujarRectangulo(contentStream, pageHeight, rectPeriodo);
					dibujarRectangulo(contentStream, pageHeight, rectDescripcion);
					dibujarRectangulo(contentStream, pageHeight, rectNombre);
					
						if (buscador.getYEncontrada() != null) {
							int ySueldoNeto = buscador.getYEncontrada().intValue();
							Rectangle rectNeto = new Rectangle(526, ySueldoNeto, 64, 13);
							stripper.addRegion("neto", rectNeto);
							dibujarRectangulo(contentStream, pageHeight, rectNeto);
						}
					
					dibujarRectangulo(contentStream, pageHeight, rectSueldoJornal);
					dibujarRectangulo(contentStream, pageHeight, rectFirma);
					
					String pathFirma = "D:/desarrollo/premec/firma.jpg"; // luego lo reemplazás

				    PDImageXObject firma = PDImageXObject.createFromFile(pathFirma, document);

				    contentStream.drawImage(
				            firma,
				            rectFirma.x,
				            pageHeight - rectFirma.y - rectFirma.height,
				            rectFirma.width,
				            rectFirma.height
				    );
					
				}


				stripper.extractRegions(page);

				String textoLegajo = stripper.getTextForRegion("legajo").replaceAll("\\s+", "").trim();
				String periodo = stripper.getTextForRegion("periodo").trim();
				String descripcion = stripper.getTextForRegion("descripcion").trim();
				String nombre = stripper.getTextForRegion("nombre").trim();
				String textoNeto = stripper.getTextForRegion("neto").trim().replace("\\n", "").replace("\\n", "");
				System.out.print(textoLegajo + " " + periodo + " " + descripcion + " " + nombre + " " + textoNeto);

				if (buscador.getYEncontrada() != null) {
					String textoSueldoJornal = stripper.getTextForRegion("sueldoJornal").trim().replace("\\n", "").replace("\\n", "");
					System.out.println("textoSueldoJornal " + textoSueldoJornal);
				}
				
				System.out.println();
				document.save("D:/desarrollo/premec/recibo-nuevo-formato-marcado.pdf");

			}

		}

	}

	private void dibujarRectangulo(PDPageContentStream contentStream, float pageHeight, Rectangle rect)
			throws IOException {

		float x = rect.x;
		float y = pageHeight - rect.y - rect.height;

		contentStream.setStrokingColor(255, 0, 0); // rojo
		contentStream.setLineWidth(1f);

		contentStream.addRect(x, y, rect.width, rect.height);

		contentStream.stroke();
	}

}
