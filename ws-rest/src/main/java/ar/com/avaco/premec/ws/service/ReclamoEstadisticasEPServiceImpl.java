package ar.com.avaco.premec.ws.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.avaco.premec.sap.dto.ClientePeriodoDTO;
import ar.com.avaco.premec.sap.dto.CustomerReclamoStatsRequestDTO;
import ar.com.avaco.premec.sap.dto.EstadisticaClienteDTO;
import ar.com.avaco.premec.sap.dto.EstadisticaMaquinaDTO;
import ar.com.avaco.premec.sap.dto.MachineReclamoStatsRequestDTO;
import ar.com.avaco.premec.sap.dto.MaquinaParadaPeriodoDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallCustomerStatsDTO;
import ar.com.avaco.premec.sap.dto.ServiceCallMachineStatsDTO;
import ar.com.avaco.premec.sap.service.ServiceCallSapService;

@Service
public class ReclamoEstadisticasEPServiceImpl extends AbstractSapService implements ReclamoEstadisticasEPService {

	@Autowired
	private ServiceCallSapService serviceCallservice;

	@Override
	public Map<String, EstadisticaMaquinaDTO> getEstadisticasMaquinaParada(MachineReclamoStatsRequestDTO dto) {

		ObjectMapper mapper = new ObjectMapper();

		String periodosJson = "";
		String maquinasJson = "";

		try {
			periodosJson = mapper.writeValueAsString(dto.getPeriodos());
			maquinasJson = mapper.writeValueAsString(dto.getMaquinas());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		List<ServiceCallMachineStatsDTO> listado = this.serviceCallservice.getEstadisticasMaquinaParada(maquinasJson,
				periodosJson);

		Map<String, EstadisticaMaquinaDTO> maquinaPeriodoMap = new HashMap<String, EstadisticaMaquinaDTO>();

		listado.forEach(e -> {

			EstadisticaMaquinaDTO estadisticaMaquinaDTO = maquinaPeriodoMap.get(e.getMaquina());
			if (estadisticaMaquinaDTO == null)
				estadisticaMaquinaDTO = new EstadisticaMaquinaDTO();

			if (e.getTotalGeneral().booleanValue()) {
				estadisticaMaquinaDTO.setCantidadReclamosTotal(e.getCantidadReclamos());
				estadisticaMaquinaDTO.setDiasParadaTotalTotal(e.getDiasParadaTotal());
			} else {
				MaquinaParadaPeriodoDTO periodo = new MaquinaParadaPeriodoDTO();
				periodo.setAnio(e.getAnio());
				periodo.setMes(e.getMes());
				periodo.setCantidadReclamos(e.getCantidadReclamos());
				periodo.setDiasParadaTotal(e.getDiasParadaTotal());
				estadisticaMaquinaDTO.getPeriodos().add(periodo);
			}

			maquinaPeriodoMap.put(e.getMaquina(), estadisticaMaquinaDTO);

		});

		return maquinaPeriodoMap;

	}

	@Override
	public Map<String, EstadisticaClienteDTO> getEstadisticasCliente(CustomerReclamoStatsRequestDTO dto) {

		ObjectMapper mapper = new ObjectMapper();

		String periodosJson = "";
		String clientesJson = "";

		try {
			periodosJson = mapper.writeValueAsString(dto.getPeriodos());
			clientesJson = mapper.writeValueAsString(dto.getClientes());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		List<ServiceCallCustomerStatsDTO> listado = serviceCallservice.getEstadisticasCliente(clientesJson,
				periodosJson);

		Map<String, EstadisticaClienteDTO> clienteMap = new LinkedHashMap<>();

		listado.forEach(e -> {

			EstadisticaClienteDTO estadistica = clienteMap.get(e.getCliente());

			if (estadistica == null)
				estadistica = new EstadisticaClienteDTO();

			if (Boolean.TRUE.equals(e.getTotalGeneral())) {

				estadistica.setTotal(e.getCantidad());

			} else {

				ClientePeriodoDTO periodo = new ClientePeriodoDTO();

				periodo.setAnio(e.getAnio());
				periodo.setMes(e.getMes());
				periodo.setCantidad(e.getCantidad());

				estadistica.getPeriodos().add(periodo);

			}

			clienteMap.put(e.getCliente(), estadistica);

		});

		return clienteMap;

	}

}
