package ar.com.avaco.premec.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "CLIENTE")
@SequenceGenerator(name = "CLIENTE_SEQ", sequenceName = "CLIENTE_SEQ", allocationSize = 1)
public class Cliente extends ar.com.avaco.fwk.core.domain.Entity<Long> {

	private static final long serialVersionUID = 4015134588869901551L;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CLIENTE_SEQ")
	@Column(name = "ID_CLIENTE")
	private Long id;

	@Column(name = "NOMBRE")
	private String nombre;

	@Column(name = "USERNAME")
	private String username;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "REQUIERE_CAMBIO_PASSWORD")
	private boolean requiereCambioPassword;

	@Column(name = "BLOQUEADO")
	private boolean bloqueado;

	@Column(name = "INTENTOS_FALLIDOS_LOGIN")
	private Integer intentosFallidosLogin;

	@Column(name = "EMAIL")
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getIntentosFallidosLogin() {
		return intentosFallidosLogin;
	}

	public void setIntentosFallidosLogin(Integer intentosFallidosLogin) {
		this.intentosFallidosLogin = intentosFallidosLogin;
	}

	public boolean isBloqueado() {
		return bloqueado;
	}

	public void setBloqueado(boolean bloqueado) {
		this.bloqueado = bloqueado;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isRequiereCambioPassword() {
		return requiereCambioPassword;
	}

	public void setRequiereCambioPassword(boolean requiereCambioPassword) {
		this.requiereCambioPassword = requiereCambioPassword;
	}

}
