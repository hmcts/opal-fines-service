package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.MANAGE_SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.VIEW_SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.common.user.authorisation.model.Domain.FINES;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissions;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionsFor;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.generated.model.SuspenseItemTransactionRequestSuspenseItems;
import uk.gov.hmcts.opal.generated.model.SuspenseItemTransactionRequestSuspenseItems.TransactionTypeEnum;
import uk.gov.hmcts.opal.service.UserStateService;

@ExtendWith(MockitoExtension.class)
class SuspenseItemTransactionServiceTest {

    private static final short BUSINESS_UNIT_ID = 78;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private SuspenseItemTransactionService service;

    @Test
    void rejectsViewOnlyPermission() {
        userState(permissions(BUSINESS_UNIT_ID, permissionsFor(VIEW_SUSPENSE_ITEMS)));

        assertThatThrownBy(this::createTransaction).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsPermissionInAnotherBusinessUnit() {
        userState(permissions((short) 77, permissionsFor(MANAGE_SUSPENSE_ITEMS)));

        assertThatThrownBy(this::createTransaction).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsUserWithoutBusinessUnits() {
        userState();

        assertThatThrownBy(this::createTransaction).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void remainsUnavailableUntilDatabaseDependenciesAreImplemented() {
        userState(permissions(BUSINESS_UNIT_ID, permissionsFor(MANAGE_SUSPENSE_ITEMS)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, this::createTransaction);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    private void createTransaction() {
        service.createTransaction(1L, BUSINESS_UNIT_ID, "\"2\"",
            new SuspenseItemTransactionRequestSuspenseItems().transactionType(TransactionTypeEnum.MS).noteText("Note"));
    }

    private void userState(BusinessUnitUser... users) {
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(UserStateV2.builder()
            .domains(Map.of(FINES, DomainBusinessUnitUsers.builder().businessUnitUsers(List.of(users)).build()))
            .build());
    }
}
