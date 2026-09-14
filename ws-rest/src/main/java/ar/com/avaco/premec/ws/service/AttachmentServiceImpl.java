package ar.com.avaco.premec.ws.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ar.com.avaco.fwk.core.exception.ErrorValidationException;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.ws.dto.attachment.ResponseAttachmentGetPost;

@Service("attachmentService")
public class AttachmentServiceImpl extends AbstractSapService implements AttachmentService {

	@Override
	public ResponseAttachmentGetPost getAttachment(Long attachmentEntry) {
		String attachmentGetUrl = urlSAP + "/Attachments2(" + attachmentEntry + ")";

		ResponseEntity<ResponseAttachmentGetPost> currentAttachmentResponse;
		try {
			currentAttachmentResponse = getRestTemplate().doExchange(attachmentGetUrl, HttpMethod.GET, null,
					ResponseAttachmentGetPost.class);
		} catch (SapBusinessException e) {
			Map<String, String> errors = new HashMap<String, String>();
			errors.put("url", attachmentGetUrl);
			errors.put("error", e.getMessage());
			e.printStackTrace();
			throw new ErrorValidationException("Error al ejecutar el siguiente WS", errors);
		}

		ResponseAttachmentGetPost currentAttach = currentAttachmentResponse.getBody();
		return currentAttach;
	}

	@Override
	public Long enviarAttachmentsSap(List<Map<String, String>> attachments) {
		Map<String, Object> attachmentMap = new HashMap<>();
		attachmentMap.put("Attachments2_Lines", attachments.toArray());

		// Preparo la url para enviar el attachment
		String attachmentUrl = urlSAP + "/Attachments2";
		HttpEntity<Map<String, Object>> httpEntityAttach = new HttpEntity<>(attachmentMap);
		ResponseEntity<ResponseAttachmentGetPost> attachmentRespose = null;

		try {
			attachmentRespose = getRestTemplate().doExchange(attachmentUrl, HttpMethod.POST, httpEntityAttach,
					ResponseAttachmentGetPost.class);
		} catch (SapBusinessException e) {
			Map<String, String> errors = new HashMap<String, String>();
			errors.put("url", attachmentUrl);
			errors.put("error", e.getMessage());
			e.printStackTrace();
			throw new ErrorValidationException("Error al ejecutar el siguiente WS", errors);
		}

		return attachmentRespose.getBody().getAbsoluteEntry();

	}

	@Override
	public void update(Long attachmentEntry, Map<String, Object> attPatchMap) {
		String attachmentUrl = urlSAP + "/Attachments2({attachmentEntry})";
		attachmentUrl = attachmentUrl.replace("{attachmentEntry}", attachmentEntry.toString());
		HttpEntity<Map<String, Object>> httpEntityAttach = new HttpEntity<>(attPatchMap);
		try {
			getRestTemplate().doExchange(attachmentUrl, HttpMethod.PATCH, httpEntityAttach,
					ResponseAttachmentGetPost.class);
		} catch (SapBusinessException e) {
			Map<String, String> errors = new HashMap<String, String>();
			errors.put("url", attachmentUrl);
			errors.put("error", e.getMessage());
			e.printStackTrace();
			throw new ErrorValidationException("Error al ejecutar el siguiente WS", errors);
		}
	}

}
