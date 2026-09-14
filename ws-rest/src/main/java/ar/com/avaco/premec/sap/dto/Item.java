package ar.com.avaco.premec.sap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {

	@JsonProperty(value = "Properties8")
	private String properties8;

	@JsonProperty(value = "Properties9")
	private String properties9;

	@JsonProperty(value = "Properties11")
	private String properties11;

	@JsonProperty(value = "Properties12")
	private String properties12;

	@JsonProperty(value = "Properties13")
	private String properties13;

	@JsonProperty(value = "Properties14")
	private String properties14;

	@JsonProperty(value = "Properties40")
	private String properties40;

	public String getProperties11() {
		return properties11;
	}

	public void setProperties11(String properties11) {
		this.properties11 = properties11;
	}

	public String getProperties12() {
		return properties12;
	}

	public void setProperties12(String properties12) {
		this.properties12 = properties12;
	}

	public String getProperties13() {
		return properties13;
	}

	public void setProperties13(String properties13) {
		this.properties13 = properties13;
	}

	public String getProperties14() {
		return properties14;
	}

	public void setProperties14(String properties14) {
		this.properties14 = properties14;
	}

	public String getProperties8() {
		return properties8;
	}

	public void setProperties8(String properties8) {
		this.properties8 = properties8;
	}

	public String getProperties9() {
		return properties9;
	}

	public void setProperties9(String properties9) {
		this.properties9 = properties9;
	}

	public String getProperties40() {
		return properties40;
	}

	public void setProperties40(String properties40) {
		this.properties40 = properties40;
	}

}
