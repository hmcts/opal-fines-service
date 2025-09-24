package uk.gov.hmcts.opal.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
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
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());

        // Act
        PostMinorCreditorAccountsSearchResponse result =
            minorCreditorService.searchMinorCreditors(
                (MinorCreditorSearch.builder().build()), "authHeaderValue");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testPermissionNotAllowedException() {
        // Arrange
        UserState noPermissionUser = mock(UserState.class);
        when(noPermissionUser.anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionUser);

        // Act & Assert
        assertThrows(
            PermissionNotAllowedException.class, () ->
            minorCreditorService.searchMinorCreditors(MinorCreditorSearch.builder().build(), "authHeaderValue")
        );
    }


}
