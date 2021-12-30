package org.openmrs.module.facilityregistry.translators;

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Organization;
import org.openmrs.module.facilityregistry.model.FhirOrganization;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirUpdatableTranslator;

public interface OrganizationTranslator extends OpenmrsFhirUpdatableTranslator<FhirOrganization , Organization> {
	
	/**
	 * Maps an {@link FhirOrganization} to a {@link org.hl7.fhir.r4.model.Organization}
	 * 
	 * @param openmrsOrganization the organization to translate
	 * @return the corresponding FHIR Organization resource
	 */
	@Override
	Organization toFhirResource(@Nonnull FhirOrganization fhirOganization);
	
	/**
	 * Maps a {@link org.hl7.fhir.r4.model.Organization} to an {@link FhirOrganization}
	 * 
	 * @param fhirOrganization the FHIR Organization to translate
	 * @return the corresponding OpenMRS organization
	 */
	@Override
	FhirOrganization toOpenmrsType(@Nonnull Organization fhirOrganization);
	
	/**
	 * Maps a {@link Organization} to an existing {@link FhirOrganization}
	 *
	 * @param existingOrganization the Organization to update
	 * @param fhirOrganization the FHIR Organization to map
	 * @return the updated OpenMRS FhirOrganization
	 */
	@Override
	FhirOrganization toOpenmrsType(@Nonnull FhirOrganization existingOrganization, @Nonnull Organization fhirOrganization);
}
