/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.facilityregistry.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.facilityregistry.TestFhirSpringConfiguration;
import org.openmrs.module.facilityregistry.api.dao.impl.FhirOrganizationDaoImpl;
import org.openmrs.module.facilityregistry.model.FhirOganizationAddress;
import org.openmrs.module.facilityregistry.model.FhirOrganization;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirOrganizationDaoImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String ORGANIZATION_INITIAL_DATA_XML = "test_data/FhirOrganizationTestdata.xml";
	
	private static final String ORGANIZATION_UUID = "cf9b1f44-0e8f-42f1-900b-bf1c5d4ed5CC";
	
	private FhirOrganizationDaoImpl dao;
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Before
	public void setup() throws Exception {
		dao = new FhirOrganizationDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(ORGANIZATION_INITIAL_DATA_XML);
	}
	
	@Test
	public void get_shouldGetFhirOrganizationByUuid() {
		FhirOrganization result = dao.get(ORGANIZATION_UUID);
		
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(ORGANIZATION_UUID));
		assertThat(result.getName(), equalTo("OpenMRS org"));
		assertThat(result.getAddress(), hasSize(equalTo(2)));
	}
	
	@Test
	public void saveOrUpdate_shouldSaveNewFhirOrganization() {
		FhirOganizationAddress add1 = new FhirOganizationAddress();
		add1.setId(3);
		add1.setName("add1");
		add1.setCity("kla");
		add1.setState("Buganda");
		add1.setDistrict("Luweero");
		add1.setCountry("ug");
		
		FhirOganizationAddress add2 = new FhirOganizationAddress();
		add2.setId(4);
		add2.setName("add2");
		add2.setCity("kla2");
		add2.setState("Buganda2");
		add2.setDistrict("Luweero2");
		add2.setCountry("ug2");
		FhirOrganization org = new FhirOrganization();
		org.setName("test Org");
		org.setActive(true);
		org.addAddress(add1);
		org.addAddress(add2);
		
		FhirOrganization result = dao.createOrUpdate(org);
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo("test Org"));
		assertThat(result.getAddress(), hasSize(equalTo(2)));
	}
}
