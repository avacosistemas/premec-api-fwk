package ar.com.avaco.premec.ws.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.avaco.fwk.core.component.epservice.CRUDEPBaseService;
import ar.com.avaco.premec.domain.Cliente;
import ar.com.avaco.premec.dto.ClienteDTO;
import ar.com.avaco.premec.service.ClienteService;

@Service("clienteEPService")
public class ClienteEPServiceImpl extends CRUDEPBaseService<Long, ClienteDTO, Cliente, ClienteService>
		implements ClienteEPService {

	@Resource(name = "clienteService")
	protected void setService(ClienteService service) {
		this.service = service;
	}

	@Override
	protected Cliente convertToEntity(ClienteDTO dto) {
		ObjectMapper mapper = new ObjectMapper();
		Cliente cliente = mapper.convertValue(dto, Cliente.class);
		return cliente;
	}

	@Override
	protected ClienteDTO convertToDto(Cliente entity) {
		ObjectMapper mapper = new ObjectMapper();
		ClienteDTO cliente = mapper.convertValue(entity, ClienteDTO.class);
		return cliente;
	}

}
