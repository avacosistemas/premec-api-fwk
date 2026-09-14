package ar.com.avaco.premec.ws.service;

import java.util.Map;

import ar.com.avaco.premec.sap.dto.CustomerReclamoStatsRequestDTO;
import ar.com.avaco.premec.sap.dto.EstadisticaClienteDTO;
import ar.com.avaco.premec.sap.dto.EstadisticaMaquinaDTO;
import ar.com.avaco.premec.sap.dto.MachineReclamoStatsRequestDTO;

public interface ReclamoEstadisticasEPService {

	Map<String, EstadisticaMaquinaDTO> getEstadisticasMaquinaParada(MachineReclamoStatsRequestDTO dto);

	Map<String, EstadisticaClienteDTO> getEstadisticasCliente(CustomerReclamoStatsRequestDTO dto);

}
