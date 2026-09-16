package ar.com.avaco.premec.ws.service;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.core.component.epservice.CRUDEPBaseService;
import ar.com.avaco.fwk.security.domain.Acceso;
import ar.com.avaco.fwk.security.domain.Perfil;
import ar.com.avaco.fwk.security.dto.PerfilDTO;
import ar.com.avaco.fwk.security.service.PerfilService;
import ar.com.avaco.premec.domain.UsuarioPremec;
import ar.com.avaco.premec.dto.UsuarioPremecDTO;
import ar.com.avaco.premec.service.UsuarioPremecService;

@Service("usuarioPremecEPService")
public class UsuarioPremecEPServiceImpl
		extends CRUDEPBaseService<Long, UsuarioPremecDTO, UsuarioPremec, UsuarioPremecService>
		implements UsuarioPremecEPService {

	@Autowired
	private PerfilService perfilService;
	
	@Override
	protected UsuarioPremec convertToEntity(UsuarioPremecDTO dto) {
		UsuarioPremec entity = new UsuarioPremec();
		entity.setApellido(dto.getLastname());
		entity.setEmail(dto.getEmail());
		entity.setNombre(dto.getName());
		entity.setUsername(dto.getUsername());
		entity.setBloqueado(!dto.isEnabled());
		entity.setAdmin(dto.getAdmin());
		entity.setUsuariosap(dto.getUsuariosap());
		entity.setDeposito(dto.getDeposito());
		entity.setLegajo(dto.getLegajo());
		if (dto.getProfiles() != null) {
			Set<Acceso> accesos = new HashSet<>();
			dto.getProfiles().stream().forEach(e -> {
				Acceso acceso = new Acceso();
				Perfil p = perfilService.get(e.getId());
				acceso.setPerfil(p);
				acceso.setUsuario(entity);
				accesos.add(acceso);
			});
			entity.setAccesos(accesos);
		}
		return entity;
	}

	@Override
	protected UsuarioPremecDTO convertToDto(UsuarioPremec usuario) {
		Set<PerfilDTO> profiles = new HashSet<>();
		if (usuario.getAccesos() != null) {
			usuario.getAccesos().stream().forEach(e -> {
				PerfilDTO pdto = new PerfilDTO();
				pdto.setId(e.getPerfil().getId());
				profiles.add(pdto);
			});
		}

		Long legajoFichaje = null;
		if (usuario.getLegajo() != null)
			legajoFichaje = usuario.getLegajo();
		return new UsuarioPremecDTO(usuario.getId(), usuario.getUsername(), usuario.getNombre(), usuario.getApellido(),
				profiles, usuario.getEmail(), usuario.getUsuariosap(), usuario.isEnabled(), usuario.getAdmin(),
				usuario.getDeposito(), legajoFichaje);
	}

	@Override
	@Resource(name = "usuarioPremecService")
	protected void setService(UsuarioPremecService service) {
		this.service = service;
	}

	@Override
	public UsuarioPremecDTO findByUsername(String name) {
		UsuarioPremec usuario = this.service.findByUsername(name);
		return convertToDto(usuario);
	}

}
