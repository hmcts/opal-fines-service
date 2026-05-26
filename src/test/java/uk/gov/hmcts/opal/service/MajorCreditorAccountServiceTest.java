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
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService;

@ExtendWith(MockitoExtension.class)
class MajorCreditorAccountServiceTest {

    private static final String AUTH_HEADER = "Bearer some_value";

    @Mock
    private UserStateService userStateService;

    @Mock
    private LegacyMajorCreditorAccountService legacyMajorCreditorAccountService;

    @InjectMocks
    private MajorCreditorAccountService majorCreditorAccountService;

    @Test
    void getHeaderSummary_authorisedUserDelegatesToLegacyService() {
        GetMajorCreditorAccountHeaderSummaryResponse response =
            new GetMajorCreditorAccountHeaderSummaryResponse();

        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(UserStateUtil.allFinesPermissionUser());
        when(legacyMajorCreditorAccountService.getHeaderSummary(123L)).thenReturn(response);

        GetMajorCreditorAccountHeaderSummaryResponse result =
            majorCreditorAccountService.getHeaderSummary(123L, AUTH_HEADER);

        assertEquals(response, result);
        verify(userStateService).checkForAuthorisedUser(AUTH_HEADER);
        verify(legacyMajorCreditorAccountService).getHeaderSummary(123L);
    }

    @Test
    void getHeaderSummary_withoutSearchAndViewAccountPermissionThrowsForbidden() {
        UserState userState = mock(UserState.class);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER)).thenReturn(userState);

        PermissionNotAllowedException exception = assertThrows(
            PermissionNotAllowedException.class,
            () -> majorCreditorAccountService.getHeaderSummary(123L, AUTH_HEADER)
        );

        assertThat(exception.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(legacyMajorCreditorAccountService);
    }
}
