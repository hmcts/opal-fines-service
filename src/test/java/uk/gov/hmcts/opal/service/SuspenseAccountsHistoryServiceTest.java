package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.MANAGE_SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.VIEW_SUSPENSE_ITEMS;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.SuspenseItemHistoryCommonStrict;
import uk.gov.hmcts.opal.service.proxy.SuspenseAccountsHistoryServiceProxy;

@ExtendWith(MockitoExtension.class)
class SuspenseAccountsHistoryServiceTest {

    @Mock
    private UserStateService userStateService;

    @Mock
    private SuspenseAccountsHistoryServiceProxy proxy;

    @InjectMocks
    private SuspenseAccountsHistoryService service;

    @ParameterizedTest
    @EnumSource(value = FinesPermission.class, names = { "VIEW_SUSPENSE_ITEMS", "MANAGE_SUSPENSE_ITEMS" })
    void getHistory_allowsEitherPermission(FinesPermission permission) {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userWith(permission));
        SuspenseItemHistoryCommonStrict expected =
            new SuspenseItemHistoryCommonStrict().businessUnitId(123).suspenseItemId(1);
        when(proxy.getSuspenseItemHistory(123L, 12345L)).thenReturn(expected);

        SuspenseItemHistoryCommonStrict result = service.getSuspenseItemHistory(123L, 12345L);

        assertEquals(expected, result);
        verify(proxy).getSuspenseItemHistory(123L, 12345L);
    }

    @ParameterizedTest
    @EnumSource(value = FinesPermission.class, mode = EnumSource.Mode.EXCLUDE,
        names = { "VIEW_SUSPENSE_ITEMS", "MANAGE_SUSPENSE_ITEMS" })
    void getHistory_rejectsUnrelatedPermission(FinesPermission permission) {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userWith(permission));

        assertAccessDenied();
    }

    @Test
    void getHistory_rejectsUserWithoutBusinessUnits() {
        UserState user = UserState.builder().userId(1L).userName("test-user").businessUnitUser(Set.of()).build();
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(user);

        assertAccessDenied();
    }

    private void assertAccessDenied() {
        PermissionNotAllowedException exception = assertThrows(PermissionNotAllowedException.class,
            () -> service.getSuspenseItemHistory(123L, 12345L));

        assertArrayEquals(new FinesPermission[] { VIEW_SUSPENSE_ITEMS, MANAGE_SUSPENSE_ITEMS },
            exception.getPermission());
        verifyNoInteractions(proxy);
    }

    private UserState userWith(FinesPermission permission) {
        BusinessUnitUser businessUnitUser = BusinessUnitUser.builder()
            .businessUnitId((short) 123)
            .permissions(Set.of(permission.toCommonPermission()))
            .build();
        return UserState.builder()
            .userId(1L)
            .userName("test-user")
            .businessUnitUser(Set.of(businessUnitUser))
            .build();
    }
}
