package ar.com.avaco.premec.sap.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ar.com.avaco.premec.sap.dto.ItemCodeDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.ws.service.AbstractSapService;

@Service("itemCodeService")
public class ItemCodeServiceImpl extends AbstractSapService implements ItemCodeService {

	@Override
	public ItemCodeDTO getByItemCode(String itemCode) throws SapBusinessException {
		String itemUrl = urlSAP + "/Items?$filter=ItemCode eq '{itemCode}'".replace("{itemCode}", itemCode);
		ResponseEntity<String> response = getRestTemplate().doExchange(itemUrl, HttpMethod.GET, null, String.class);

		JsonObject body = gson.fromJson(response.getBody(), JsonObject.class);
		JsonArray items = body.getAsJsonArray("value");	
		if (items == null || items.size() == 0) {
			return null;
		}

		return gson.fromJson(items.get(0), ItemCodeDTO.class);
	}

}
