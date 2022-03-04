/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.facilityregistry.api.translators.impl;

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Type;

import org.openmrs.Concept;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.facilityregistry.FacilityRegistryConstants;
import org.openmrs.module.facilityregistry.api.dao.FhirOrganizationDao;
import org.openmrs.module.facilityregistry.api.translators.OrganizationTranslator;
import org.openmrs.module.facilityregistry.model.FhirOganizationAddress;
import org.openmrs.module.facilityregistry.model.FhirOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.impl.BaseReferenceHandlingTranslator;

import lombok.Setter;
import lombok.AccessLevel;

import static org.apache.commons.lang3.Validate.notNull;

@Component
@Setter(AccessLevel.PACKAGE)
public class OrganizationTranslatorImpl extends BaseReferenceHandlingTranslator implements OrganizationTranslator {
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService administrationService;
	
	@Autowired
	private ConceptTranslator conceptTranslator;
	
	@Autowired
	private FhirOrganizationDao fhirOrganizationDao;
	
	@Override
	public Organization toFhirResource(@Nonnull FhirOrganization openmrsOrg) {
		notNull(openmrsOrg, "The Openmrs Fhir Organization object should not be null");
		Organization fhirOrg = new Organization();
		fhirOrg.setId(openmrsOrg.getUuid());
		fhirOrg.setName(openmrsOrg.getName());
		Identifier identifier = fhirOrg.addIdentifier();
		identifier.setSystem(
		    administrationService.getGlobalProperty(FacilityRegistryConstants.FACILITY_REGISTRY_ORGANISATION_FHIR_SYSTEM,
		        "http://fhir.openmrs.org/organization/identifier")).setValue(openmrsOrg.getUuid());
		if (openmrsOrg.getAddress() != null) {
			for (FhirOganizationAddress openmrsAddress : openmrsOrg.getAddress()) {
				Address fhirAddress = fhirOrg.addAddress();
				if (openmrsAddress.getCity() != null) {
					fhirAddress.setCity(openmrsAddress.getCity());
				}
				if (openmrsAddress.getState() != null) {
					fhirAddress.setState(openmrsAddress.getState());
				}
				if (openmrsAddress.getDistrict() != null) {
					fhirAddress.setDistrict(openmrsAddress.getDistrict());
				}
				if (openmrsAddress.getCountry() != null) {
					fhirAddress.setCountry(openmrsAddress.getCountry());
				}
				
				if (openmrsAddress.getPostalCode() != null) {
					fhirAddress.setPostalCode(openmrsAddress.getPostalCode());
				}
			}
		}
		fhirOrg.setActive(openmrsOrg.getActive());
		for (Concept concept : openmrsOrg.getType()) {
			fhirOrg.addType(conceptTranslator.toFhirResource(concept));
		}
		
		if (openmrsOrg.getPartOfOrg() != null) {
			fhirOrg.setPartOf(new Reference(ResourceType.Organization + "/" + openmrsOrg.getPartOfOrg().getUuid()));
		}
		
		// add the mCSD ext http://ihe.net/fhir/StructureDefinition/IHE.mCSD.hierarchy.extension 
		if (openmrsOrg.getMcsdPartOfOrg() != null) {
			Extension mcsdExt = fhirOrg.addExtension();
			mcsdExt.setUrl(FacilityRegistryConstants.MCSD_EXTENTION_URL);
			
			Reference reference = new Reference(ResourceType.Organization + "/" + openmrsOrg.getMcsdPartOfOrg().getUuid());
			
			Extension partOfExt = mcsdExt.addExtension();
			partOfExt.setUrl(FacilityRegistryConstants.MCSD_EXTENTION_URL_PART_OF);
			partOfExt.setValue(reference);
			
			if (openmrsOrg.getMcsdHierarchyType() != null) {
				Extension hierarchyTypeExt = mcsdExt.addExtension();
				hierarchyTypeExt.setUrl(FacilityRegistryConstants.MCSD_EXTENTION_URL_HIERACHY_TYPE);
				hierarchyTypeExt.setValue(conceptTranslator.toFhirResource(openmrsOrg.getMcsdHierarchyType()));
			}
			
			fhirOrg.getMeta().setLastUpdated(openmrsOrg.getDateChanged());
			
		}
		return fhirOrg;
	}
	
