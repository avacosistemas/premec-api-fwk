package ar.com.avaco.premec.ws.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.core.component.epservice.CRUDEPBaseService;
import ar.com.avaco.premec.domain.GrupoEmpleado;
import ar.com.avaco.premec.domain.UsuarioPremec;
import ar.com.avaco.premec.dto.GrupoEmpleadoDTO;
import ar.com.avaco.premec.dto.UsuarioEmpleadoDTO;
import ar.com.avaco.premec.service.GrupoEmpleadoService;
import ar.com.avaco.premec.service.UsuarioPremecService;

@Service("grupoEmpleadoEPService")
public class GrupoEmpleadoEPServiceImpl
		extends CRUDEPBaseService<Long, GrupoEmpleadoDTO, GrupoEmpleado, GrupoEmpleadoService>
		implements GrupoEmpleadoEPService {

	@Autowired
	private UsuarioPremecService usuarioPremecService;

	@Override
	public List<UsuarioEmpleadoDTO> listUsuarios() {
		List<UsuarioPremec> usrs = this.usuarioPremecService.list();
		List<UsuarioEmpleadoDTO> usrdtolist = new ArrayList<UsuarioEmpleadoDTO>();
		usrs.stream().forEach(x -> usrdtolist.add(new UsuarioEmpleadoDTO(x.getId(), x.getNombreApellido())));
		usrdtolist.sort(new Comparator<UsuarioEmpleadoDTO>() {
			@Override
			public int compare(UsuarioEmpleadoDTO o1, UsuarioEmpleadoDTO o2) {
				return o1.getUsuario().compareTo(o2.getUsuario());
			}
		});
		return usrdtolist;
	}

	@Override
	@Resource(name = "grupoEmpleadoService")
	protected void setService(GrupoEmpleadoService service) {
		this.service = service;
	}

	@Override
	protected GrupoEmpleado convertToEntity(GrupoEmpleadoDTO dto) {
		GrupoEmpleado ge = new GrupoEmpleado();
		ge.setId(dto.getId());
		ge.setNombre(dto.getNombre());
		dto.getUsuarios().stream().forEach(usr -> ge.getUsuarios().add(usuarioPremecService.get(usr.getId())));
		return ge;
	}

	@Override
	protected GrupoEmpleadoDTO convertToDto(GrupoEmpleado entity) {
		GrupoEmpleadoDTO dto = new GrupoEmpleadoDTO();
		dto.setId(entity.getId());
		dto.setNombre(entity.getNombre());
		entity.getUsuarios().stream()
				.forEach(usr -> dto.getUsuarios().add(new UsuarioEmpleadoDTO(usr.getId(), usr.getNombreApellidoUsername())));
		return dto;
	}

}
