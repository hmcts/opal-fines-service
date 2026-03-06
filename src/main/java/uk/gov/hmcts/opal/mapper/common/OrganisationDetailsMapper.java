package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrganisationDetailsMapper {

    OrganisationDetails toDto(
        uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails legacy
    );

    @Mapping(
        target = "sequenceNumber",
        expression = "java(legacy.getSequenceNumber() == null ? null : Integer.valueOf(legacy.getSequenceNumber()))"
    )
    OrganisationAlias toDto(uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias legacy);

    @Mapping(target = "organisationAliases", ignore = true)
    OrganisationDetailsCommon toOrganisationDetailsCommon(PartyEntity party);
}

