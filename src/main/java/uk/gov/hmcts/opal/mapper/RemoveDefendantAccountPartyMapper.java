package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.request.RemoveDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyResponseDefendantAccount;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RemoveDefendantAccountPartyMapper {

    @Mapping(target = "version", ignore = true)
    RemoveDefendantAccountPartyRequest toServiceRequest(
        RemoveDefendantAccountPartyRequestDefendantAccount request);

    RemoveDefendantAccountPartyResponseDefendantAccount toGeneratedResponse(
        RemoveDefendantAccountPartyResponse response);
}
