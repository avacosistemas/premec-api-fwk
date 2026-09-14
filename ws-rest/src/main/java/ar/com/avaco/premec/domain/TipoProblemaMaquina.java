package ar.com.avaco.premec.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "TIPO_PROBLEMA_MAQUINA")
@SequenceGenerator(name = "TIPO_PROBLEMA_MAQUINA_SEQ", sequenceName = "TIPO_PROBLEMA_MAQUINA_SEQ", allocationSize = 1)
public class TipoProblemaMaquina extends ar.com.avaco.fwk.core.domain.Entity<Long> {

	private static final long serialVersionUID = 2476967267656599606L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TIPO_PROBLEMA_MAQUINA_SEQ")
	@Column(name = "ID_TIPO_PROBLEMA_MAQUINA")
	private Long id;

	@Column(name = "NOMBRE", nullable = false)
	private String nombre;

	@Column(name = "TIPO_MAQUINA", nullable = false)
	@Enumerated(EnumType.STRING)
	private TipoMaquina tipoMaquina;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public TipoMaquina getTipoMaquina() {
		return tipoMaquina;
	}

	public void setTipoMaquina(TipoMaquina tipoMaquina) {
		this.tipoMaquina = tipoMaquina;
	}

}
