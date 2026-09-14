package ar.com.avaco.premec.ws.service;

import java.util.List;

import ar.com.avaco.premec.dto.ReclamoFilterDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallActivityDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallReclamoListDTO;

public interface ReclamoEPService {

	List<ServiceCallReclamoListDTO> list(ReclamoFilterDTO filter);

	List<ServiceCallActivityDTO> listActividades(Long idServiceCall);

}
