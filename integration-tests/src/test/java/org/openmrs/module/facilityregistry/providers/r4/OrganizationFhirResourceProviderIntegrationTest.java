/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.facilityregistry.providers.r4;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;

import java.util.Objects;
import org.apache.commons.io.IOUtils;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class OrganizationFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<OrganizationFhirResourceProvider, Organization> {
	
	private static final String ORGANIZATION_INITIAL_DATA_XML = "test_data/FhirOrganizationTestdata.xml";
	
	private static final String JSON_ORGANIZATION = "test_data/organization.json";
	
	private static final String ORGANIZATION_UUID = "cf9b1f44-0e8f-42f1-900b-bf1c5d4ed5CC";
	
	private static final String UNKNOWN_ORGANIZATION_UUID = "8516d594-9c31-4bd3-bfec-b42b2f8a8444";
	
	@Autowired
	@Getter(AccessLevel.PUBLIC)
	private OrganizationFhirResourceProvider resourceProvider;
	
	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		executeDataSet(ORGANIZATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void shouldReturnExistingOrganizationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Organization/" + ORGANIZATION_UUID).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Organization org = readResponse(response);
		
		assertThat(org, notNullValue());
		assertThat(org.getIdElement().getIdPart(), equalTo(ORGANIZATION_UUID));
		assertThat(org, validResource());
	}
	
	@Test
	public void shouldThrow404ForNonExistingOrganizationAsJson() throws Exception {
		MockHttpServletResponse response = get("/Organization/" + UNKNOWN_ORGANIZATION_UUID).accept(FhirMediaTypes.JSON)
		        .go();
		
		assertThat(response, isNotFound());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		OperationOutcome operationOutcome = readOperationOutcome(response);
		
		assertThat(operationOutcome, notNullValue());
		assertThat(operationOutcome.hasIssue(), is(true));
	}
	
	@Test
	public void shouldReturnExistingLocationAsXML() throws Exception {
		MockHttpServletResponse response = get("/Organization/" + ORGANIZATION_UUID).accept(FhirMediaTypes.XML).go();
		
		assertThat(response, isOk());
		assertThat(response.getContentType(), is(FhirMediaTypes.XML.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Organization org = readResponse(response);
		
		assertThat(org, notNullValue());
		assertThat(org.getIdElement().getIdPart(), equalTo(ORGANIZATION_UUID));
		assertThat(org, validResource());
	}
	
	@Test
	public void shouldCreateNewOrganozationAsJson() throws Exception {
		// read JSON record
		String jsonOrg;
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(JSON_ORGANIZATION)) {
			Objects.requireNonNull(is);
			jsonOrg = IOUtils.toString(is, StandardCharsets.UTF_8);
		}
		
		// create Organization
		MockHttpServletResponse response = post("/Organization").accept(FhirMediaTypes.JSON).jsonContent(jsonOrg).go();
		
		// verify created correctly
		assertThat(response, isCreated());
		assertThat(response.getContentType(), is(FhirMediaTypes.JSON.toString()));
		assertThat(response.getContentAsString(), notNullValue());
		
		Organization org = readResponse(response);
		
		assertThat(org, notNullValue());
		assertThat(org.getName(), equalTo("Test Org"));
		assertThat(org, validResource());
		
		response = get("/Organization/" + org.getIdElement().getIdPart()).accept(FhirMediaTypes.JSON).go();
		
		assertThat(response, isOk());
		
		Organization newOrg = readResponse(response);
		
		assertThat(newOrg.getId(), equalTo(org.getId()));
	}
}
