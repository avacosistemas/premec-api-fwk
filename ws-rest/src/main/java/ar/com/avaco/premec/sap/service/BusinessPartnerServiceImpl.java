package ar.com.avaco.premec.sap.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ar.com.avaco.premec.sap.dto.BusinessPartnerResponseDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.ws.service.AbstractSapService;

@Service("businessPartnerService")
public class BusinessPartnerServiceImpl extends AbstractSapService implements BusinessPartnerService {

	@Override
	public BusinessPartnerResponseDTO getByCUIT(String cuit) throws SapBusinessException {
		String bpUrl = urlSAP + "/BusinessPartners('C{cuit}')".replace("{cuit}", cuit);
		ResponseEntity<BusinessPartnerResponseDTO> bpresponse = null;
		bpresponse = getRestTemplate().doExchange(bpUrl, HttpMethod.GET, null, BusinessPartnerResponseDTO.class);
		BusinessPartnerResponseDTO registro = bpresponse.getBody();
		return registro;
	}

	@Override
	public void updateEmail(String cuit, String email) throws SapBusinessException {
		String bpUrl = urlSAP + "/BusinessPartners('C{cuit}')".replace("{cuit}", cuit);
		Map<String, Object> map = new HashMap<>();
		map.put("U_correoreclamos", email);
		HttpEntity<Map<String, Object>> httpEntityPatchBP = new HttpEntity<>(map);
		getRestTemplate().doExchange(bpUrl, HttpMethod.PATCH, httpEntityPatchBP, Object.class);
	}

}
