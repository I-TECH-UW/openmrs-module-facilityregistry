/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.facilityregistry.task;

import java.io.IOException;
import java.util.Objects;

import org.openmrs.api.AdministrationService;
import org.openmrs.module.facilityregistry.utils.FacilityRegistryUtils;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Location.LocationStatus;
import org.openmrs.api.context.Context;
import org.openmrs.module.facilityregistry.FacilityRegistryConstants;
import org.openmrs.module.facilityregistry.api.FhirOrganizationService;
import org.openmrs.module.facilityregistry.utils.FhirUtils;
import org.openmrs.module.fhir2.api.FhirLocationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A scheduled task that automatically poll data from a Facility Registry Server ie GOFR
 */
@Component
public class FacilityRegistryTask extends AbstractTask implements ApplicationContextAware {
	
	private static final Logger log = LoggerFactory.getLogger(FacilityRegistryTask.class);
	
	private static ApplicationContext applicationContext;

	@Autowired
	@Qualifier("adminService")
	private AdministrationService administrationService;

	@Autowired
	private FacilityRegistryUtils facilityRegistryUtils;

	@Override
	public void execute() {
		
		try {
			applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
		}
		catch (Exception e) {
			// return;
		}
		Bundle searchBundle;
		try {
			log.info("executing FacilityRegistryTask");
			searchBundle = getFhirClient().search().forResource(Location.class).include(Location.INCLUDE_ORGANIZATION)
			        .returnBundle(Bundle.class).execute();
			log.debug(FhirUtils.getFhirContext().newJsonParser().setPrettyPrint(true).encodeResourceToString(searchBundle));
			facilityRegistryUtils.saveFhirLocation(searchBundle, getFhirClient());
			while (searchBundle.getLink(IBaseBundle.LINK_NEXT) != null) {
				searchBundle = getFhirClient().loadPage().next(searchBundle).execute();
				facilityRegistryUtils.saveFhirLocation(searchBundle, getFhirClient());
			}
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
		
	}

	private IGenericClient getFhirClient() throws IOException {
		administrationService = Context.getAdministrationService();
		String fhirStorePath = administrationService.getGlobalProperty(
				FacilityRegistryConstants.GP_FACILITY_REGISTRY_SERVER_URL, "http://localhost:4000/fhir/DEFAULT");
		String authUrl = administrationService.getGlobalProperty(FacilityRegistryConstants.GP_FACILITY_REGISTRY_AUTH_URL,
				"http://localhost:4000/auth/token");
		String authUserName = administrationService
				.getGlobalProperty(FacilityRegistryConstants.GP_FACILITY_REGISTRY_USER_NAME, "root@gofr.org");
		String authPassword = administrationService
				.getGlobalProperty(FacilityRegistryConstants.GP_FACILITY_REGISTRY_PASSWORD, "gofr");

		String token = FhirUtils.getAccesToken(authUrl, authUserName, authPassword);
		log.info("generating Bearer Token");
		log.debug(token);

		return FhirUtils.getFhirClient(fhirStorePath, token);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
