package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyRemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyRemoveDefendantAccountEnforcementHoldResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LegacyRemoveDefendantEnforcementHoldMapper {

    @Mapping(target = "defendantAccountId", expression = "java(String.valueOf(defendantAccountId))")
    @Mapping(
        target = "businessUnitId",
        expression = "java(String.valueOf(businessUnitId))"
    )
    @Mapping(target = "businessUnitUserId", source = "businessUnitUserId")
    @Mapping(
        target = "version",
        expression = "java(uk.gov.hmcts.opal.util.VersionUtils.extractBigInteger(ifMatch))")
    @Mapping(target = "reason", source = "request.reason")
    LegacyRemoveDefendantAccountEnforcementHoldRequest toLegacyRequest(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        RemoveDefendantAccountEnforcementHoldRequest request
    );

    RemoveDefendantAccountEnforcementHoldResponse toOpalResponse(
        LegacyRemoveDefendantAccountEnforcementHoldResponse legacyResponse
    );
}