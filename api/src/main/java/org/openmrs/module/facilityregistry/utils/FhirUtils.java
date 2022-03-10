/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.facilityregistry.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import ca.uhn.fhir.parser.IParser;
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
import org.openmrs.api.context.Context;
import org.openmrs.module.facilityregistry.FacilityRegistryConstants;

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
	 * Create Fhir Client
	 * 
	 * @param fhirStorePath fhir server Base Url
	 * @return IGenericClient
	 */
	public static IGenericClient getFhirClient(String fhirStorePath) {
		IGenericClient fhirClient = getFhirContext().newRestfulGenericClient(fhirStorePath);
		return fhirClient;
	}
	
	/**
	 * Return FHIR parser
	 * 
	 * @return Parser
	 */
	public static IParser getParser() {
		return getFhirContext().newJsonParser();
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
