package org.openmrs.module.facilityregistry.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import junit.framework.TestCase;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.facilityregistry.api.FhirOrganizationService;
import org.openmrs.module.fhir2.api.FhirLocationService;

@RunWith(MockitoJUnitRunner.class)
public class FacilityRegistryUtilsTest extends TestCase {
	
	private static final String EXAMPLE_SEARCH_BUNDLE_URL = "https://i-tech-uw.github.io/facility-ig/Bundle-example-fr-search-bundle.json";
	
	@Mock
	private FhirLocationService locationService;
	
	@Mock
	private FhirOrganizationService organizationService;
	
	@Mock
	private IGenericClient fhirClient;
	
	private FacilityRegistryUtils frUtils;
	
	private Bundle searchBundle;
	
	private Location exampleLocation;
	
	@Before
	public void setup() throws IOException {
		frUtils = new FacilityRegistryUtils();
		frUtils.setLocationService(locationService);
		frUtils.setOrganizationService(organizationService);
		
		searchBundle = getExampleBundle();
		exampleLocation = (Location) searchBundle.getEntryFirstRep().getResource();
	}
	
	@Test
	public void testSaveFhirLocation() {
		when(locationService.get(any(String.class))).thenReturn(null);
		when(locationService.create(any(Location.class))).thenReturn(exampleLocation);
		
		frUtils.saveFhirLocation(searchBundle, fhirClient);
	}
	
	private Bundle getExampleBundle() throws IOException {
		try (CloseableHttpClient client = HttpClients.createDefault();
		        CloseableHttpResponse response = client.execute(new HttpGet(EXAMPLE_SEARCH_BUNDLE_URL))) {
			
			System.out.println(response.getStatusLine().toString());
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// return it as a String
				String result = EntityUtils.toString(entity);
				System.out.println(result);
				
				// Parse it
				// TODO: FIGURE OUT JACKSON ISSUE WITH PARSER: https://stackoverflow.com/questions/56872363/couldnt-resolve-error-java-lang-nosuchfielderror-fail-on-symbol-hash-overflow
				// TODO: and https://github.com/hapifhir/hapi-fhir/issues/2873
				Bundle parsed = FhirUtils.getParser().parseResource(Bundle.class, result);
				
				return parsed;
			}
		}
		
		return null;
	}
}
