package ar.com.avaco.premec.sap.service;

import ar.com.avaco.premec.sap.dto.ItemCodeDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;

public interface ItemCodeService {

	ItemCodeDTO getByItemCode(String itemCode) throws SapBusinessException;

}
