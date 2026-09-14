package ar.com.avaco.premec.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "REGISTRO_HORAS_MAQUINA_EX_RES")
@SequenceGenerator(name = "REGISTRO_HORAS_MAQUINA_EX_RES_SEQ", sequenceName = "REGISTRO_HORAS_MAQUINA_EX_RES_SEQ", allocationSize = 1)
public class RegistroHorasMaquinaExcedidaReseteo extends ar.com.avaco.fwk.core.domain.Entity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REGISTRO_HORAS_MAQUINA_EX_RES_SEQ")
	@Column(name = "ID_REG_HS_MAQ_EX_RES")
	private Long id;

	@Column(name = "INTERNAL_SERIAL_NUM")
	private String internalSerialNum;

	@Column(name = "SERVICE_CALL_ID")
	private Long serviceCallId;

	@Column(name = "MAX_MENSUAL")
	private Integer maximoMensual;

	@Column(name = "FECHA_ANTERIOR")
	private Date fechaAnterior;

	@Column(name = "HS_MAQ_ANTERIOR")
	private Double hsMaqAnterior;

	@Column(name = "FECHA_ACTUAL")
	private Date fechaActual;

	@Column(name = "HS_MAQ_ACTUAL")
	private Double horasMaquinaActual;

	@Column(name = "PROMEDIO")
	private Double promedio;

	@Column(name = "HORAS_MAQ_EXC")
	private Double horasMaquinaExcedidas;

	@Column(name = "TIPO")
	private String tipo;

	public Double getHorasMaquinaExcedidas() {
		return horasMaquinaExcedidas;
	}

	public void setHorasMaquinaExcedidas(Double horasMaquinaExcedidas) {
		this.horasMaquinaExcedidas = horasMaquinaExcedidas;
	}

	public String getInternalSerialNum() {
		return internalSerialNum;
	}

	public void setInternalSerialNum(String internalSerialNum) {
		this.internalSerialNum = internalSerialNum;
	}

	public Integer getMaximoMensual() {
		return maximoMensual;
	}

	public void setMaximoMensual(Integer maximoMensual) {
		this.maximoMensual = maximoMensual;
	}

	public Long getServiceCallId() {
		return serviceCallId;
	}

	public void setServiceCallId(Long serviceCallId) {
		this.serviceCallId = serviceCallId;
	}

	public Date getFechaAnterior() {
		return fechaAnterior;
	}

	public void setFechaAnterior(Date fechaAnterior) {
		this.fechaAnterior = fechaAnterior;
	}

	public Double getHsMaqAnterior() {
		return hsMaqAnterior;
	}

	public void setHsMaqAnterior(Double hsMaqAnterior) {
		this.hsMaqAnterior = hsMaqAnterior;
	}

	public Date getFechaActual() {
		return fechaActual;
	}

	public void setFechaActual(Date fechaActual) {
		this.fechaActual = fechaActual;
	}

	public Double getHorasMaquinaActual() {
		return horasMaquinaActual;
	}

	public void setHorasMaquinaActual(Double horasMaquinaActual) {
		this.horasMaquinaActual = horasMaquinaActual;
	}

	public Double getPromedio() {
		return promedio;
	}

	public void setPromedio(Double promedio) {
		this.promedio = promedio;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
