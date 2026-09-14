package ar.com.avaco.premec.ws.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.com.avaco.premec.dto.ReclamoFilterDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallActivityDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallReclamoListDTO;
import ar.com.avaco.premec.sap.service.ServiceCallSapService;

@Service
public class ReclamoEPServiceImpl extends AbstractSapService implements ReclamoEPService {

	@Autowired
	private ServiceCallSapService serviceCallservice;
	
	@Override
	public List<ServiceCallReclamoListDTO> list(ReclamoFilterDTO filter) {
		return serviceCallservice.getServiceCalls(filter);
	}

	public List<ServiceCallActivityDTO> listActividades (Long idServiceCall) {
		return serviceCallservice.getActivitiesByServiceCall(idServiceCall);
	}

}
