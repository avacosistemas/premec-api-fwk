package ar.com.avaco.premec.ws.dto;

public class ArchivoDTO {

	private byte[] file;

	private String fileName;

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
