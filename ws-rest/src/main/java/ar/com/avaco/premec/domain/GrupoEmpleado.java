package ar.com.avaco.premec.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import ar.com.avaco.fwk.security.domain.Usuario;

@Entity
@Table(name = "GRUPO_EMPLEADO")
@SequenceGenerator(name = "GRUPO_EMPLEADO_SEQ", sequenceName = "GRUPO_EMPLEADO_SEQ", allocationSize = 1)
public class GrupoEmpleado extends ar.com.avaco.fwk.core.domain.Entity<Long> {

	private static final long serialVersionUID = 4015134588869901551L;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GRUPO_EMPLEADO_SEQ")
	@Column(name = "ID_GRUPO_EMPLEADO")
	private Long id;

	@Column(name = "NOMBRE")
	private String nombre;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "GRUPO_EMPLEADO_USUARIO", joinColumns = @JoinColumn(name = "ID_GRUPO_EMPLEADO", referencedColumnName = "ID_GRUPO_EMPLEADO"), inverseJoinColumns = @JoinColumn(name = "ID_SEG_USUARIO", referencedColumnName = "ID_SEG_USUARIO"))
	@Fetch(FetchMode.SELECT)
	private Set<Usuario> usuarios = new HashSet<>();

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<Usuario> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(Set<Usuario> usuarios) {
		this.usuarios = usuarios;
	}

}
