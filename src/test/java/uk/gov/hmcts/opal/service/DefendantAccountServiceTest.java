package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchReferenceNumberDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchResponseDefendantAccount;
import uk.gov.hmcts.opal.mapper.request.DefendantAccountSearchRequestMapper;
import uk.gov.hmcts.opal.mapper.response.DefendantAccountSearchResponseMapper;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountRequestPayload;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

@ExtendWith(MockitoExtension.class)
class DefendantAccountServiceTest {

    @Mock
    private DefendantAccountServiceProxy defendantAccountServiceProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private UserState userState;

    @Mock
    private OpalDefendantAccountService opalDefendantAccountService;

    @Mock
    private DefendantAccountSearchRequestMapper defendantAccountSearchRequestMapper;

    @Mock
    private DefendantAccountSearchResponseMapper defendantAccountSearchResponseMapper;

    @Mock
    private DefendantAccountSearchRequestValidator defendantAccountSearchRequestValidator;

    @InjectMocks
    private DefendantAccountService defendantAccountService;

    @Test
    void testGetHeaderSummary() {
        // Arrange
        GetDefendantAccountHeaderSummary200Response response = GetDefendantAccountHeaderSummary200Response.builder()
            .accountNumber("X123")
            .hasConsolidatedAccounts(Boolean.FALSE)
            .build();
        DefendantAccountHeaderSummary headerSummary = DefendantAccountHeaderSummary.builder()
            .version(BigInteger.ZERO)
            .response(response)
            .build();

        when(defendantAccountServiceProxy.getHeaderSummary(anyLong())).thenReturn(headerSummary);

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(UserStateUtil.allFinesPermissionUser());
        // Act
        DefendantAccountHeaderSummary result = defendantAccountService.getHeaderSummary(1L);

        // Assert
        assertNotNull(result);
        assertEquals("X123", result.getResponse().getAccountNumber());
        assertFalse(result.getResponse().getHasConsolidatedAccounts());
    }

