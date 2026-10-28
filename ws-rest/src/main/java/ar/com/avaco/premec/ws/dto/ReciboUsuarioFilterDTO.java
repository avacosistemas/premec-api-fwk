package ar.com.avaco.premec.ws.dto;

public class ReciboUsuarioFilterDTO {

	private Integer page;
	private Integer pageSize;
	private Integer year;
	private Integer month;
	private String tipo;
	private String attachmentEntry;

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getAttachmentEntry() {
		return attachmentEntry;
	}

	public void setAttachmentEntry(String attachmentEntry) {
		this.attachmentEntry = attachmentEntry;
	}
}
