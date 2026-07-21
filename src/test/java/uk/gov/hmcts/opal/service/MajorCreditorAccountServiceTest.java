package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.service.proxy.MajorCreditorAccountProxy;

@ExtendWith(MockitoExtension.class)
class MajorCreditorAccountServiceTest {

    @Mock
    private UserStateService userStateService;

    @Mock
    private MajorCreditorAccountProxy majorCreditorAccountProxy;

    @InjectMocks
    private MajorCreditorAccountService majorCreditorAccountService;

    @Test
    void getAtAGlance_authorisedUserDelegatesToProxy() {
        UserState userState = mock(UserState.class);
        GetMajorCreditorAccountAtAGlanceResponse response = new GetMajorCreditorAccountAtAGlanceResponse();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(majorCreditorAccountProxy.getAtAGlance(123L)).thenReturn(response);

        GetMajorCreditorAccountAtAGlanceResponse result = majorCreditorAccountService.getAtAGlance(123L);

        assertEquals(response, result);
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(majorCreditorAccountProxy).getAtAGlance(123L);
    }

    @Test
    void getAtAGlance_withoutSearchAndViewAccountPermissionThrowsForbidden() {
        UserState userState = mock(UserState.class);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);

        PermissionNotAllowedException exception = assertThrows(
            PermissionNotAllowedException.class,
            () -> majorCreditorAccountService.getAtAGlance(123L)
        );

        assertThat(exception.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(majorCreditorAccountProxy);
    }

    @Test
    void getHeaderSummary_authorisedUserDelegatesToProxy() {
        UserState userState = mock(UserState.class);
        GetMajorCreditorAccountHeaderSummaryResponse response = responseWithBusinessUnit((short) 77);

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(userState.hasBusinessUnitUserWithPermission((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(true);
        when(majorCreditorAccountProxy.getHeaderSummary(123L)).thenReturn(response);

        GetMajorCreditorAccountHeaderSummaryResponse result =
            majorCreditorAccountService.getHeaderSummary(123L);

        assertEquals(response, result);
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).hasBusinessUnitUserWithPermission((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(majorCreditorAccountProxy).getHeaderSummary(123L);
    }

    @Test
    void getHeaderSummary_withoutSearchAndViewAccountPermissionThrowsForbidden() {
        UserState userState = mock(UserState.class);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);

        PermissionNotAllowedException exception = assertThrows(
            PermissionNotAllowedException.class,
            () -> majorCreditorAccountService.getHeaderSummary(123L)
        );

        assertThat(exception.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(majorCreditorAccountProxy);
    }

    @Test
    void getHeaderSummary_permissionInDifferentBusinessUnitThrowsForbidden() {
        UserState userState = mock(UserState.class);
        GetMajorCreditorAccountHeaderSummaryResponse response = responseWithBusinessUnit((short) 77);

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(userState.hasBusinessUnitUserWithPermission((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);
        when(majorCreditorAccountProxy.getHeaderSummary(123L)).thenReturn(response);

        PermissionNotAllowedException exception = assertThrows(
            PermissionNotAllowedException.class,
            () -> majorCreditorAccountService.getHeaderSummary(123L)
        );

        assertThat(exception.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        assertThat(exception.getBusinessUnitId()).isEqualTo((short) 77);
        verify(majorCreditorAccountProxy).getHeaderSummary(123L);
    }

    @Test
    void getHistory_authorisedUserDelegatesToProxy() {
        UserState userState = mock(UserState.class);
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        List<String> itemTypes = List.of("financial");
        GetMajorCreditorHistoryResponse response = GetMajorCreditorHistoryResponse.builder().build();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(majorCreditorAccountProxy.getHistory(123L, dateFrom, dateTo, itemTypes)).thenReturn(response);

        GetMajorCreditorHistoryResponse result =
            majorCreditorAccountService.getHistory(123L, dateFrom, dateTo, itemTypes);

        assertEquals(response, result);
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(majorCreditorAccountProxy).getHistory(123L, dateFrom, dateTo, itemTypes);
    }

    @Test
    void getHistory_withoutSearchAndViewAccountPermissionThrowsForbidden() {
        UserState userState = mock(UserState.class);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);

        PermissionNotAllowedException exception = assertThrows(
            PermissionNotAllowedException.class,
            () -> majorCreditorAccountService.getHistory(123L, null, null, null)
        );

        assertThat(exception.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(majorCreditorAccountProxy);
    }

    @Test
    void getHistory_whenDateFromIsAfterDateToThrowsBadRequestBeforeProxyCall() {
        UserState userState = mock(UserState.class);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> majorCreditorAccountService.getHistory(
                123L,
                LocalDate.of(2026, 1, 31),
                LocalDate.of(2026, 1, 1),
                null
            )
        );

        assertEquals("dateFrom must be on or before dateTo", exception.getMessage());
        verifyNoInteractions(majorCreditorAccountProxy);
    }

    @Test
    void getHistory_whenItemTypesContainUnsupportedValueThrowsBadRequestBeforeProxyCall() {
        UserState userState = mock(UserState.class);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> majorCreditorAccountService.getHistory(123L, null, null, List.of("amendment"))
        );

        assertEquals("itemTypes must contain only financial, note", exception.getMessage());
        verifyNoInteractions(majorCreditorAccountProxy);
    }

    private GetMajorCreditorAccountHeaderSummaryResponse responseWithBusinessUnit(Short businessUnitId) {
        GetMajorCreditorAccountHeaderSummaryResponse response =
            new GetMajorCreditorAccountHeaderSummaryResponse();
        response.setBusinessUnitDetails(new BusinessUnitSummaryCommon().businessUnitId(businessUnitId));
        return response;
    }
}
