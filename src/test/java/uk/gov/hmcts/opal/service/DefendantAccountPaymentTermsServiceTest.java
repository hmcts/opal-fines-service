package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountPaymentTermsServiceProxy;

@ExtendWith(MockitoExtension.class)
class DefendantAccountPaymentTermsServiceTest {

    @Mock
    private DefendantAccountPaymentTermsServiceProxy defendantAccountPaymentTermsServiceProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private UserState userState;

    @InjectMocks
    private DefendantAccountPaymentTermsService defendantAccountPaymentTermsService;

    @Test
    void getPaymentTerms_whenUserHasPermission_returnsProxyResult() {
        // arrange
        Long defendantAccountId = 77L;
        GetDefendantAccountPaymentTermsResponse proxyResponse = new GetDefendantAccountPaymentTermsResponse();
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(defendantAccountPaymentTermsServiceProxy.getPaymentTerms(defendantAccountId)).thenReturn(proxyResponse);

        // act
        GetDefendantAccountPaymentTermsResponse result =
            defendantAccountPaymentTermsService.getPaymentTerms(defendantAccountId);

        // assert
        assertSame(proxyResponse, result, "Should return exactly the proxy response");

        // verify interactions
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(defendantAccountPaymentTermsServiceProxy).getPaymentTerms(defendantAccountId);
        verifyNoMoreInteractions(userStateService, userState, defendantAccountPaymentTermsServiceProxy);
    }

    @Test
    void getPaymentTerms_whenUserLacksPermission_throwsPermissionNotAllowed() {
        // arrange
        Long defendantAccountId = 77L;
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        // act + assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPaymentTermsService.getPaymentTerms(defendantAccountId)
        );
        assertTrue(
            ex.getMessage() == null || ex.getMessage().contains(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.name()),
            "Exception should mention the denied permission"
        );

        // proxy must not be called
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(defendantAccountPaymentTermsServiceProxy);
        verifyNoMoreInteractions(userStateService, userState);
    }

    @Test
    void addPaymentTerms_overwritesPostedDetailsFromUserState() {
        Long defendantAccountId = 77L;
        String businessUnitId = "78";
        String ifMatch = "\"1\"";

        UserState userWithPerm = UserStateUtil.permissionUser((short) 78, FinesPermission.AMEND_PAYMENT_TERMS);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userWithPerm);

        AddDefendantAccountPaymentTermsRequest request = AddDefendantAccountPaymentTermsRequest.builder()
            .paymentTerms(PaymentTerms.builder()
                .postedDetails(PostedDetails.builder()
                    .postedBy("FE_USER")
                    .postedByName("FE_NAME")
                    .build())
                .build())
            .build();

        GetDefendantAccountPaymentTermsResponse proxyResponse = new GetDefendantAccountPaymentTermsResponse();
        when(defendantAccountPaymentTermsServiceProxy.addPaymentTerms(eq(defendantAccountId),
            eq(businessUnitId),
            eq("USER01"),
            eq(ifMatch),
            any(AddDefendantAccountPaymentTermsRequest.class)))
            .thenReturn(proxyResponse);

        GetDefendantAccountPaymentTermsResponse result = defendantAccountPaymentTermsService.addPaymentTerms(
            defendantAccountId, businessUnitId, ifMatch, request);

        assertSame(proxyResponse, result);

        ArgumentCaptor<AddDefendantAccountPaymentTermsRequest> captor =
            ArgumentCaptor.forClass(AddDefendantAccountPaymentTermsRequest.class);
        verify(defendantAccountPaymentTermsServiceProxy).addPaymentTerms(eq(defendantAccountId),
            eq(businessUnitId),
            eq("USER01"),
            eq(ifMatch),
            captor.capture());

        PostedDetails postedDetails = captor.getValue().getPaymentTerms().getPostedDetails();
        assertNotNull(postedDetails);
        assertEquals("USER01", postedDetails.getPostedBy());
        assertEquals("Normal User", postedDetails.getPostedByName());
    }
}
