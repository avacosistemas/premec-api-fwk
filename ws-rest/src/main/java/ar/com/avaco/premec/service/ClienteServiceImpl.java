/**
 * 
 */
package ar.com.avaco.premec.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.avaco.fwk.commons.service.mail.MailSenderSMTPService;
import ar.com.avaco.fwk.core.component.service.NJBaseService;
import ar.com.avaco.fwk.core.exception.ErrorValidationException;
import ar.com.avaco.premec.domain.Cliente;
import ar.com.avaco.premec.repository.ClienteRepository;
import ar.com.avaco.premec.sap.dto.BusinessPartnerResponseDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.sap.service.BusinessPartnerService;
import ar.com.avaco.premec.ws.service.SQLServerConnection;

/**
 * @author avaco
 */

@Transactional
@Service("clienteService")
public class ClienteServiceImpl extends NJBaseService<Long, Cliente, ClienteRepository> implements ClienteService {

	@Value("${mail.password.from}")
	private String from;

	@Value("${mail.password.cc}")
	private String cc;
	
	@Value("${reclamos.mail.test}")
	private String mailTest;

	@Value("${email.test}")
	private Boolean test;
	
	@Value("${reclamos.url}")
	private String urlReclamos;
	
	@Value("${nuevo.cliente.asunto}")
	private String subjectNuevoCliente;

	@Value("${nuevo.cliente.cuerpo}")
	private String bodyNuevoCliente;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MailSenderSMTPService mailSenderSMTPService;

	@Autowired
	private BusinessPartnerService bpservice;

	@Autowired
	private SQLServerConnection sqlcon;
	
	@Override
	public Cliente save(Cliente cliente) {
		try {
			// Obtengo el BP
			BusinessPartnerResponseDTO bddto = bpservice.getByCUIT(cliente.getUsername());
			
			// le actualizo el mail
			bpservice.updateEmail(cliente.getUsername(), cliente.getEmail());
			
			// Armo la entidad
			cliente.setNombre(bddto.getCardName());
			cliente.setBloqueado(false);
			cliente.setIntentosFallidosLogin(0);
			cliente.setRequiereCambioPassword(true);
			String tmppass = KeyGenerators.string().generateKey();
			cliente.setPassword(passwordEncoder.encode(tmppass));
			cliente = getRepository().save(cliente);
			if (mailSenderSMTPService != null) {
				notificarPasswordNuevoCliente(cliente, tmppass);
			}
			return cliente;
		} catch (SapBusinessException e) {
			if (e.getSapCode().equals(SapBusinessException.ERRROR_NO_MATCHING_RECORDS_FOUND)) {
				throw new ErrorValidationException("No existe un cliente con CUIT " + cliente.getUsername(), null);
			} else {
				throw new ErrorValidationException(e.getSapMessage(), null);
			}
		}
	}

	@Override
	public List<Cliente> list() {
		List<Cliente> list = super.list();

		Map<String, String> mails = new HashMap<String, String>();
		
		String sqlString = "select U_correoreclamos, CardCode from OCRD where U_correoreclamos is not null";
		try (Connection conn = sqlcon.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sqlString );
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				String cuit = rs.getString("CardCode").replace("C", "");
				String email = rs.getString("U_correoreclamos").trim();
				mails.put(cuit, email);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
		Iterator<Cliente> iter = list.iterator();
		while (iter.hasNext()) {
			Cliente next = iter.next();
			String username = next.getUsername();
			String mail = mails.containsKey(username) ? mails.get(username): "Falta asociar en SAP";
			next.setEmail(mail);
		}
		
		return list;
		
	}
	
private void notificarPasswordNuevoCliente(Cliente cliente, String tmpass) {
		
		String body = bodyNuevoCliente;
		body = body.replaceAll("%cliente%", cliente.getNombre());
		body = body.replaceAll("%usuario%", cliente.getUsername());
		body = body.replaceAll("%temppass%", tmpass);
		body = body.replaceAll("%urlreclamos%", urlReclamos);
		
		String email = cliente.getEmail();
		if (test) {
			email = mailTest;
		}
		mailSenderSMTPService.sendMail(from, email, cc, subjectNuevoCliente, body, null);
	}

	@Resource(name = "clienteRepository")
	public void setRepository(ClienteRepository clienteRepository) {
		repository = clienteRepository;
	}
}
