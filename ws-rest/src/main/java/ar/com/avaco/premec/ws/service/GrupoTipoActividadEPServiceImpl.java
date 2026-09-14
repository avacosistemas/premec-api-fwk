package ar.com.avaco.premec.ws.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.core.component.epservice.CRUDEPBaseService;
import ar.com.avaco.premec.domain.GrupoTipoActividad;
import ar.com.avaco.premec.domain.TipoActividad;
import ar.com.avaco.premec.service.GrupoTipoActividadService;
import ar.com.avaco.premec.ws.dto.formulario.GrupoTipoActividadDTO;
import ar.com.avaco.premec.ws.dto.formulario.ItemChecklistGrupoDTO;

@Service("grupoTipoActividadEPService")
public class GrupoTipoActividadEPServiceImpl
		extends CRUDEPBaseService<Long, GrupoTipoActividadDTO, GrupoTipoActividad, GrupoTipoActividadService>
		implements GrupoTipoActividadEPService {

	@Override
	protected GrupoTipoActividad convertToEntity(GrupoTipoActividadDTO dto) {
		GrupoTipoActividad grupo = new GrupoTipoActividad();
		grupo.setId(dto.getId());
		grupo.setOrden(dto.getOrden());
		grupo.setTipo(TipoActividad.valueOf(dto.getTipo()));
		grupo.setTitulo(dto.getTitulo());
		return grupo;
	}

	@Override
	protected GrupoTipoActividadDTO convertToDto(GrupoTipoActividad entity) {
		GrupoTipoActividadDTO dto = new GrupoTipoActividadDTO();
		dto.setId(entity.getId());
		dto.setOrden(entity.getOrden());
		dto.setTipo(entity.getTipo().name());
		dto.setTitulo(entity.getTitulo());
		dto.setTipoString(entity.getTipo().getNombre());
		dto.setTituloTipo(entity.getTipo().getNombre() + " - " + entity.getTitulo());
		entity.getItems().forEach(x -> {
			ItemChecklistGrupoDTO gdto = new ItemChecklistGrupoDTO();
			gdto.setId(x.getId());
			gdto.setIdGrupo(x.getGrupo().getId());
			gdto.setNombre(x.getNombre());
			gdto.setOrden(x.getOrden());
			dto.getItems().add(gdto);
		});

		return dto;

	}

	@Override
	@Resource(name = "grupoTipoActividadService")
	protected void setService(GrupoTipoActividadService service) {
		this.service = service;
	}

}
