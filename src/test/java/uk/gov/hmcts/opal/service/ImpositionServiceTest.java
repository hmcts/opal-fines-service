package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.service.proxy.ImpositionServiceProxy;

@ExtendWith(MockitoExtension.class)
class ImpositionServiceTest {

    @Mock
    private UserStateService userStateService;

    @Mock
    private UserState userState;

    @Mock
    private ImpositionServiceProxy impositionServiceProxy;

    @InjectMocks
    private ImpositionService impositionService;

    @Test
    void getImpositions_whenUserHasPermission_returnsImpositionsServiceResult() {
        Long defendantAccountId = 77L;
        String authHeader = "Bearer abc";
        GetDefendantAccountImpositionsResponse impositionsResponse = GetDefendantAccountImpositionsResponse.builder()
            .version(BigInteger.valueOf(9))
            .build();
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(impositionServiceProxy.getImpositions(defendantAccountId)).thenReturn(impositionsResponse);

        GetDefendantAccountImpositionsResponse result =
            impositionService.getImpositions(defendantAccountId, authHeader);

        assertSame(impositionsResponse, result);
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(impositionServiceProxy).getImpositions(defendantAccountId);
        verifyNoMoreInteractions(userStateService, userState, impositionServiceProxy);
    }

    @Test
    void getImpositions_whenUserLacksPermission_throwsPermissionNotAllowed() {
        Long defendantAccountId = 77L;
        String authHeader = "Bearer abc";
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        assertThrows(
            PermissionNotAllowedException.class,
            () -> impositionService.getImpositions(defendantAccountId, authHeader)
        );

        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(impositionServiceProxy);
        verifyNoMoreInteractions(userStateService, userState);
    }
}
