package org.openmrs.module.facilityregistry.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;

public class FhirUtils {
	
	/**
	 * Create Fhir Client with Bearer Authentication
	 * 
	 * @param fhirStorePath fhir server Base Url
	 * @param token generated Token from the Facility Registry Server
	 * @return IGenericClient
	 */
	public static IGenericClient getFhirClient(String fhirStorePath, String token) {
		IGenericClient fhirClient = getFhirContext().newRestfulGenericClient(fhirStorePath);
		BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);
		fhirClient.registerInterceptor(authInterceptor);
		return fhirClient;
	}
	
	/**
	 * Generate Bearer Authentication Token
	 * 
	 * @param authUrl url for generating the acces Bearer token
	 * @param authUserName login user name for the Facility Registry Server
	 * @param authPassword login password for the Facility Registry Server
	 * @return String Token
	 */
	public static String getAccesToken(String authUrl, String authUserName, String authPassword) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(authUrl);
		
		String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", authUserName, authPassword);
		StringEntity entity = new StringEntity(json);
		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode response = mapper.createObjectNode();
		try (CloseableHttpResponse res = client.execute(httpPost)) {
			if (res.getStatusLine().getStatusCode() == 200) {
				response = mapper.readTree(EntityUtils.toString(res.getEntity(), StandardCharsets.UTF_8));
			}
		}
		return response.get("access_token").asText();
	}
	
	public static FhirContext getFhirContext() {
		return FhirContext.forR4();
	}
}
