package org.openmrs.module.facilityregistry.model;

import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
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
	
	@ElementCollection(targetClass = String.class)
	@CollectionTable(name = "fhir_organization_identifier", joinColumns = {
	        @JoinColumn(name = "organization_id", referencedColumnName = "organization_id") })
	@MapKeyColumn(name = "identifier_system")
	@Column(name = "identifier_code")
	private Map<String, String> identifier;
	
	@Column(name = "active")
	private Boolean active;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "fhir_organization_type", joinColumns = @JoinColumn(name = "organization_id"), inverseJoinColumns = @JoinColumn(name = "concept_id"))
	private List<Concept> type;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "fhir_organization_addresses", joinColumns = @JoinColumn(name = "organization_id"), inverseJoinColumns = @JoinColumn(name = "organization_address_id"))
	private List<FhirOganisationAddress> address;
	
	// The organization id, of which this organization forms a part
	@Column(name = "partof_orgid")
	private String partOfOrgId;
	
	// part-of organization id ,defined by the http://ihe.net/fhir/StructureDefinition/IHE.mCSD.hierarchy.extension
    @Column(name = "mcsd_partof_orgid")
	private String mcsdPartOfOrgId;
	
	//hierarchy-type Coding ,defined by the http://ihe.net/fhir/StructureDefinition/IHE.mCSD.hierarchy.extension
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "hierachy_concept_id", referencedColumnName = "concept_id")
	private Concept hierarchyType;
	
	@Column(name = "name", nullable = false, length = 255)
	private String name;
	
	@Column(name = "description", length = 255)
	private String description;	
}
