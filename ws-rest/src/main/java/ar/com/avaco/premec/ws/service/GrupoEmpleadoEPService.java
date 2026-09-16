package ar.com.avaco.premec.ws.service;

import java.util.List;

import ar.com.avaco.fwk.core.component.epservice.CRUDEPService;
import ar.com.avaco.premec.dto.GrupoEmpleadoDTO;
import ar.com.avaco.premec.dto.UsuarioEmpleadoDTO;

public interface GrupoEmpleadoEPService extends CRUDEPService<Long, GrupoEmpleadoDTO> {

	List<UsuarioEmpleadoDTO> listUsuarios();

}
