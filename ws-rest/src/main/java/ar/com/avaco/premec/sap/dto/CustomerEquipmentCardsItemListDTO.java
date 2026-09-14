package ar.com.avaco.premec.sap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ar.com.avaco.premec.domain.TipoMaquina;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerEquipmentCardsItemListDTO {

	@JsonProperty("InternalSerialNum")
	private String internalSerialNum;

	@JsonProperty("ItemCode")
	private String itemCode;

	@JsonProperty("ManufacturerSerialNum")
	private String manufacturerSerialNum;

	@JsonProperty("EquipmentCardNum")
	private Integer equipmentCardNum;

	@JsonProperty("Item")
	private Item item;

	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getInternalSerialNum() {
		return internalSerialNum;
	}

	public void setInternalSerialNum(String internalSerialNum) {
		this.internalSerialNum = internalSerialNum;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getManufacturerSerialNum() {
		return manufacturerSerialNum;
	}

	public void setManufacturerSerialNum(String manufacturerSerialNum) {
		this.manufacturerSerialNum = manufacturerSerialNum;
	}

	public Integer getEquipmentCardNum() {
		return equipmentCardNum;
	}

	public void setEquipmentCardNum(Integer equipmentCardNum) {
		this.equipmentCardNum = equipmentCardNum;
	}

	public String getTipo() {
		if (item.getProperties11().equals("tYES") || item.getProperties12().equals("tYES")
				|| item.getProperties13().equals("tYES")) {
			return TipoMaquina.COMBUSTION.name();
		} else if (item.getProperties14().equals("tYES")) {
			return TipoMaquina.ELECTRICA.name();
		}
		return "SIN_TIPO";
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

}
