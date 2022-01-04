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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import org.openmrs.module.facilityregistry.providers.r4.OrganizationFhirResourceProvider;
import org.openmrs.module.fhir2.providers.r4.BaseFhirR4IntegrationTest;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.r4.model.Organization;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class OrganizationFhirResourceProviderIntegrationTest extends BaseFhirR4IntegrationTest<OrganizationFhirResourceProvider, Organization> {
	
	private static final String ORGANIZATION_INITIAL_DATA_XML = "test_data/FhirOrganizationTestdata.xml";
	
	private static final String ORGANIZATION_UUID = "cf9b1f44-0e8f-42f1-900b-bf1c5d4ed5CC";
	
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
		
		//assertThat(response, isOk());
	}
	
}
