package ar.com.avaco.premec.sap.error;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SapErrorDTO {

	@JsonProperty("code")
	private String code;

	@JsonProperty("message")
	private String message;

	@JsonProperty("details")
	private List<SapErrorDetailDTO> details;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<SapErrorDetailDTO> getDetails() {
		return details;
	}

	public void setDetails(List<SapErrorDetailDTO> details) {
		this.details = details;
	}
}
