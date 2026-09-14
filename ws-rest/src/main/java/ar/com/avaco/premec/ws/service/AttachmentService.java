package ar.com.avaco.premec.ws.service;

import java.util.List;
import java.util.Map;

import ar.com.avaco.premec.ws.dto.attachment.ResponseAttachmentGetPost;

public interface AttachmentService {

	ResponseAttachmentGetPost getAttachment(Long attachmentEntry);

	Long enviarAttachmentsSap(List<Map<String, String>> attachments);

	void update(Long attachmentEntry, Map<String, Object> attPatchMap);

}
