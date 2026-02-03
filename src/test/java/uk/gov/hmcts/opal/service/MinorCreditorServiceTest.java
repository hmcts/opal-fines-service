package uk.gov.hmcts.opal.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.service.proxy.MinorCreditorSearchProxy;

@ExtendWith(MockitoExtension.class)
class MinorCreditorServiceTest {

    @Mock
    UserStateService userStateService;

    @Mock
    MinorCreditorSearchProxy minorCreditorSearchProxy;

    @InjectMocks
    private MinorCreditorService minorCreditorService;

    @Test
    void testPostSearchMinorCreditors() {
        // Arrange
        PostMinorCreditorAccountsSearchResponse postMinorCreditorAccountsSearchResponse =
            PostMinorCreditorAccountsSearchResponse.builder().build();

        when(minorCreditorSearchProxy.searchMinorCreditors(any())).thenReturn(postMinorCreditorAccountsSearchResponse);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allFinesPermissionUser());

        // Act
        PostMinorCreditorAccountsSearchResponse result =
            minorCreditorService.searchMinorCreditors(
                (MinorCreditorSearch.builder().build()), "authHeaderValue");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetMinorCreditorAccountHeaderSummary() {
        // Arrange
        long id = 123L;
        GetMinorCreditorAccountHeaderSummaryResponse response =
            GetMinorCreditorAccountHeaderSummaryResponse.builder().build();

        when(minorCreditorSearchProxy.getHeaderSummary(id)).thenReturn(response);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allFinesPermissionUser());

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse result =
            minorCreditorService.getMinorCreditorAccountHeaderSummary(id, "authHeaderValue");

        // Assert
        assertNotNull(result);
        assertEquals(response, result);
        verify(minorCreditorSearchProxy).getHeaderSummary(eq(id));
    }

    @Test
    void testGetMinorCreditorAccountHeaderSummary_permissionNotAllowed() {
        // Arrange
        UserState noPermissionUser = mock(UserState.class);
        when(noPermissionUser.anyBusinessUnitUserHasPermission(
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionUser);

        // Act & Assert
        assertThrows(
            PermissionNotAllowedException.class, () ->
                minorCreditorService.getMinorCreditorAccountHeaderSummary(123L, "authHeaderValue")
        );
    }

    @Test
    void testPermissionNotAllowedException() {
        // Arrange
        UserState noPermissionUser = mock(UserState.class);
        when(noPermissionUser.anyBusinessUnitUserHasPermission(
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionUser);

        // Act & Assert
        assertThrows(
            PermissionNotAllowedException.class, () ->
            minorCreditorService.searchMinorCreditors(MinorCreditorSearch.builder().build(), "authHeaderValue")
        );
    }


}
