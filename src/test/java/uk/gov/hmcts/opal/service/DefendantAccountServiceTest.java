package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import java.math.BigInteger;
import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

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

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allFinesPermissionUser());
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
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(defendantAccountServiceProxy.getPaymentTerms(defendantAccountId)).thenReturn(proxyResponse);

        // act
        GetDefendantAccountPaymentTermsResponse result =
            defendantAccountService.getPaymentTerms(defendantAccountId, authHeader);

        // assert
        assertSame(proxyResponse, result, "Should return exactly the proxy response");

        // verify interactions
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(defendantAccountServiceProxy).getPaymentTerms(defendantAccountId);
        verifyNoMoreInteractions(userStateService, userState, defendantAccountServiceProxy);
    }

    @Test
    void getPaymentTerms_whenUserLacksPermission_throwsPermissionNotAllowed() {
        // arrange
        Long defendantAccountId = 77L;
        String authHeader = "Bearer abc";
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        // act + assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountService.getPaymentTerms(defendantAccountId, authHeader)
        );
        assertTrue(
            ex.getMessage() == null || ex.getMessage().contains(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.name()),
            "Exception should mention the denied permission"
        );

        // proxy must not be called
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
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
                            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.getId(),
                            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.getDescription()
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
                            .permissionId(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.getId())
                            .permissionName(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.name())
                            .build()
                    ))
                    .build()
            ))
            .build();

        DefendantAccountSearchResultsDto expected = DefendantAccountSearchResultsDto.builder().build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userWithPerm);
        when(defendantAccountServiceProxy.searchDefendantAccounts(any(AccountSearchDto.class))).thenReturn(expected);

        AccountSearchDto dto = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto result = defendantAccountService.searchDefendantAccounts(dto,
            "authHeaderValue");

        assertNotNull(result);
        verify(defendantAccountServiceProxy).searchDefendantAccounts(dto);
    }

    @Test
    void addPaymentTerms_overwritesPostedDetailsFromUserState() {
        Long defendantAccountId = 77L;
        String businessUnitId = "78";
        String ifMatch = "\"1\"";
        String authHeader = "Bearer token";

        UserState userWithPerm = UserStateUtil.permissionUser((short) 78, FinesPermission.AMEND_PAYMENT_TERMS);
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userWithPerm);

        AddDefendantAccountPaymentTermsRequest request = AddDefendantAccountPaymentTermsRequest.builder()
            .paymentTerms(PaymentTerms.builder()
                .postedDetails(PostedDetails.builder()
                    .postedBy("FE_USER")
                    .postedByName("FE_NAME")
                    .build())
                .build())
            .build();

        GetDefendantAccountPaymentTermsResponse proxyResponse = new GetDefendantAccountPaymentTermsResponse();
        when(defendantAccountServiceProxy.addPaymentTerms(eq(defendantAccountId),
            eq(businessUnitId),
            eq("USER01"),
            eq(ifMatch),
            eq(authHeader),
            any(AddDefendantAccountPaymentTermsRequest.class)))
            .thenReturn(proxyResponse);

        GetDefendantAccountPaymentTermsResponse result = defendantAccountService.addPaymentTerms(
            defendantAccountId, businessUnitId, ifMatch, authHeader, request);

        assertSame(proxyResponse, result);

        ArgumentCaptor<AddDefendantAccountPaymentTermsRequest> captor =
            ArgumentCaptor.forClass(AddDefendantAccountPaymentTermsRequest.class);
        verify(defendantAccountServiceProxy).addPaymentTerms(eq(defendantAccountId),
            eq(businessUnitId),
            eq("USER01"),
            eq(ifMatch),
            eq(authHeader),
            captor.capture());

        PostedDetails postedDetails = captor.getValue().getPaymentTerms().getPostedDetails();
        assertNotNull(postedDetails);
        assertEquals("USER01", postedDetails.getPostedBy());
        assertEquals("normal@users.com", postedDetails.getPostedByName());
    }

    @Test
    void getAtAGlance_whenUserHasPermission_returnsProxyResult() {
        // arrange
        Long defendantAccountId = 77L;
        String authHeader = "Bearer abc";
        DefendantAccountAtAGlanceResponse proxyResponse = new DefendantAccountAtAGlanceResponse();
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(defendantAccountServiceProxy.getAtAGlance(defendantAccountId)).thenReturn(proxyResponse);

        // act
        DefendantAccountAtAGlanceResponse result =
            defendantAccountService.getAtAGlance(defendantAccountId, authHeader);

        // assert
        assertSame(proxyResponse, result, "Should return exactly the proxy response");

        // verify interactions
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(defendantAccountServiceProxy).getAtAGlance(defendantAccountId);
        verifyNoMoreInteractions(userStateService, userState, defendantAccountServiceProxy);
    }

    @Test
    void getAtAGlance_whenUserLacksPermission_throwsPermissionNotAllowed() {
        // arrange
        Long defendantAccountId = 77L;
        String authHeader = "Bearer abc";
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        // act + assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountService.getAtAGlance(defendantAccountId, authHeader)
        );
        assertTrue(
            ex.getMessage() == null || ex.getMessage().contains(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.name()),
            "Exception should mention the denied permission"
        );

        // proxy must not be called
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(defendantAccountServiceProxy);
        verifyNoMoreInteractions(userStateService, userState);
    }


    @Test
    void addEnforcement_whenUserHasPermission_callsProxyAndReturnsResult() {
        // arrange
        Long defendantAccountId = 77L;
        String businessUnitId = "10";
        String ifMatch = "\"3\"";
        String authHeader = "Bearer abc";
        AddDefendantAccountEnforcementRequest req = mock(AddDefendantAccountEnforcementRequest.class);

        AddEnforcementResponse proxyResponse = AddEnforcementResponse.builder()
            .enforcementId("ENF123")
            .defendantAccountId("77")
            .version(3)
            .build();

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT)).thenReturn(true);

        // business unit user lookup returns an Optional<BusinessUnitUser> with a non-blank ID
        BusinessUnitUser buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("BU-USER-1");
        when(userState.getBusinessUnitUserForBusinessUnit((short)10))
            .thenReturn(java.util.Optional.of(buUser));

        when(defendantAccountServiceProxy.addEnforcement(
            defendantAccountId, businessUnitId, "BU-USER-1", ifMatch, authHeader, req))
            .thenReturn(proxyResponse);

        // act
        AddEnforcementResponse result =
            defendantAccountService.addEnforcement(defendantAccountId, businessUnitId, ifMatch, authHeader, req);

        // assert
        assertSame(proxyResponse, result, "Should return exactly the proxy response");

        // verify interactions
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT);
        verify(userState).getBusinessUnitUserForBusinessUnit((short)10);
        verify(defendantAccountServiceProxy)
            .addEnforcement(defendantAccountId, businessUnitId, "BU-USER-1", ifMatch, authHeader, req);
        verifyNoMoreInteractions(userStateService, userState, defendantAccountServiceProxy);
    }

    @Test
    void addEnforcement_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // arrange
        Long defendantAccountId = 77L;
        String businessUnitId = "10";
        String authHeader = "Bearer abc";

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT))
            .thenReturn(false);

        // act + assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountService.addEnforcement(defendantAccountId, businessUnitId, "\"3\"", authHeader, null)
        );

        assertTrue(
            ex.getMessage() == null || ex.getMessage().contains(FinesPermission.ENTER_ENFORCEMENT.name()),
            "Exception should mention ENTER_ENFORCEMENT"
        );

        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT);
        verifyNoInteractions(defendantAccountServiceProxy);
    }

    @Test
    void addEnforcement_whenBusinessUnitUserIdBlank_usesNullInProxyCall() {
        // arrange
        Long defendantAccountId = 77L;
        String businessUnitId = "10";
        String authHeader = "Bearer abc";

        AddDefendantAccountEnforcementRequest req = mock(AddDefendantAccountEnforcementRequest.class);

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT)).thenReturn(true);

        // return Optional<BusinessUnitUser> but with blank ID -> results in null
        BusinessUnitUser buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("   "); // blank
        when(userState.getBusinessUnitUserForBusinessUnit((short)10))
            .thenReturn(java.util.Optional.of(buUser));

        AddEnforcementResponse proxyResult = AddEnforcementResponse.builder()
            .enforcementId("X")
            .build();

        when(defendantAccountServiceProxy.addEnforcement(
            eq(defendantAccountId),
            eq(businessUnitId),
            isNull(),                   // IMPORTANT: businessUnitUserId expected to be null
            eq("\"3\""),
            eq(authHeader),
            eq(req)
        )).thenReturn(proxyResult);

        // act
        AddEnforcementResponse out =
            defendantAccountService.addEnforcement(defendantAccountId, businessUnitId, "\"3\"", authHeader, req);

        // assert
        assertNotNull(out);
        verify(defendantAccountServiceProxy).addEnforcement(
            eq(defendantAccountId),
            eq(businessUnitId),
            isNull(),                   // verifies null is passed
            eq("\"3\""),
            eq(authHeader),
            eq(req)
        );
    }


    @Test
    void testGetEnforcementStatus() {
        // Arrange
        EnforcementStatus status = EnforcementStatus.builder()
            .employerFlag(true)
            .isHmrcCheckEligible(true)
            .version(new BigInteger("1234567890123345678901234567890"))
            .build();
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());
        when(defendantAccountServiceProxy.getEnforcementStatus(anyLong())).thenReturn(status);

        // Act
        EnforcementStatus response = defendantAccountService
            .getEnforcementStatus(33L, "Bearer a_bearer_token");

        // Assert
        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertTrue(response.getIsHmrcCheckEligible());
        assertEquals(new BigInteger("1234567890123345678901234567890"), response.getVersion());

    }
}