	@Override
	public FhirOrganization toOpenmrsType(Organization fhirOrganization) {
		notNull(fhirOrganization, "The Organization object should not be null");
		return toOpenmrsType(new FhirOrganization(), fhirOrganization);
	}
	
	@Override
	public FhirOrganization toOpenmrsType(FhirOrganization openmrsOrg, Organization fhirOrg) {
		notNull(fhirOrg, "The Organization object should not be null");
		notNull(openmrsOrg, "The existing Organization object should not be null");
		
		openmrsOrg.setUuid(fhirOrg.getIdElement().getIdPart());
		openmrsOrg.setName(fhirOrg.getName());
		openmrsOrg.setActive(fhirOrg.getActive());
		for (CodeableConcept code : fhirOrg.getType()) {
			openmrsOrg.addType(conceptTranslator.toOpenmrsType(code));
		}
		
		for (Address fhirAddress : fhirOrg.getAddress()) {
			FhirOganizationAddress openmrsAddress = new FhirOganizationAddress();
			openmrsAddress.setCity(fhirAddress.getCity());
			openmrsAddress.setState(fhirAddress.getState());
			openmrsAddress.setDistrict(fhirAddress.getDistrict());
			openmrsAddress.setCountry(fhirAddress.getCountry());
			openmrsAddress.setPostalCode(fhirAddress.getPostalCode());
			openmrsOrg.addAddress(openmrsAddress);
		}
		
		if (fhirOrg.hasPartOf()) {
			openmrsOrg.setPartOfOrg(getPartOfOrg(fhirOrg.getPartOf()));
		}
		
		if (fhirOrg.hasExtension(FacilityRegistryConstants.MCSD_EXTENTION_URL)) {
			Extension mcsdExt = fhirOrg.getExtensionByUrl(FacilityRegistryConstants.MCSD_EXTENTION_URL);
			if (mcsdExt.hasExtension(FacilityRegistryConstants.MCSD_EXTENTION_URL_PART_OF)) {
				Extension extParOf = mcsdExt.getExtensionByUrl(FacilityRegistryConstants.MCSD_EXTENTION_URL_PART_OF);
				Type referenceOrgType = extParOf.getValue();
				if (referenceOrgType instanceof Reference) {
					Reference reference = (Reference) referenceOrgType;
					openmrsOrg.setMcsdPartOfOrg(getPartOfOrg(reference));
				}
			}
			
			if (mcsdExt.hasExtension(FacilityRegistryConstants.MCSD_EXTENTION_URL_HIERACHY_TYPE)) {
				Extension extHierachyType = mcsdExt
				        .getExtensionByUrl(FacilityRegistryConstants.MCSD_EXTENTION_URL_HIERACHY_TYPE);
				Type codeableType = extHierachyType.getValue();
				if (codeableType instanceof CodeableConcept) {
					CodeableConcept code = (CodeableConcept) codeableType;
					openmrsOrg.setMcsdHierarchyType(conceptTranslator.toOpenmrsType(code));
				}
			}
			
		}
		return openmrsOrg;
	}
	
	private FhirOrganization getPartOfOrg(Reference organization) {
		if (organization == null) {
			return null;
		}
		if (organization.hasType() && !organization.getType().equals(ResourceType.Organization.toString())) {
			throw new IllegalArgumentException("Reference must be to Organization not a " + organization.getType());
		}
		return getReferenceId(organization).map(uuid -> fhirOrganizationDao.get(uuid)).orElse(null);
	}
}
