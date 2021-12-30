/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.facilityregistry.api.dao;

import javax.annotation.Nonnull;

import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.facilityregistry.model.FhirOrganization;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.util.PrivilegeConstants;

public interface FhirOrganizationDao extends FhirDao<FhirOrganization> {
    
    @Override
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    FhirOrganization get(@Nonnull String uuid);
    
    @Override
    @Authorized(PrivilegeConstants.MANAGE_LOCATIONS)
    FhirOrganization createOrUpdate(@Nonnull FhirOrganization newEntry);
    
    @Override
    @Authorized(PrivilegeConstants.MANAGE_LOCATIONS)
    FhirOrganization delete(@Nonnull String uuid);
    
    @Override
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams);
    
    @Override
    @Authorized(PrivilegeConstants.GET_LOCATIONS)
    List<FhirOrganization> getSearchResults(@Nonnull SearchParameterMap theParams, @Nonnull List<String> resourceUuids);
}
