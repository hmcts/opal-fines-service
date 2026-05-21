package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganisationDetailsCommonMapper {

    OrganisationDetailsLegacy toLegacy(OrganisationDetailsCommon organisationDetails);
}
