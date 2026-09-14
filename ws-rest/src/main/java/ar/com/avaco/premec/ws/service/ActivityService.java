package ar.com.avaco.premec.ws.service;

import java.util.List;

import ar.com.avaco.premec.ws.dto.actividad.HorasPorEmpleadoDTO;
import ar.com.avaco.premec.ws.dto.actividad.RegistroPreviewEmpleadoMensualDTO;
import ar.com.avaco.premec.ws.dto.employee.PlantillaData;

public interface ActivityService {

	List<HorasPorEmpleadoDTO> obtenerHorasAgrupadasPorFechaEmpleado(Long employeeId, String fechaDesde,
			String fechaHasta, String horaDesde, String horaHasta, String exclusionesActividadesCalculoHorasNetas);

	List<HorasPorEmpleadoDTO> obtenerHorasAgrupadasPorFechaEmpleado(List<Long> employeeIds, String fechaDesde,
			String fechaHasta, String exclusionesActividadesCalculoHorasNetas);

	List<RegistroPreviewEmpleadoMensualDTO> obtenerActividadesValoradas(String mes, String anio, String exclusiones);

	PlantillaData getPreviewNovedadesContador(String anio, String mes);


	List<RegistroPreviewEmpleadoMensualDTO> obtenerActividadesValoradasSinAgrupar(String fechaDesde, String fechaHasta,
			String exclusionesActividadesCalculoHorasNetas);

	List<RegistroPreviewEmpleadoMensualDTO> getRegistrosCierre(String fechaDesde, String fechaHasta,
			String exclusionesActividadesCalculoHorasNetas, String usuarioSap);

	List<RegistroPreviewEmpleadoMensualDTO> obtenerIndicadoresPorGrupoEmpleado(String fechaDesde, String fechaHasta,
			String exclusionesActividadesCalculoHorasNetas, List<Long> idUsuariosSap);

	List<RegistroPreviewEmpleadoMensualDTO> obtenerIndicadoresPorEmpleados(String fechaDesde, String fechaHasta,
			String exclusionesActividadesCalculoHorasNetas, List<Long> idUsuariosSap);

}
