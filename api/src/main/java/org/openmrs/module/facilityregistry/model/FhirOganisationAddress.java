package org.openmrs.module.facilityregistry.model;

import org.openmrs.BaseOpenmrsMetadata;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "fhir_organization_address")
public class FhirOganisationAddress extends BaseOpenmrsMetadata {

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
	
	@Column(name = "postalCode", length = 255)
	private String postalCode;
	
	@Column(name = "county", length = 255)
	private String county;
}
