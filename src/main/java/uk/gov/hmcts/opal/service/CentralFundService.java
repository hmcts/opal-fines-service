package uk.gov.hmcts.opal.service;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.CentralFundResponse;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.GetCentralFundByBusinessUnit200Response;
import uk.gov.hmcts.opal.generated.model.GetCentralFundByBusinessUnit200ResponseMajorCreditor;
import uk.gov.hmcts.opal.repository.CentralFundProjection;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;

@Service
@Slf4j(topic = "opal.CentralFundService")
@RequiredArgsConstructor
public class CentralFundService {

    private final UserStateService userStateService;
    private final CreditorAccountRepository creditorAccountRepository;

    @Transactional(readOnly = true)
    public CentralFundResponse getCentralFundByBusinessUnit(Integer businessUnitId, String authHeaderValue) {
        log.debug(":getCentralFundByBusinessUnit: businessUnitId={}", businessUnitId);

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        Short shortBusinessUnitId = toBusinessUnitId(businessUnitId);
        CentralFundProjection centralFund = creditorAccountRepository
            .findCentralFundByBusinessUnitId(shortBusinessUnitId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Central fund not found for business unit: " + businessUnitId
            ));

        return CentralFundResponse.builder()
            .payload(toPayload(centralFund))
            .version(toVersion(centralFund))
            .build();
    }

    private Short toBusinessUnitId(Integer businessUnitId) {
        if (businessUnitId == null) {
            throw new IllegalArgumentException("Business unit id must be provided");
        }
        if (businessUnitId < Short.MIN_VALUE || businessUnitId > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Business unit id is out of range: " + businessUnitId);
        }
        return businessUnitId.shortValue();
    }

    private GetCentralFundByBusinessUnit200Response toPayload(CentralFundProjection centralFund) {
        return GetCentralFundByBusinessUnit200Response.builder()
            .majorCreditor(GetCentralFundByBusinessUnit200ResponseMajorCreditor.builder()
                .creditorAccountId(centralFund.getCreditorAccountId())
                .accountNumber(centralFund.getAccountNumber())
                .name(centralFund.getName())
                .build())
            .businessUnitDetails(BusinessUnitSummaryCommon.builder()
                .businessUnitId(String.valueOf(centralFund.getBusinessUnitId()))
                .businessUnitName(centralFund.getBusinessUnitName())
                .welshSpeaking(toWelshSpeaking(centralFund.getWelshLanguage()))
                .build())
            .build();
    }

    private String toWelshSpeaking(Boolean welshLanguage) {
        return Boolean.TRUE.equals(welshLanguage) ? "Y" : "N";
    }

    private BigInteger toVersion(CentralFundProjection centralFund) {
        return centralFund.getVersionNumber() == null ? null : BigInteger.valueOf(centralFund.getVersionNumber());
    }
}
