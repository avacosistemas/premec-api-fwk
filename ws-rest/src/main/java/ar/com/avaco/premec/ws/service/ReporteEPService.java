package ar.com.avaco.premec.ws.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.http.ResponseEntity;

import com.itextpdf.text.DocumentException;

import ar.com.avaco.fwk.core.component.dto.JSONResponse;
import ar.com.avaco.premec.ws.dto.actividad.ActividadReporteDTO;

public interface ReporteEPService {

	void enviarReporte(ActividadReporteDTO eldto) throws MalformedURLException, DocumentException, IOException;

	ResponseEntity<JSONResponse> generarReporte(ActividadReporteDTO eldto) throws FileNotFoundException, DocumentException, IOException;

}
