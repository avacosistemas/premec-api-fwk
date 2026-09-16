package ar.com.avaco.premec.ws.service;

import ar.com.avaco.fwk.core.component.epservice.CRUDEPService;
import ar.com.avaco.premec.dto.UsuarioPremecDTO;

public interface UsuarioPremecEPService extends CRUDEPService<Long, UsuarioPremecDTO> {

	UsuarioPremecDTO findByUsername(String name);

}
