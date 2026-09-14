package ar.com.avaco.premec.sap.service;

import java.util.List;

import ar.com.avaco.premec.sap.dto.CustomerEquipmentCardsItemListDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;

public interface CustomerEquipmentCardsSapService {

	List<CustomerEquipmentCardsItemListDTO> listByCustomer(String cuit, String name) throws SapBusinessException;

	Boolean valiteByCustomerMachine(String name, String internalSerialNum);

}
