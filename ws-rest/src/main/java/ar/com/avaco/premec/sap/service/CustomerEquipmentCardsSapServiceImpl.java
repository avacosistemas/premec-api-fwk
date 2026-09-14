package ar.com.avaco.premec.sap.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.core.utils.DateUtils;
import ar.com.avaco.premec.sap.DTOList;
import ar.com.avaco.premec.sap.dto.CustomerEquipmentCardsItemListDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.ws.service.AbstractSapService;
import ar.com.avaco.premec.ws.service.SQLServerConnection;

@Service("customerEquipmentCardsSapService")
public class CustomerEquipmentCardsSapServiceImpl extends AbstractSapService
		implements CustomerEquipmentCardsSapService {

	@Autowired
	private SQLServerConnection sqlcon;

	@Override
	public List<CustomerEquipmentCardsItemListDTO> listByCustomer(String cuit, String maquina) throws SapBusinessException {

		StringBuilder bpUrl = new StringBuilder();
		bpUrl.append(urlSAP);
		bpUrl.append("/CustomerEquipmentCards?");
		bpUrl.append("$select=InternalSerialNum,ItemCode,ManufacturerSerialNum,EquipmentCardNum");
		bpUrl.append(
				"&$expand=Item($select=Properties11,Properties12,Properties13,Properties14,Properties8,Properties9,Properties40)");
		bpUrl.append("&$filter=CustomerCode eq 'C{cuit}' and not contains(ItemCode,'PIEZASAREPARAR')");
		bpUrl.append(" and StatusOfSerialNumber eq 'sns_Active'");

		String url = bpUrl.toString().replace("{cuit}", cuit);

		ParameterizedTypeReference<DTOList<CustomerEquipmentCardsItemListDTO>> ptr = new ParameterizedTypeReference<DTOList<CustomerEquipmentCardsItemListDTO>>() {
		};

		List<CustomerEquipmentCardsItemListDTO> result = new ArrayList<>();

		String nextUrl = url;

		while (nextUrl != null) {

			ResponseEntity<DTOList<CustomerEquipmentCardsItemListDTO>> response = getRestTemplate().doExchange(nextUrl,
					HttpMethod.GET, null, ptr);

			nextUrl = null;

			if (response != null) {

				DTOList<CustomerEquipmentCardsItemListDTO> body = response.getBody();

				if (body == null || body.getValue() == null)
					break;

				result.addAll(body.getValue());

				if (body.getNextLink() != null)
					nextUrl = URLDecoder.decode(urlSAP + "/" + body.getNextLink(), StandardCharsets.UTF_8);

			}
		}

		Map<Object, CustomerEquipmentCardsItemListDTO> mapa = new HashMap<Object, CustomerEquipmentCardsItemListDTO>();

		Iterator<CustomerEquipmentCardsItemListDTO> iter = result.iterator();

		while (iter.hasNext()) {
			CustomerEquipmentCardsItemListDTO next = iter.next();
			if ("tYES".equals(next.getItem().getProperties8()) || "tYES".equals(next.getItem().getProperties9())
					|| "tYES".equals(next.getItem().getProperties40())) {
				iter.remove();
			} else if (next.getInternalSerialNum().toUpperCase().contains(maquina.toUpperCase())) {
				next.setLabel(next.getInternalSerialNum() + " - " + next.getItemCode());
				mapa.put(next.getEquipmentCardNum(), next);
			}
		}
		
		result = new ArrayList(mapa.values());
		
		result = result.stream().sorted(new Comparator<CustomerEquipmentCardsItemListDTO>() {
			@Override
			public int compare(CustomerEquipmentCardsItemListDTO o1, CustomerEquipmentCardsItemListDTO o2) {
				return o1.getEquipmentCardNum().compareTo(o2.getEquipmentCardNum());
			}
		}).collect(Collectors.toList());

		

		return result;
	}

	@Override
	public Boolean valiteByCustomerMachine(String cstmrcode, String internalSerialNum) {

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * ").append("FROM OCTR c ").append("INNER JOIN CTR1 s ON s.ContractID = c.ContractID ")
				.append("WHERE c.cstmrcode = 'C:cstmrcode' ").append("AND s.InternalSN = ':internalSN' ")
				.append("AND c.Status = 'A' ") // Approved
				.append("AND c.StartDate <= ':startDate' ").append("AND c.EndDate >= ':endDate' ");

		String query = sb.toString();

		String now = DateUtils.toString(DateUtils.getFechaYHoraActual(), DateUtils.PATTERN_yyyyMMdd);

		query = query.replace(":cstmrcode", cstmrcode).replace(":internalSN", internalSerialNum)
				.replace(":startDate", now).replace(":endDate", now);

		Boolean valid = false;

		try (Connection conn = sqlcon.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {

			valid = rs.next();

			sqlcon.getConnection().close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return valid;
	}
}
