package ar.com.avaco.premec.sap.service;

import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ar.com.avaco.premec.sap.dto.ItemCodeDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.ws.service.AbstractSapService;

@Service("itemCodeService")
public class ItemCodeServiceImpl extends AbstractSapService implements ItemCodeService {

	@Override
	public ItemCodeDTO getByItemCode(String itemCode) throws SapBusinessException {
		String itemUrl = urlSAP + "/Items?$filter=ItemCode eq '{itemCode}'".replace("{itemCode}", itemCode);
		ResponseEntity<ItemsResponseDTO> response = getRestTemplate().doExchange(itemUrl, HttpMethod.GET, null,
				ItemsResponseDTO.class);

		ItemsResponseDTO body = response.getBody();
		if (body == null || body.getValue() == null || body.getValue().isEmpty()) {
			return null;
		}

		return body.getValue().get(0);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class ItemsResponseDTO {

		@JsonProperty("value")
		private List<ItemCodeDTO> value;

		public List<ItemCodeDTO> getValue() {
			return value;
		}

		public void setValue(List<ItemCodeDTO> value) {
			this.value = value;
		}
	}

}
