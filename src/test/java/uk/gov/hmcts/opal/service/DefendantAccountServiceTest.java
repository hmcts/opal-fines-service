package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.opal.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantAccountServiceTest {

    @Mock
    private DefendantAccountServiceProxy defendantAccountServiceProxy;

    @Mock
    private UserStateService userStateService;

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
}
