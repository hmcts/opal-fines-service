package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IndividualDetailsMapper {

    @Mapping(source = "firstNames", target = "forenames")
    @Mapping(
        target = "dateOfBirth",
        expression = "java(legacy.getDateOfBirth() == null ? null : legacy.getDateOfBirth().toString())"
    )
    IndividualDetails toDto(
        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails legacy
    );

    @Mapping(
        target = "sequenceNumber",
        expression = "java(legacy.getSequenceNumber() == null ? null : Integer.valueOf(legacy.getSequenceNumber()))"
    )
    IndividualAlias toDto(uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias legacy);
}
