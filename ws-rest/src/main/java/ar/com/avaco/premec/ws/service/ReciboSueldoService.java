package ar.com.avaco.premec.ws.service;

import java.io.IOException;
import java.util.List;

import ar.com.avaco.fwk.core.component.dto.PageDTO;
import ar.com.avaco.premec.ws.dto.ReciboFilterDTO;
import ar.com.avaco.premec.ws.dto.timesheet.ReciboSueldoDTO;
import ar.com.avaco.premec.ws.dto.timesheet.RegistroReciboPorUsuarioDTO;

public interface ReciboSueldoService {

	void rechazarRecibos(List<ReciboSueldoDTO> lote);

	void aprobarRecibos(List<ReciboSueldoDTO> lote);

	List<RegistroReciboPorUsuarioDTO> listarRecibosPorUsuario();

	byte[] obtenerReciboPDF(RegistroReciboPorUsuarioDTO recibo) throws IOException;

	List<ReciboSueldoDTO> procesarRecibos(String tipo, byte[] archivo) throws IOException;

	void firmarReciboPDF(RegistroReciboPorUsuarioDTO recibo) throws IOException;

	PageDTO<RegistroReciboPorUsuarioDTO> listarRecibos(ReciboFilterDTO filter);

}