    @Test
    void testGetHeaderSummary_forbiddenWhenUserHasNoPermission() {
        // Arrange
        UserState user = UserState.builder()
            .userId(456L)
            .userName("noperms")
            .businessUnitUser(java.util.Collections.emptySet())
            .build();
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(user);

        // Act & Assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () ->  defendantAccountService.getHeaderSummary(1L)
        );
        assertThat(ex.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
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
                            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.getId(),
                            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.getDescription()
                        )
                    ))
                    .build()
            ))
            .build();

        GetDefendantAccountHeaderSummary200Response response = GetDefendantAccountHeaderSummary200Response.builder()
            .accountNumber("X123")
            .hasConsolidatedAccounts(Boolean.TRUE)
            .build();
        DefendantAccountHeaderSummary expected = DefendantAccountHeaderSummary.builder()
            .version(BigInteger.ZERO)
            .response(response)
            .build();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userWithPerm);
        when(defendantAccountServiceProxy.getHeaderSummary(anyLong())).thenReturn(expected);

        DefendantAccountHeaderSummary result = defendantAccountService.getHeaderSummary(1L);

        assertNotNull(result);
        assertEquals("X123", result.getResponse().getAccountNumber());
        assertTrue(result.getResponse().getHasConsolidatedAccounts());
        verify(defendantAccountServiceProxy).getHeaderSummary(1L);
    }

    @Test
    void testSearchDefendantAccounts_forbiddenWhenUserHasNoPermission() {

        //Arrange
        UserState user = UserState.builder()
            .userId(456L)
            .userName("noperms")
            .businessUnitUser(java.util.Collections.emptySet())
            .build();
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(user);

        AccountSearchDto dto = AccountSearchDto.builder().build();

        // Act & Assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountService.searchDefendantAccounts(dto)
        );

        assertThat(ex.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
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
                            .permissionId(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.getId())
                            .permissionName(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.name())
                            .build()
                    ))
                    .build()
            ))
            .build();

        DefendantAccountSearchResultsDto expected = DefendantAccountSearchResultsDto.builder().build();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userWithPerm);
        when(defendantAccountServiceProxy.searchDefendantAccounts(any(AccountSearchDto.class))).thenReturn(expected);

        AccountSearchDto dto = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto result = defendantAccountService.searchDefendantAccounts(dto);

        assertNotNull(result);
        verify(defendantAccountServiceProxy).searchDefendantAccounts(dto);
    }

    @Test
    void searchDefendantAccounts_generatedRequest_happyPath_mapsAndReturnsGeneratedResponse() {
        PostDefendantAccountSearchRequestDefendantAccount request =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
                .activeAccountsOnly(true)
                .businessUnitIds(List.of(77))
                .referenceNumber(new DefendantAccountSearchReferenceNumberDefendantAccount()
                    .organisation(false)
                    .accountNumber("A123"))
                .build();
        AccountSearchDto mappedRequest = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto proxyResults = DefendantAccountSearchResultsDto.builder().build();
        PostDefendantAccountSearchResponseDefendantAccount expectedResponse =
            PostDefendantAccountSearchResponseDefendantAccount.builder()
                .count(0)
                .defendantAccounts(List.of())
                .build();

        when(defendantAccountSearchRequestMapper.toAccountSearchDto(request)).thenReturn(mappedRequest);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(defendantAccountServiceProxy.searchDefendantAccounts(mappedRequest)).thenReturn(proxyResults);
        when(defendantAccountSearchResponseMapper.toResponse(proxyResults)).thenReturn(expectedResponse);

        PostDefendantAccountSearchResponseDefendantAccount result =
            defendantAccountService.searchDefendantAccounts(request);

        assertSame(expectedResponse, result);
        verify(defendantAccountSearchRequestValidator).validateAndCheckFeature(request);
        verify(defendantAccountSearchRequestMapper).toAccountSearchDto(request);
        verify(defendantAccountServiceProxy).searchDefendantAccounts(mappedRequest);
        verify(defendantAccountSearchResponseMapper).toResponse(proxyResults);
    }

    @Test
    void getAtAGlance_whenUserHasPermission_returnsProxyResult() {
        // arrange
        Long defendantAccountId = 77L;
        DefendantAccountAtAGlanceResponse proxyResponse = new DefendantAccountAtAGlanceResponse();
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(defendantAccountServiceProxy.getAtAGlance(defendantAccountId)).thenReturn(proxyResponse);

        // act
        DefendantAccountAtAGlanceResponse result =
            defendantAccountService.getAtAGlance(defendantAccountId);

        // assert
        assertSame(proxyResponse, result, "Should return exactly the proxy response");

        // verify interactions
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(defendantAccountServiceProxy).getAtAGlance(defendantAccountId);
        verifyNoMoreInteractions(userStateService, userState, defendantAccountServiceProxy);
    }

    @Test
    void getAtAGlance_whenUserLacksPermission_throwsPermissionNotAllowed() {
        // arrange
        Long defendantAccountId = 77L;
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        // act + assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountService.getAtAGlance(defendantAccountId)
        );
        assertTrue(
            ex.getMessage() == null || ex.getMessage().contains(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.name()),
            "Exception should mention the denied permission"
        );
        assertThat(ex.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        // proxy must not be called
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(defendantAccountServiceProxy);
        verifyNoMoreInteractions(userStateService, userState);
    }

    @Test
    void updateDefendantAccount_throwsWhenNoUpdateGroupsProvided() {
        // Arrange
        Long id = 1L;
        UpdateDefendantAccountRequestPayload req = UpdateDefendantAccountRequestPayload.builder().build();
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(allPermissionsUser());

        // Act
        final String buHeader = "10";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            defendantAccountService.updateDefendantAccount(id, buHeader, req, "1")
        );

        // Assert
        assertTrue(ex.getMessage().contains("Exactly one update group must be provided"));
        verifyNoInteractions(opalDefendantAccountService);
    }
}
