package ar.com.avaco.premec.factory;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
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

	private static final int MAX_RETRIES = 3;

	private Date expiration;

	private RestTemplateFactory restTemplateFactory;

	private final ObjectMapper objectMapper = new ObjectMapper();

	public RestTemplatePremec(HttpComponentsClientHttpRequestFactory httpRequestFactory) {

		super(httpRequestFactory);
	}

	public void setRestTemplateFactory(RestTemplateFactory restTemplateFactory) {

		this.restTemplateFactory = restTemplateFactory;
	}

	public <T> ResponseEntity<T> doExchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
			ParameterizedTypeReference<T> responseType, Object... uriVariables) throws SapBusinessException {

		return executeWithRetry(url,
				template -> template.exchange(url, method, requestEntity, responseType, uriVariables));
	}

	public <T> ResponseEntity<T> doExchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
			Class<T> responseType, Object... uriVariables) throws SapBusinessException {

		return executeWithRetry(url,
				template -> template.exchange(url, method, requestEntity, responseType, uriVariables));
	}

	private <T> ResponseEntity<T> executeWithRetry(String url,
			Function<RestTemplatePremec, ResponseEntity<T>> operation) throws SapBusinessException {

		int tries = 0;

		RestTemplatePremec currentTemplate = this;

		while (tries < MAX_RETRIES) {

			try {

				return operation.apply(currentTemplate);

			} catch (ResourceAccessException e) {

				tries++;

				if (tries >= MAX_RETRIES) {

					throw new SapBusinessException("Error de conexión con SAP", e);
				}

				currentTemplate = reconnect();

			} catch (RestClientResponseException e) {

				int statusCode = e.getRawStatusCode();

				if (statusCode == 401) {

					tries++;

					if (tries >= MAX_RETRIES) {

						throw buildSapException(e, url);
					}

					currentTemplate = reconnect();

				} else {

					throw buildSapException(e, url);
				}

			} catch (RestClientException e) {

				throw new SapBusinessException("Error técnico llamando a SAP", e);
			}
		}

		throw new SapBusinessException("No fue posible comunicarse con SAP", null);
	}

	private RestTemplatePremec reconnect() throws SapBusinessException {

		if (restTemplateFactory == null) {

			throw new SapBusinessException("No se encuentra configurado el RestTemplateFactory", null);
		}

		RestTemplatePremec nuevoTemplate = restTemplateFactory.reconnect();

		if (nuevoTemplate == null) {

			throw new SapBusinessException("No fue posible establecer una nueva sesión con SAP", null);
		}

		return nuevoTemplate;
	}

	private SapBusinessException buildSapException(RestClientResponseException e, String url) {

		String responseBody = e.getResponseBodyAsString();

		int statusCode = e.getRawStatusCode();

		if (responseBody != null && !responseBody.isEmpty()) {

			try {

				SapErrorResponseDTO sapError = objectMapper.readValue(responseBody, SapErrorResponseDTO.class);

				if (sapError != null && sapError.getError() != null) {

					return new SapBusinessException(sapError, statusCode);
				}

			} catch (Exception ignored) {
				// Fallback
			}
		}

		return new SapBusinessException("Error SAP Service Layer. HTTP " + statusCode + " - " + url, e);
	}

	public void addSessionExpiration(Integer minutes) {

		minutes = minutes - 5;

		Calendar instance = Calendar.getInstance();

		instance.add(Calendar.MINUTE, minutes);

		expiration = instance.getTime();
	}

	public boolean isSessionActive() {

		return expiration != null && Calendar.getInstance().getTime().before(expiration);
	}
}
