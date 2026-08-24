package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;
import uk.gov.hmcts.opal.generated.model.PartyResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.RemoveDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.RemoveDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.generated.model.AddPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.dto.request.RemoveDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.mapper.legacy.DefendantAccountPartyLegacyResponseMapper;
import uk.gov.hmcts.opal.mapper.legacy.RemoveDefendantAccountPartyLegacyResponseMapper;
import uk.gov.hmcts.opal.service.iface.DefendantAccountPartyServiceInterface;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDefendantAccountPartyService")
public class LegacyDefendantAccountPartyService implements DefendantAccountPartyServiceInterface {

    public static final String GET_DEFENDANT_ACCOUNT_PARTY = "getDefendantAccountParty";
    public static final String REPLACE_DEFENDANT_ACCOUNT_PARTY = "replaceDefendantAccountParty";
    public static final String ADD_DEFENDANT_ACCOUNT_PARTY = "addDefendantAccountParty";
    public static final String REMOVE_DEFENDANT_ACCOUNT_PARTY = "removeDefendantAccountParty";

    /* ---- Services ---- */
    private final GatewayService gatewayService;

    /* ---- Mappers ---- */
    private final DefendantAccountPartyLegacyResponseMapper defendantAccountPartyLegacyResponseMapper;
    private final RemoveDefendantAccountPartyLegacyResponseMapper removeDAPLegacyResponseMapper;

    @Override
    public PartyResponseDefendantAccount getDefendantAccountParty(Long defendantAccountId,
                                                                     Long defendantAccountPartyId) {
        log.debug(":getDefendantAccountParty: Legacy call for accountId={}, partyId={}",
            defendantAccountId, defendantAccountPartyId);

        GetDefendantAccountPartyLegacyRequest req = GetDefendantAccountPartyLegacyRequest.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .defendantAccountPartyId(String.valueOf(defendantAccountPartyId))
            .build();

        Response<GetDefendantAccountPartyLegacyResponse> response = gatewayService.postToGateway(
            GET_DEFENDANT_ACCOUNT_PARTY,
            GetDefendantAccountPartyLegacyResponse.class,
            req,
            null
        );

        if (response.isError()) {
            log.error(":getDefendantAccountParty: Legacy error HTTP {}", response.code);
            if (response.isException()) {
                log.error(":getDefendantAccountParty: exception:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":getDefendantAccountParty: legacy failure body:\n{}", response.body);
            }
            return PartyResponseDefendantAccount.builder().build();
        } else if (response.isSuccessful()) {
            log.info(":getDefendantAccountParty: Legacy success.");
        }

        return defendantAccountPartyLegacyResponseMapper.toGeneratedResponse(response.responseEntity);
    }

    @Override
    public PartyResponseDefendantAccount replaceDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId,
        DefendantAccountParty defendantAccountParty, String ifMatch, String businessUnitId, String postedBy,
        String postedByName, String businessUnitUserId) {

        LegacyReplaceDefendantAccountPartyRequest req = LegacyReplaceDefendantAccountPartyRequest.builder()
            .version(VersionUtils.extractBigInteger(ifMatch))
            .defendantAccountId(defendantAccountId)
            .businessUnitId(businessUnitId)
            .businessUnitUserId(businessUnitUserId)
            .defendantAccountParty(defendantAccountParty)
            .build();

        Response<LegacyReplaceDefendantAccountPartyResponse> response = gatewayService.postToGateway(
            REPLACE_DEFENDANT_ACCOUNT_PARTY,
            LegacyReplaceDefendantAccountPartyResponse.class,
            req,
            null
        );

        if (response.isError()) {
            log.error(":replaceDefendantAccountParty: Legacy error HTTP {}", response.code);
            if (response.isException()) {
                log.error(":replaceDefendantAccountParty: exception:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":replaceDefendantAccountParty: legacy failure body:\n{}", response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":replaceDefendantAccountParty: Legacy success.");
        }

        return defendantAccountPartyLegacyResponseMapper.toGeneratedResponse(response.responseEntity);
    }

    @Override
    public PartyResponseDefendantAccount addDefendantAccountParty(Long defendantAccountId,
                                                                     String businessUnitId,
                                                                     String businessUnitUserId,
                                                                     String postedBy,
                                                                     String postedByName,
                                                                     String ifMatch,
                                                                     AddPartyRequestDefendantAccount request) {

        AddDefendantAccountPartyLegacyRequest req = AddDefendantAccountPartyLegacyRequest.builder()
            .version(VersionUtils.extractBigInteger(ifMatch))
            .defendantAccountId(defendantAccountId)
            .businessUnitId(businessUnitId)
            .businessUnitUserId(businessUnitUserId)
            .defendantAccountParty(request.getDefendantAccountParty())
            .build();

        Response<AddDefendantAccountPartyLegacyResponse> response = gatewayService.postToGateway(
            ADD_DEFENDANT_ACCOUNT_PARTY,
            AddDefendantAccountPartyLegacyResponse.class,
            req,
            null
        );

        if (response.isError()) {
            log.error(":addDefendantAccountParty: Legacy error HTTP {}", response.code);
            if (response.isException()) {
                log.error(":addDefendantAccountParty: exception:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":addDefendantAccountParty: legacy failure body:\n{}", response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":addDefendantAccountParty: Legacy success.");
        }

        if (response.responseEntity == null) {
            return new PartyResponseDefendantAccount();
        }

        return defendantAccountPartyLegacyResponseMapper.toGeneratedResponse(response.responseEntity);
    }

    @Override
    public RemoveDefendantAccountPartyResponse removeDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId, Short businessUnitId, String businessUnitUserId, String postedBy,
        String postedByName, String ifMatch, RemoveDefendantAccountPartyRequest request) {
        RemoveDefendantAccountPartyLegacyRequest req = RemoveDefendantAccountPartyLegacyRequest.builder()
            .version(VersionUtils.extractBigInteger(ifMatch))
            .defendantAccountId(defendantAccountId)
            .businessUnitId(businessUnitId)
            .businessUnitUserId(businessUnitUserId)
            .defendantAccountPartyId(defendantAccountPartyId)
            .build();

        Response<RemoveDefendantAccountPartyLegacyResponse> response = gatewayService.postToGateway(
            REMOVE_DEFENDANT_ACCOUNT_PARTY,
            RemoveDefendantAccountPartyLegacyResponse.class,
            req,
            null);

        if (response.isError()) {
            log.error(":removeDefendantAccountParty: Legacy error HTTP {}", response.code);
            if (response.isException()) {
                log.error(":removeDefendantAccountParty: exception:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":removeDefendantAccountParty: legacy failure body:\n{}", response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":removeDefendantAccountParty: Legacy success.");
        }

        return removeDAPLegacyResponseMapper.toRemoveDefendantAccountPartyResponse(response.responseEntity);
    }
}
