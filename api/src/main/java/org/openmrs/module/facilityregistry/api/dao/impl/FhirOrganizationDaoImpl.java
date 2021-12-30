package org.openmrs.module.facilityregistry.api.dao.impl;

import javax.annotation.Nonnull;

import org.hibernate.Criteria;
import org.openmrs.module.facilityregistry.api.dao.FhirOrganizationDao;
import org.openmrs.module.facilityregistry.model.FhirOrganization;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.impl.BaseFhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.rest.param.StringAndListParam;
import lombok.Setter;
import lombok.AccessLevel;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirOrganizationDaoImpl extends BaseFhirDao<FhirOrganization> implements FhirOrganizationDao {
   
    @Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.NAME_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleName(criteria, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.CITY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCity(criteria, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.STATE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleState(criteria, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.COUNTRY_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleCountry(criteria, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.POSTALCODE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handlePostalCode(criteria, (StringAndListParam) param.getParam()));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
			}
		});
	}
	
	private void handleName(Criteria criteria, StringAndListParam namePattern) {
		handleAndListParam(namePattern, (name) -> propertyLike("name", name)).ifPresent(criteria::add);
	}
	
	private void handleCity(Criteria criteria, StringAndListParam cityPattern) {
		handleAndListParam(cityPattern, (city) -> propertyLike("address.city", city)).ifPresent(criteria::add);
	}
	
	private void handleCountry(Criteria criteria, StringAndListParam countryPattern) {
		handleAndListParam(countryPattern, (country) -> propertyLike("address.country", country)).ifPresent(criteria::add);
	}
	
	private void handlePostalCode(Criteria criteria, StringAndListParam postalCodePattern) {
		handleAndListParam(postalCodePattern, (postalCode) -> propertyLike("address.postalCode", postalCode))
		        .ifPresent(criteria::add);
	}
	
	private void handleState(Criteria criteria, StringAndListParam statePattern) {
		handleAndListParam(statePattern, (state) -> propertyLike("address.state", state)).ifPresent(criteria::add);
	}
	
	
	@Override
	protected String paramToProp(@Nonnull String param) {
		switch (param) {
			case org.hl7.fhir.r4.model.Organization.SP_NAME:
				return "name";
			case org.hl7.fhir.r4.model.Organization.SP_ADDRESS_CITY:
				return "city";
			case org.hl7.fhir.r4.model.Organization.SP_ADDRESS_STATE:
				return "state";
			case org.hl7.fhir.r4.model.Organization.SP_ADDRESS_COUNTRY:
				return "country";
			case org.hl7.fhir.r4.model.Organization.SP_ADDRESS_POSTALCODE:
				return "postalCode";
			default:
				return super.paramToProp(param);
		}
	}
}
