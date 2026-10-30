package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.MANAGE_SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.common.user.authorisation.model.Domain.FINES;
import static uk.gov.hmcts.opal.util.PermissionUtil.checkBusinessUnitUserHasPermission;
import static uk.gov.hmcts.opal.util.PermissionUtil.getRequiredBusinessUnitUser;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.generated.model.SuspenseItemTransactionRequestSuspenseItems;
import uk.gov.hmcts.opal.generated.model.SuspenseItemTransactionResponseSuspenseItems;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@RequiredArgsConstructor
public class SuspenseItemTransactionService {

    private final UserStateService userStateService;

    public SuspenseItemTransactionResponseSuspenseItems createTransaction(
        Long id, Short businessUnitId, String ifMatch, SuspenseItemTransactionRequestSuspenseItems request) {

        DomainBusinessUnitUsers businessUnitUsers = userStateService.getUserStateFromSecurityContext()
            .getDomainBusinessUnitUsers(FINES);
        checkBusinessUnitUserHasPermission(
            getRequiredBusinessUnitUser(businessUnitUsers, businessUnitId), MANAGE_SUSPENSE_ITEMS);

        // PO-10406: replace this guard with atomic version checking and transaction creation once
        // the database contracts from PO-10349 and PO-10350 have been merged and verified.
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "Suspense item transaction creation is not yet available");
    }
}
