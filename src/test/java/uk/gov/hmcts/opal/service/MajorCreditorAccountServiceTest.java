package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService;

@ExtendWith(MockitoExtension.class)
class MajorCreditorAccountServiceTest {

    @Mock
    private UserStateService userStateService;

    @Mock
    private LegacyMajorCreditorAccountService legacyMajorCreditorAccountService;

    @InjectMocks
    private MajorCreditorAccountService majorCreditorAccountService;

    @Test
    void getHeaderSummary_authorisedUserDelegatesToLegacyService() {
        UserState userState = mock(UserState.class);
        GetMajorCreditorAccountHeaderSummaryResponse response = responseWithBusinessUnit("77");

        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(userState.hasBusinessUnitUserWithPermission((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(true);
        when(legacyMajorCreditorAccountService.getHeaderSummary(123L)).thenReturn(response);

        GetMajorCreditorAccountHeaderSummaryResponse result =
            majorCreditorAccountService.getHeaderSummary(123L);

        assertEquals(response, result);
        verify(userStateService).checkForAuthorisedUser();
        verify(userState).hasBusinessUnitUserWithPermission((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(legacyMajorCreditorAccountService).getHeaderSummary(123L);
    }

    @Test
    void getHeaderSummary_withoutSearchAndViewAccountPermissionThrowsForbidden() {
        UserState userState = mock(UserState.class);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);
        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);

        PermissionNotAllowedException exception = assertThrows(
            PermissionNotAllowedException.class,
            () -> majorCreditorAccountService.getHeaderSummary(123L)
        );

        assertThat(exception.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(legacyMajorCreditorAccountService);
    }

    @Test
    void getHeaderSummary_permissionInDifferentBusinessUnitThrowsForbidden() {
        UserState userState = mock(UserState.class);
        GetMajorCreditorAccountHeaderSummaryResponse response = responseWithBusinessUnit("77");

        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(userState.hasBusinessUnitUserWithPermission((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);
        when(legacyMajorCreditorAccountService.getHeaderSummary(123L)).thenReturn(response);

        PermissionNotAllowedException exception = assertThrows(
            PermissionNotAllowedException.class,
            () -> majorCreditorAccountService.getHeaderSummary(123L)
        );

        assertThat(exception.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        assertThat(exception.getBusinessUnitId()).isEqualTo((short) 77);
        verify(legacyMajorCreditorAccountService).getHeaderSummary(123L);
    }

    private GetMajorCreditorAccountHeaderSummaryResponse responseWithBusinessUnit(String businessUnitId) {
        GetMajorCreditorAccountHeaderSummaryResponse response =
            new GetMajorCreditorAccountHeaderSummaryResponse();
        response.setBusinessUnitDetails(new BusinessUnitSummaryCommon().businessUnitId(businessUnitId));
        return response;
    }
}
