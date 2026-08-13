package uk.gov.hmcts.opal.mapper;

import java.math.BigInteger;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.request.RemoveDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyResponseDefendantAccount;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RemoveDefendantAccountPartyMapper {

    default RemoveDefendantAccountPartyRequest toServiceRequest(
        RemoveDefendantAccountPartyRequestDefendantAccount request) {
        if (request == null) {
            return null;
        }

        String partyId = request.getPartyDetails() == null
            ? request.getDefendantAccountPartyId()
            : request.getPartyDetails().getPartyId();

        return RemoveDefendantAccountPartyRequest.builder()
            .defendantAccountPartyId(partyId == null ? null : Long.valueOf(partyId))
            .version(request.getVersion() == null ? null : BigInteger.valueOf(request.getVersion()))
            .build();
    }

    RemoveDefendantAccountPartyResponseDefendantAccount toGeneratedResponse(
        RemoveDefendantAccountPartyResponse response);
}
