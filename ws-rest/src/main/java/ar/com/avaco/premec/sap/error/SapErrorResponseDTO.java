package ar.com.avaco.premec.sap.error;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SapErrorResponseDTO {
	@JsonProperty("error")
	private SapErrorDTO error;

	public SapErrorDTO getError() {
		return error;
	}

	public void setError(SapErrorDTO error) {
		this.error = error;
	}
}
