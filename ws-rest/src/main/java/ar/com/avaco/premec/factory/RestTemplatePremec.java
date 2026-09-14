package ar.com.avaco.premec.factory;

import java.util.Calendar;
import java.util.Date;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.avaco.premec.sap.error.SapErrorResponseDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;

public class RestTemplatePremec extends RestTemplate {

	private Date expiration;
	private HttpHeaders defaultHeaders;

	private final ObjectMapper objectMapper = new ObjectMapper();

	public RestTemplatePremec(HttpComponentsClientHttpRequestFactory httpRequestFactory) {
		super(httpRequestFactory);
	}

	/*
	 * =============================== ParameterizedTypeReference
	 * ===============================
	 */
	public <T> ResponseEntity<T> doExchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
			ParameterizedTypeReference<T> responseType, Object... uriVariables) throws SapBusinessException {

		int tries = 1;

		do {
			try {
				return super.exchange(url, method, requestEntity, responseType, uriVariables);
			} catch (ResourceAccessException e) {
				if (tries == 3) {
					throw new SapBusinessException("Error de conexión con SAP", e);
				}
				tries++;
			} catch (RestClientResponseException e) {
				throw buildSapException(e, url);
			} catch (RestClientException e) {
				throw new SapBusinessException("Error técnico llamando a SAP", e);
			}
		} while (tries < 3);

		return null;
	}

	/*
	 * =============================== Class<T> ===============================
	 */
	public <T> ResponseEntity<T> doExchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
			Class<T> responseType, Object... uriVariables) throws SapBusinessException {
		int tries = 1;
		do {
			try {
				return super.exchange(url, method, requestEntity, responseType, uriVariables);
			} catch (ResourceAccessException e) {
				if (tries == 3) {
					throw new SapBusinessException("Error de conexión con SAP", e);
				}
				tries++;
			} catch (RestClientException e) {
				throw buildSapException(e, url);
			}

		} while (tries < 3);

		return null;
	}

	/*
	 * =============================== SAP Error handling centralizado
	 * ===============================
	 */
	private SapBusinessException buildSapException(RestClientException e, String url) {
	    String responseBody = e.getLocalizedMessage();
	    int statusCode = 500; // Valor por defecto si no hay status code

	    if (responseBody != null && !responseBody.isEmpty()) {
	        try {
	            SapErrorResponseDTO sapError = objectMapper.readValue(responseBody, SapErrorResponseDTO.class);

	            if (sapError != null && sapError.getError() != null) {
	                return new SapBusinessException(sapError, statusCode);
	            }
	        } catch (Exception ignored) {
	            // Error de parseo, continuamos al fallback
	        }
	    }

	    // Fallback si no hay body o falló el parseo
	    return new SapBusinessException("Error SAP Service Layer. HTTP " + statusCode + " - " + url, e);
	}

	/*
	 * =============================== Session helpers
	 * ===============================
	 */
	public void addSessionExpiration(Integer minutes) {
		minutes = minutes - 5;
		Calendar instance = Calendar.getInstance();
		instance.add(Calendar.MINUTE, minutes);
		expiration = instance.getTime();
	}

	public boolean isSessionActive() {
		return expiration != null && Calendar.getInstance().getTime().before(expiration);
	}

	public HttpHeaders getDefaultHeaders() {
		return defaultHeaders;
	}

	public void setDefaultHeaders(HttpHeaders defaultHeaders) {
		this.defaultHeaders = defaultHeaders;
	}
}

//package ar.com.avaco.factory;
//
//import java.util.Calendar;
//import java.util.Date;
//
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.web.client.ResourceAccessException;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;
//
//public class RestTemplatePremec extends RestTemplate {
//
//	private Date expiration;
//
//	private HttpHeaders defaultHeaders;
//
//	public RestTemplatePremec(HttpComponentsClientHttpRequestFactory httpRequestFactory) {
//		super(httpRequestFactory);
//	}
//
//	public <T> ResponseEntity<T> doExchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
//			ParameterizedTypeReference<T> responseType, Object... uriVariables) throws SapBusinessException {
//		int tries = 1;
//		ResponseEntity<T> exchange = null;
//		do {
//			try {
//				exchange = super.exchange(url, method, requestEntity, responseType, uriVariables);
//			} catch (ResourceAccessException e) {
//				if (tries == 3)
//					throw new SapBusinessException(e);
//				tries++;
//			} catch (RestClientException rce) {
//				throw new SapBusinessException(rce);
//			}
//		} while (tries < 3 && exchange == null);
//		return exchange;
//
//	}
//
//	public <T> ResponseEntity<T> doExchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
//			Class<T> responseType, Object... uriVariables) throws SapBusinessException {
//
//		int tries = 1;
//		ResponseEntity<T> exchange = null;
//		do {
//			try {
//				exchange = super.exchange(url, method, requestEntity, responseType, uriVariables);
//			} catch (ResourceAccessException e) {
//				if (tries == 3)
//					throw new SapBusinessException(e);
//				tries++;
//			} catch (RestClientException rce) {
//				throw new SapBusinessException(rce);
//			} catch (Exception e) {
//				throw new SapBusinessException(e);
//			}
//		} while (tries < 3 && exchange == null);
//		return exchange;
//
//	}
//
//	public void addSessionExpiration(Integer minutes) {
//		minutes = minutes - 5;
//		Calendar instance = Calendar.getInstance();
//		instance.add(Calendar.MINUTE, minutes);
//		expiration = instance.getTime();
//
//	}
//
//	public boolean isSessionActive() {
//		return Calendar.getInstance().getTime().before(expiration);
//	}
//
//	public HttpHeaders getDefaultHeaders() {
//		return defaultHeaders;
//	}
//
//	public void setDefaultHeaders(HttpHeaders defaultHeaders) {
//		this.defaultHeaders = defaultHeaders;
//	}
//
//}
