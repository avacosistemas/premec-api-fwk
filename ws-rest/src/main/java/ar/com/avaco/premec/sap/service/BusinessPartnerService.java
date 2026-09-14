package ar.com.avaco.premec.sap.service;

import ar.com.avaco.premec.sap.dto.BusinessPartnerResponseDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;

public interface BusinessPartnerService {

	BusinessPartnerResponseDTO getByCUIT(String cuit) throws SapBusinessException;

	void updateEmail(String username, String email) throws SapBusinessException;

}
