package uk.gov.hmcts.opal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.CentralFundResponse;
import uk.gov.hmcts.opal.mapper.CentralFundMapper;
import uk.gov.hmcts.opal.repository.CentralFundProjection;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;

@Service
@Slf4j(topic = "opal.CentralFundService")
@RequiredArgsConstructor
public class CentralFundService {

    private final UserStateService userStateService;
    private final CreditorAccountRepository creditorAccountRepository;
    private final CentralFundMapper centralFundMapper;

    @Transactional(readOnly = true)
    public CentralFundResponse getCentralFundByBusinessUnit(int businessUnitId) {
        log.debug(":getCentralFundByBusinessUnit: businessUnitId={}", businessUnitId);

        UserState userState = userStateService.getUserStateV1FromSecurityContext();
        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        Short shortBusinessUnitId = toBusinessUnitId(businessUnitId);
        CentralFundProjection centralFund = creditorAccountRepository
            .findCentralFundByBusinessUnitId(shortBusinessUnitId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Central fund not found for business unit: " + businessUnitId
            ));

        return centralFundMapper.toCentralFundResponse(centralFund);
    }

    private Short toBusinessUnitId(int businessUnitId) {
        if (businessUnitId < Short.MIN_VALUE || businessUnitId > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Business unit id is out of range: " + businessUnitId);
        }
        return (short) businessUnitId;
    }

}
