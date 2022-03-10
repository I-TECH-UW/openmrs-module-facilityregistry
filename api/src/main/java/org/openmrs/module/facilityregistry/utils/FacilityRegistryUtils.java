package org.openmrs.module.facilityregistry.utils;

import java.io.IOException;
import java.util.Objects;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Type;
import org.openmrs.module.facilityregistry.FacilityRegistryConstants;
import org.openmrs.module.facilityregistry.api.FhirOrganizationService;
import org.openmrs.module.facilityregistry.task.FacilityRegistryTask;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.api.FhirService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter(AccessLevel.PACKAGE)
public class FacilityRegistryUtils {
	@Autowired
	private FhirLocationService locationService;

	@Autowired
	private FhirOrganizationService organizationService;

	private static final Logger log = LoggerFactory.getLogger(FacilityRegistryUtils.class);

	/**
	 * Saves/Updates Fhir Location into the OpenMRS database
	 *
	 * @param searchBundle Bundle fetched from the Facility Registry Server
	 */
	public Resource saveFhirLocation(Bundle searchBundle, IGenericClient fhirClient) {
		for (Bundle.BundleEntryComponent entry : searchBundle.getEntry()) {
			if (entry.hasResource()) {
				if (ResourceType.Location.equals(entry.getResource().getResourceType())) {
					Location newLocation = (Location) entry.getResource();
					// tag the Location from Facility Registry as mCSD Location
					newLocation.getMeta().addTag(FacilityRegistryConstants.FACILITY_REGISTRY_LOCATION_FHIR_SYSTEM,
							FacilityRegistryConstants.FACILITY_REGISTRY_LOCATION,
							FacilityRegistryConstants.FACILITY_REGISTRY_LOCATION);
					// tag Laboratory locations as mCSD Laboratory
					if(newLocation.hasType("Laboratory")) {
						newLocation.getMeta().addTag("mCSD Laboratory", "mCSD Laboratory", "mCSD Laboratory");
					}
					return saveOrUpdateLocation(newLocation);
				} else if (ResourceType.Organization.equals(entry.getResource().getResourceType())) {
					Organization organization = (Organization) entry.getResource();
					if (organization.hasExtension(FacilityRegistryConstants.MCSD_EXTENTION_URL)) {
						Extension extParOf = organization.getExtensionByUrl(FacilityRegistryConstants.MCSD_EXTENTION_URL)
								.getExtensionByUrl(FacilityRegistryConstants.MCSD_EXTENTION_URL_PART_OF);
						Type referenceOrgType = extParOf.getValue();
						if (referenceOrgType instanceof Reference) {
							Reference reference = (Reference) referenceOrgType;
							String referenceOrgId = reference.getReference();
							Organization mcsdReferenceOrg = fhirClient.read().resource(Organization.class)
									.withId(referenceOrgId).encodedJson().execute();
							saveOrUpdateOrganization(mcsdReferenceOrg);
							// save the mcsd reference Organization as a location
							Location mcsdreferenceLocation = convertOrganisationToLocation(mcsdReferenceOrg);
							saveOrUpdateLocation(mcsdreferenceLocation);

						}
					}
					if (organization.hasPartOf()) {
						String parentOrgId = organization.getPartOf().getReference();
						Organization partOfOrg = fhirClient.read().resource(Organization.class).withId(parentOrgId)
								.encodedJson().execute();
						saveOrUpdateOrganization(partOfOrg);

					}
					return saveOrUpdateOrganization(organization);
				}
			}
		}
		return null;
	}

	private Organization saveOrUpdateOrganization(Organization newOrganization) {
		return (Organization) saveOrUpdate(newOrganization, organizationService);
	}

	private Location saveOrUpdateLocation(Location newLocation) {
		return (Location) saveOrUpdate(newLocation, locationService);
	}

	private IAnyResource saveOrUpdate(IAnyResource newResource, FhirService service ) {
		IAnyResource existing;
		try {
			existing = service.get(newResource.getIdElement().getIdPart());
		} catch (ResourceNotFoundException e) {
			existing = null;
		}
		if (Objects.isNull(existing)) {
			return service.create(newResource);
		} else {
			return service.update(newResource.getIdElement().getIdPart(), newResource);
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
		orgLocation.setStatus(Location.LocationStatus.ACTIVE);
		orgLocation.setType(organisation.getType());
		orgLocation.setIdentifier(organisation.getIdentifier());
		// tag the Organisation from Facility Registry as mCSD_Organisation
		orgLocation.getMeta().addTag(FacilityRegistryConstants.FACILITY_REGISTRY_ORGANISATION_FHIR_SYSTEM,
				FacilityRegistryConstants.FACILITY_REGISTRY_ORGANISATION,
				FacilityRegistryConstants.FACILITY_REGISTRY_ORGANISATION);
		return orgLocation;
	}

}
