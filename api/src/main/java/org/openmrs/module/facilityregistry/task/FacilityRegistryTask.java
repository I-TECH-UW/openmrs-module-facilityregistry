package org.openmrs.module.facilityregistry.task;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;

import org.openmrs.api.AdministrationService;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.context.ApplicationContext;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.facilityregistry.FacilityRegistryConstants;
import org.openmrs.module.facilityregistry.utils.FhirUtils;
import org.openmrs.module.fhir2.api.FhirLocationService;

public class FacilityRegistryTask extends AbstractTask {
	
	private AdministrationService administrationService;
	
	@Override
	public void execute() {
		Bundle searchBundle;
		try {
			searchBundle = getFhirClient().search().forResource(Location.class).returnBundle(Bundle.class).execute();
			System.out.println("........test Bundle............");
			System.out.println(
			    FhirUtils.getFhirContext().newJsonParser().setPrettyPrint(true).encodeResourceToString(searchBundle));
			saveFhirLocation(searchBundle);
			while (searchBundle.getLink(IBaseBundle.LINK_NEXT) != null) {
				searchBundle = getFhirClient().loadPage().next(searchBundle).execute();
				saveFhirLocation(searchBundle);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private IGenericClient getFhirClient() throws IOException {
		administrationService = Context.getAdministrationService();
		String fhirStorePath = administrationService
		        .getGlobalProperty(FacilityRegistryConstants.GP_FACILITY_REGISTRY_SERVER_URL);
		String authUrl = administrationService.getGlobalProperty(FacilityRegistryConstants.GP_FACILITY_REGISTRY_AUTH_URL);
		String authUserName = administrationService
		        .getGlobalProperty(FacilityRegistryConstants.GP_FACILITY_REGISTRY_USER_NAME);
		String authPassowrd = administrationService
		        .getGlobalProperty(FacilityRegistryConstants.GP_FACILITY_REGISTRY_PASSWORD);
		String token = FhirUtils.getAccesToken(authUrl, authUserName, authPassowrd);
		System.out.println("........test Token............");
		System.out.println(token);
		return FhirUtils.getFhirClient(fhirStorePath, token);
	}
	
	private FhirLocationService getFhirLocationService() {
		try {
			Field serviceContextField = Context.class.getDeclaredField("serviceContext");
			serviceContextField.setAccessible(true);
			FhirLocationService locationService;
			try {
				ApplicationContext applicationContext = ((ServiceContext) serviceContextField.get(null))
				        .getApplicationContext();
				locationService = applicationContext.getBean(FhirLocationService.class);
			}
			finally {
				serviceContextField.setAccessible(false);
			}
			return locationService;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void saveFhirLocation(Bundle searchBundle) {
		FhirLocationService locationService = getFhirLocationService();
		for (BundleEntryComponent entry : searchBundle.getEntry()) {
			if (entry.hasResource() && ResourceType.Location.equals(entry.getResource().getResourceType())) {
				Location newLocation = (Location) entry.getResource();
				Location existingLocation;
				try {
					existingLocation = locationService.get(newLocation.getIdElement().getIdPart());
				}
				catch (ResourceNotFoundException e) {
					existingLocation = null;
				}
				if (Objects.isNull(existingLocation)) {
					locationService.create(newLocation);
					System.out.println("created new Location" + newLocation.getIdElement().getIdPart());
				} else {
					locationService.update(newLocation.getIdElement().getIdPart(), newLocation);
					System.out.println("Updated Location" + newLocation.getIdElement().getIdPart());
				}
				
			}
		}
		
	}
	
}
