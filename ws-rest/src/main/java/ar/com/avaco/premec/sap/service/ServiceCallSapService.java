package ar.com.avaco.premec.sap.service;

import java.util.List;

import ar.com.avaco.premec.dto.ReclamoFilterDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallActivityDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallCustomerStatsDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallMachineStatsDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallReclamoListDTO;

public interface ServiceCallSapService {

	List<ServiceCallReclamoListDTO> getServiceCalls(ReclamoFilterDTO filter); 
	
	List<ServiceCallActivityDTO> getActivitiesByServiceCall(Long serviceCallId);

	List<ServiceCallMachineStatsDTO> getEstadisticasMaquinaParada(String maquinasJson, String periodosJson);

	List<ServiceCallCustomerStatsDTO> getEstadisticasCliente(String clientesJson, String periodosJson);
	
}
