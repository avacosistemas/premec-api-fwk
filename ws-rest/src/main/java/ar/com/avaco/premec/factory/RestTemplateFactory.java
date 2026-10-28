package ar.com.avaco.premec.factory;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;

import ar.com.avaco.premec.dto.PostLoginSAPDTO;
import ar.com.avaco.premec.dto.ResponseLoginSAPDTO;
import ar.com.avaco.premec.sap.exception.SapBusinessException;
import ar.com.avaco.premec.sap.exception.SapResponseErrorHandler;

public class RestTemplateFactory {

	private static final int MAX_LOGIN_RETRIES = 3;

	private static final int CONNECT_TIMEOUT = 5000;
	private static final int CONNECTION_REQUEST_TIMEOUT = 5000;
	private static final int READ_TIMEOUT = 5000;

	private static final int MAX_TOTAL_CONNECTIONS = 50;
	private static final int MAX_CONNECTIONS_PER_ROUTE = 20;

	private static final int CONNECTION_TTL_SECONDS = 60;

	private String urlSAP;
	private String user;
	private String pass;
	private String db;

	private RestTemplatePremec restTemplate;

	public RestTemplateFactory(String urlSAP, String user, String pass, String db, RestTemplatePremec restTemplate) {

		this.urlSAP = urlSAP;
		this.user = user;
		this.pass = pass;
		this.db = db;
		this.restTemplate = restTemplate;

		if (this.restTemplate != null) {
			this.restTemplate.setRestTemplateFactory(this);
		}
	}

	public synchronized RestTemplatePremec get() throws SapBusinessException {

		if (this.restTemplate != null && this.restTemplate.isSessionActive()) {

			return this.restTemplate;
		}

		this.restTemplate = null;

		SapBusinessException lastException = null;

		for (int attempt = 1; attempt <= MAX_LOGIN_RETRIES; attempt++) {

			try {

				this.restTemplate = getLoggedRestTemplate();

				return this.restTemplate;

			} catch (SapBusinessException e) {

				lastException = e;

			} catch (Exception e) {

				lastException = new SapBusinessException("Error al establecer la sesión con SAP", e);
			}
		}

		if (lastException != null) {

			throw new SapBusinessException(
					"No fue posible iniciar sesión en SAP después de " + MAX_LOGIN_RETRIES + " intentos",
					lastException);
		}

		throw new SapBusinessException("No fue posible iniciar sesión en SAP", null);
	}

	/**
	 * Fuerza una nueva sesión contra SAP.
	 */
	public synchronized RestTemplatePremec reconnect() throws SapBusinessException {

		this.restTemplate = null;

		return get();
	}

	private RestTemplatePremec getLoggedRestTemplate() throws SapBusinessException, Exception {

		HttpComponentsClientHttpRequestFactory httpRequestFactory = createHttpRequestFactory();

		RestTemplatePremec restTemplate = new RestTemplatePremec(httpRequestFactory);

		restTemplate.setRestTemplateFactory(this);

		restTemplate.setErrorHandler(new SapResponseErrorHandler());

		/*
		 * Headers utilizados solamente para el Login.
		 */
		HttpHeaders loginHeaders = new HttpHeaders();

		loginHeaders.set("Content-Type", "application/json");

		PostLoginSAPDTO dto = new PostLoginSAPDTO();

		dto.setCompanyDB(db);
		dto.setPassword(pass);
		dto.setUserName(user);

		String urlLogin = urlSAP + "/Login";

		HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(dto.getAsMap(), loginHeaders);

		try {

			ResponseEntity<ResponseLoginSAPDTO> response = restTemplate.exchange(urlLogin, HttpMethod.POST, httpEntity,
					ResponseLoginSAPDTO.class);

			ResponseLoginSAPDTO body = response.getBody();

			if (body == null || body.getSessionId() == null) {

				throw new SapBusinessException("SAP no devolvió una sesión válida", null);
			}

			/*
			 * Headers que deben enviarse en las llamadas posteriores al Login.
			 */
			HttpHeaders defaultHeaders = new HttpHeaders();

			defaultHeaders.set("Content-Type", "application/json");

			defaultHeaders.set("Cookie", "B1SESSION=" + body.getSessionId() + "; ROUTEID=.node7");

			defaultHeaders.setCacheControl("no-cache");

			defaultHeaders.set("Prefer", "odata.maxpagesize=0");

			ClientHttpRequestInterceptor interceptor = (request, requestBody, execution) -> {
				boolean monitorQuery = request.getURI().getRawPath()
						.contains("$crossjoin(Activities,EmployeesInfo,ServiceCalls)");
				String monitorPrefer = monitorQuery ? request.getHeaders().getFirst("Prefer") : null;

				request.getHeaders().putAll(defaultHeaders);

				if (monitorPrefer != null) {
					request.getHeaders().set("Prefer", monitorPrefer);
				}

				return execution.execute(request, requestBody);
			};

			restTemplate.getInterceptors().add(interceptor);

			if (body.getSessionTimeout() != null) {

				restTemplate.addSessionExpiration(body.getSessionTimeout().intValue());
			}

			return restTemplate;

		} catch (RestClientException rce) {

			throw new SapBusinessException(rce);
		}
	}

	/**
	 * Crea el cliente HTTP con SSL y configuración de conexiones para SAP Service
	 * Layer.
	 */
	private HttpComponentsClientHttpRequestFactory createHttpRequestFactory() throws Exception {

		SSLContext sslContext = createSslContext();

		CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext)
				.setMaxConnTotal(MAX_TOTAL_CONNECTIONS).setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
				.evictExpiredConnections().evictIdleConnections(CONNECTION_TTL_SECONDS, TimeUnit.SECONDS).build();

		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

		factory.setConnectTimeout(CONNECT_TIMEOUT);

		factory.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT);

		factory.setReadTimeout(READ_TIMEOUT);

		return factory;
	}

	/**
	 * Construye el SSLContext utilizando myCA.cer como certificado de confianza.
	 */
	private SSLContext createSslContext() throws Exception {

		ClassLoader classLoader = getClass().getClassLoader();

		try (InputStream certInputStream = classLoader.getResourceAsStream("myCA.cer")) {

			if (certInputStream == null) {

				throw new SapBusinessException("No se encontró el certificado myCA.cer", null);
			}

			X509Certificate certificado = (X509Certificate) CertificateFactory.getInstance("X.509")
					.generateCertificate(certInputStream);

			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

			keyStore.load(null, null);

			keyStore.setCertificateEntry("cert", certificado);

			TrustManagerFactory trustManagerFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());

			trustManagerFactory.init(keyStore);

			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

			sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

			return sslContext;
		}
	}
}
