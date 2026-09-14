package ar.com.avaco.premec.ws.dto.formulario;

public class CheckListItemDTO {

	private String titulo;

	public CheckListItemDTO() {
	}

	public CheckListItemDTO(String titulo) {
		super();
		this.titulo = titulo;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

}
