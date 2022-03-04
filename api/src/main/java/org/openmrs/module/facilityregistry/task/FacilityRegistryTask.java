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
	private FhirLocationService locationService;
	
	@Autowired
	FhirOrganizationService organizationService;
	
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
			saveFhirLocation(searchBundle);
			while (searchBundle.getLink(IBaseBundle.LINK_NEXT) != null) {
				searchBundle = getFhirClient().loadPage().next(searchBundle).execute();
				saveFhirLocation(searchBundle);
			}
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
		
	}
	
	/**
	 * Creates Fhir Client with Bearer Authentication
	 * 
	 * @return IGenericClient
	 */
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
	
	/**
	 * Saves/Updates Fhir Location into the OpenMRS database
	 * 
	 * @param searchBundle Bundle fetched from the Facility Registry Server
	 * @throws IOException
	 */
	private void saveFhirLocation(Bundle searchBundle) throws IOException {
		for (BundleEntryComponent entry : searchBundle.getEntry()) {
			if (entry.hasResource()) {
				if (ResourceType.Location.equals(entry.getResource().getResourceType())) {
					Location newLocation = (Location) entry.getResource();
					// tag the Location from Facility Registry as mCSD Location
					newLocation.getMeta().addTag(FacilityRegistryConstants.FACILITY_REGISTRY_LOCATION_FHIR_SYSTEM,
					    FacilityRegistryConstants.FACILITY_REGISTRY_LOCATION,
					    FacilityRegistryConstants.FACILITY_REGISTRY_LOCATION);
					saveOrUpdateLocation(newLocation);
				} else if (ResourceType.Organization.equals(entry.getResource().getResourceType())) {
					Organization organization = (Organization) entry.getResource();
					if (organization.hasExtension(FacilityRegistryConstants.MCSD_EXTENTION_URL)) {
						Extension extParOf = organization.getExtensionByUrl(FacilityRegistryConstants.MCSD_EXTENTION_URL)
						        .getExtensionByUrl(FacilityRegistryConstants.MCSD_EXTENTION_URL_PART_OF);
						Type referenceOrgType = extParOf.getValue();
						if (referenceOrgType instanceof Reference) {
							Reference reference = (Reference) referenceOrgType;
							String referenceOrgId = reference.getReference();
							Organization mcsdReferenceOrg = getFhirClient().read().resource(Organization.class)
							        .withId(referenceOrgId).encodedJson().execute();
							saveOrUpdateOrganization(mcsdReferenceOrg);
							// save the mcsd reference Organization as a location
							Location mcsdreferenceLocation = convertOrganisationToLocation(mcsdReferenceOrg);
							saveOrUpdateLocation(mcsdreferenceLocation);
							
						}
					}
					if (organization.hasPartOf()) {
						String parentOrgId = organization.getPartOf().getReference();
						Organization partOfOrg = getFhirClient().read().resource(Organization.class).withId(parentOrgId)
						        .encodedJson().execute();
						saveOrUpdateOrganization(partOfOrg);
						
					}
					saveOrUpdateOrganization(organization);
				}
			}
		}
		
	}
	
	private void saveOrUpdateLocation(Location newLocation) {
		Location existingLocation;
		try {
			existingLocation = locationService.get(newLocation.getIdElement().getIdPart());
		}
		catch (ResourceNotFoundException e) {
			existingLocation = null;
		}
		if (Objects.isNull(existingLocation)) {
			locationService.create(newLocation);
			log.debug("created new Location Resource with ID " + newLocation.getIdElement().getIdPart());
		} else {
			locationService.update(newLocation.getIdElement().getIdPart(), newLocation);
			log.debug("Updated Location Resource with ID " + newLocation.getIdElement().getIdPart());
		}
		
	}
	
	private void saveOrUpdateOrganization(Organization newOrganization) {
		Organization existingOrganization;
		try {
			existingOrganization = organizationService.get(newOrganization.getIdElement().getIdPart());
		}
		catch (ResourceNotFoundException e) {
			existingOrganization = null;
		}
		if (Objects.isNull(existingOrganization)) {
			organizationService.create(newOrganization);
			log.debug("created new Organization Resource with ID " + newOrganization.getIdElement().getIdPart());
		} else {
			organizationService.update(newOrganization.getIdElement().getIdPart(), newOrganization);
			log.debug("Updated Organization Resource with ID " + newOrganization.getIdElement().getIdPart());
		}
		
	}
	
	/**
	 * Converts mcsd Organisation to Location to be persisted by the Fhir Location Service
	 * 
	 * @param organisation Organisation to be converted
	 * @return converted Location
	 */
	private Location convertOrganisationToLocation(Organization organisation) {
		Location orgLocation = new Location();
		orgLocation.setId(organisation.getIdElement().getIdPart());
		orgLocation.setName(organisation.getName());
		orgLocation.setDescription(organisation.getName());
		orgLocation.setAddress(organisation.getAddressFirstRep());
		orgLocation.setStatus(LocationStatus.ACTIVE);
		orgLocation.setType(organisation.getType());
		orgLocation.setIdentifier(organisation.getIdentifier());
		// tag the Organisation from Facility Registry as mCSD_Organisation
		orgLocation.getMeta().addTag(FacilityRegistryConstants.FACILITY_REGISTRY_ORGANISATION_FHIR_SYSTEM,
		    FacilityRegistryConstants.FACILITY_REGISTRY_ORGANISATION,
		    FacilityRegistryConstants.FACILITY_REGISTRY_ORGANISATION);
		return orgLocation;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
