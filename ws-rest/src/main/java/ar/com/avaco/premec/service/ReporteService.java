package ar.com.avaco.premec.service;

public interface ReporteService {
	
	void sendMail(String email, String activityCode, String body, String subject);

}
