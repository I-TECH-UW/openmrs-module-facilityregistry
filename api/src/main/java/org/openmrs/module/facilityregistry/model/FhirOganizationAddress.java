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

import org.openmrs.Auditable;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Retireable;
import org.openmrs.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.search.annotations.Field;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "fhir_organization_address")
public class FhirOganizationAddress implements Auditable, Retireable {
	
	private static final long serialVersionUID = 1L;
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "organization_address_id")
	private Integer id;
	
	@Column(name = "city", length = 255)
	private String city;
	
	@Column(name = "state", length = 255)
	private String state;
	
	@Column(name = "district", length = 255)
	private String district;
	
	@Column(name = "country", length = 255)
	private String country;
	
	@Column(name = "postal_code", length = 255)
	private String postalCode;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "creator", updatable = false)
	protected User creator;
	
	@Column(name = "date_created", nullable = false, updatable = false)
	private Date dateCreated;
	
	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed")
	private Date dateChanged;
	
	@Column(name = "retired", nullable = false)
	@Field
	private Boolean retired = Boolean.FALSE;
	
	@Column(name = "date_retired")
	private Date dateRetired;
	
	@ManyToOne
	@JoinColumn(name = "retired_by")
	private User retiredBy;
	
	@Column(name = "retire_reason")
	private String retireReason;
	
	@Column(name = "uuid", unique = true, nullable = false, length = 36)
	private String uuid = UUID.randomUUID().toString();
	
	@Override
	@Deprecated
	public Boolean isRetired() {
		return retired;
	}
	
}
