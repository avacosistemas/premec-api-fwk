package ar.com.avaco.premec.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "PROBLEMA_MAQUINA")
@SequenceGenerator(name = "PROBLEMA_MAQUINA_SEQ", sequenceName = "PROBLEMA_MAQUINA_SEQ", allocationSize = 1)
public class ProblemaMaquina extends ar.com.avaco.fwk.core.domain.Entity<Long> {

	private static final long serialVersionUID = -2565942813218546044L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PROBLEMA_MAQUINA_SEQ")
	@Column(name = "ID_PROBLEMA_MAQUINA")
	private Long id;

	@Column(name = "NOMBRE", nullable = false)
	private String nombre;

	@ManyToOne
	@JoinColumn(name = "ID_TIPO_PROBLEMA_MAQUINA", nullable = false)
	private TipoProblemaMaquina tipoProblemaMaquina;

	public TipoProblemaMaquina getTipoProblemaMaquina() {
		return tipoProblemaMaquina;
	}

	public void setTipoProblemaMaquina(TipoProblemaMaquina tipoProblemaMaquina) {
		this.tipoProblemaMaquina = tipoProblemaMaquina;
	}

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

}
