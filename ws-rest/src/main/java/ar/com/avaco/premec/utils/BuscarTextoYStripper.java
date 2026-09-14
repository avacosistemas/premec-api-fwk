package ar.com.avaco.premec.utils;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class BuscarTextoYStripper extends PDFTextStripper {

	private final String textoBuscado;
	private Float yEncontrada;
	private String textoEncontrado;
	
	public BuscarTextoYStripper(String textoBuscado) throws IOException {
		super();
		this.textoBuscado = textoBuscado;
		setSortByPosition(true);
	}

	@Override
	protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
		if (yEncontrada == null && text.toUpperCase().contains(textoBuscado.toUpperCase())) {
			yEncontrada = textPositions.get(0).getY();
			textoEncontrado = text;
		}

		super.writeString(text, textPositions);
	}

	public Float getYEncontrada() {
		return yEncontrada;
	}
	
	public String getTextoEcontrado() {
		return textoEncontrado;
	}
}