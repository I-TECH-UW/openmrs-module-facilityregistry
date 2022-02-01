/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.facilityregistry.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "fhir_organization")
public class FhirOrganization extends BaseOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "organization_id")
	private Integer id;
	
	@Column(name = "name", nullable = false, length = 255)
	private String name;
	
	@Column(name = "active")
	private Boolean active;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "fhir_organization_type", joinColumns = @JoinColumn(name = "organization_id"), inverseJoinColumns = @JoinColumn(name = "concept_id"))
	private Set<Concept> type;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "fhir_organization_addresses", joinColumns = @JoinColumn(name = "organization_id"), inverseJoinColumns = @JoinColumn(name = "organization_address_id"))
	private Set<FhirOganizationAddress> address;
	
	// The organization, of which this organization forms a part
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "partof_org_id", referencedColumnName = "organization_id")
	private FhirOrganization partOfOrg;
	
	// part-of organization ,defined by the http://ihe.net/fhir/StructureDefinition/IHE.mCSD.hierarchy.extension
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "mcsd_partof_org_id", referencedColumnName = "organization_id")
	private FhirOrganization mcsdPartOfOrg;
	
	//hierarchy-type Coding ,defined by the http://ihe.net/fhir/StructureDefinition/IHE.mCSD.hierarchy.extension
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "mcsd_hierachy_concept_id", referencedColumnName = "concept_id")
	private Concept mcsdHierarchyType;
	
	public void addAddress(FhirOganizationAddress newAddress) {
		if (address == null) {
			address = new HashSet<>();
		}
		if (address != null && !address.contains(newAddress)) {
			address.add(newAddress);
		}
	}
	
	public void addType(Concept newType) {
		if (type == null) {
			type = new HashSet<>();
		}
		if (type != null && !type.contains(newType)) {
			type.add(newType);
		}
	}
}
