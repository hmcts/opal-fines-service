package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permissions;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantAccountServiceTest {

    @Mock
    private DefendantAccountServiceProxy defendantAccountServiceProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private UserState userState;

    @InjectMocks
    private DefendantAccountService defendantAccountService;

    @Test
    void testGetHeaderSummary() {
        // Arrange
        DefendantAccountHeaderSummary headerSummary = DefendantAccountHeaderSummary.builder().build();

        when(defendantAccountServiceProxy.getHeaderSummary(anyLong())).thenReturn(headerSummary);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());
        // Act
        DefendantAccountHeaderSummary result = defendantAccountService.getHeaderSummary(1L, "authHeaderValue");

        // Assert
        assertNotNull(result);
    }


    @Test
    void getPaymentTerms_whenUserHasPermission_returnsProxyResult() {
        // arrange
        Long defendantAccountId = 77L;
        String authHeader = "Bearer abc";
        GetDefendantAccountPaymentTermsResponse proxyResponse = new GetDefendantAccountPaymentTermsResponse();
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(defendantAccountServiceProxy.getPaymentTerms(defendantAccountId)).thenReturn(proxyResponse);

        // act
        GetDefendantAccountPaymentTermsResponse result =
            defendantAccountService.getPaymentTerms(defendantAccountId, authHeader);

        // assert
        assertSame(proxyResponse, result, "Should return exactly the proxy response");

        // verify interactions
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS);
        verify(defendantAccountServiceProxy).getPaymentTerms(defendantAccountId);
        verifyNoMoreInteractions(userStateService, userState, defendantAccountServiceProxy);
    }

    @Test
    void getPaymentTerms_whenUserLacksPermission_throwsPermissionNotAllowed() {
        // arrange
        Long defendantAccountId = 77L;
        String authHeader = "Bearer abc";
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        // act + assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountService.getPaymentTerms(defendantAccountId, authHeader)
        );
        assertTrue(
            ex.getMessage() == null || ex.getMessage().contains(Permissions.SEARCH_AND_VIEW_ACCOUNTS.name()),
            "Exception should mention the denied permission"
        );

        // proxy must not be called
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(defendantAccountServiceProxy);
        verifyNoMoreInteractions(userStateService, userState);
    }

    @Test
    void testGetHeaderSummary_forbiddenWhenUserHasNoPermission() {
        UserState user = UserState.builder()
            .userId(456L)
            .userName("noperms")
            .businessUnitUser(java.util.Collections.emptySet())
            .build();
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        Assertions.assertThrows(PermissionNotAllowedException.class, () -> {
            defendantAccountService.getHeaderSummary(1L, "authHeaderValue");
        });
    }

    @Test
    void testGetHeaderSummary_happyPath_proxyCalled() {
        UserState userWithPerm = UserState.builder()
            .userId(789L)
            .userName("hasperm")
            .businessUnitUser(java.util.Set.of(
                BusinessUnitUser.builder()
                    .businessUnitUserId("BU1")
                    .businessUnitId((short)77)
                    .permissions(java.util.Set.of(
                        new Permission(
                            Permissions.SEARCH_AND_VIEW_ACCOUNTS.id,
                            Permissions.SEARCH_AND_VIEW_ACCOUNTS.description
                        )
                    ))
                    .build()
            ))
            .build();

        DefendantAccountHeaderSummary expected = DefendantAccountHeaderSummary.builder().accountNumber("X123").build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userWithPerm);
        when(defendantAccountServiceProxy.getHeaderSummary(anyLong())).thenReturn(expected);

        DefendantAccountHeaderSummary result = defendantAccountService.getHeaderSummary(1L, "authHeaderValue");

        assertNotNull(result);
        assertEquals("X123", result.getAccountNumber());
        verify(defendantAccountServiceProxy).getHeaderSummary(1L);
    }

    @Test
    void testSearchDefendantAccounts_forbiddenWhenUserHasNoPermission() {
        UserState user = UserState.builder()
            .userId(456L)
            .userName("noperms")
            .businessUnitUser(java.util.Collections.emptySet())
            .build();
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        AccountSearchDto dto = AccountSearchDto.builder().build();
        Assertions.assertThrows(PermissionNotAllowedException.class, () -> {
            defendantAccountService.searchDefendantAccounts(dto, "authHeaderValue");
        });
    }

    @Test
    void testSearchDefendantAccounts_happyPath_proxyCalled() {
        UserState userWithPerm = UserState.builder()
            .userId(789L)
            .userName("hasperm")
            .businessUnitUser(java.util.Set.of(
                BusinessUnitUser.builder()
                    .businessUnitUserId("BU1")
                    .businessUnitId((short) 77)
                    .permissions(java.util.Set.of(
                        Permission.builder()
                            .permissionId(Permissions.SEARCH_AND_VIEW_ACCOUNTS.id)
                            .permissionName(Permissions.SEARCH_AND_VIEW_ACCOUNTS.name())
                            .build()
                    ))
                    .build()
            ))
            .build();

        DefendantAccountSearchResultsDto expected = DefendantAccountSearchResultsDto.builder().count(1).build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userWithPerm);
        when(defendantAccountServiceProxy.searchDefendantAccounts(any(AccountSearchDto.class))).thenReturn(expected);

        AccountSearchDto dto = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto result = defendantAccountService.searchDefendantAccounts(dto,
            "authHeaderValue");

        assertNotNull(result);
        assertEquals(1, result.getCount().intValue());
        verify(defendantAccountServiceProxy).searchDefendantAccounts(dto);
    }

    @Test
    void getAtAGlance_whenUserHasPermission_returnsProxyResult() {
        // arrange
        Long defendantAccountId = 77L;
        String authHeader = "Bearer abc";
        DefendantAccountAtAGlanceResponse proxyResponse = new DefendantAccountAtAGlanceResponse();
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(defendantAccountServiceProxy.getAtAGlance(defendantAccountId)).thenReturn(proxyResponse);

        // act
        DefendantAccountAtAGlanceResponse result =
            defendantAccountService.getAtAGlance(defendantAccountId, authHeader);

        // assert
        assertSame(proxyResponse, result, "Should return exactly the proxy response");

        // verify interactions
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS);
        verify(defendantAccountServiceProxy).getAtAGlance(defendantAccountId);
        verifyNoMoreInteractions(userStateService, userState, defendantAccountServiceProxy);
    }

    @Test
    void getAtAGlance_whenUserLacksPermission_throwsPermissionNotAllowed() {
        // arrange
        Long defendantAccountId = 77L;
        String authHeader = "Bearer abc";
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        // act + assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountService.getAtAGlance(defendantAccountId, authHeader)
        );
        assertTrue(
            ex.getMessage() == null || ex.getMessage().contains(Permissions.SEARCH_AND_VIEW_ACCOUNTS.name()),
            "Exception should mention the denied permission"
        );

        // proxy must not be called
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(defendantAccountServiceProxy);
        verifyNoMoreInteractions(userStateService, userState);
    }
}
