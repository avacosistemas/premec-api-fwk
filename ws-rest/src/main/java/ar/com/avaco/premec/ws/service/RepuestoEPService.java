package ar.com.avaco.premec.ws.service;

import java.util.List;

import ar.com.avaco.premec.ws.dto.repuesto.RepuestoDepositoDTO;

public interface RepuestoEPService {

	List<RepuestoDepositoDTO> getRepuestos(String username) throws Exception;

}
