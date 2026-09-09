package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertAll;
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
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.mapper.DefendantAccountFixedPenaltyMapper;
import uk.gov.hmcts.opal.service.DefendantAccountFixedPenaltyService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.FixedPenaltyOffenceRepositoryService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountFixedPenaltyServiceProxy;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountServiceTest02 {

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private FixedPenaltyOffenceRepositoryService fixedPenaltyOffenceRepositoryService;

    @Mock
    private DefendantAccountFixedPenaltyMapper defendantAccountFixedPenaltyMapper;

    // Service under test
    @InjectMocks
    private OpalDefendantAccountFixedPenaltyService service;

    @Test
    void getDefendantAccountFixedPenalty_shouldReturnMapperResponse() {
        Long defendantAccountId = 77L;

        DefendantAccountEntity account = DefendantAccountEntity.builder().build();
        FixedPenaltyOffenceEntity offence = FixedPenaltyOffenceEntity.builder().build();
        GetDefendantAccountFixedPenaltyResponse mappedResponse = new GetDefendantAccountFixedPenaltyResponse();

        when(defendantAccountRepositoryService.findById(defendantAccountId))
            .thenReturn(account);
        when(fixedPenaltyOffenceRepositoryService.findByDefendantAccountId(defendantAccountId))
            .thenReturn(offence);
        when(defendantAccountFixedPenaltyMapper.toResponse(account, offence)).thenReturn(mappedResponse);

        GetDefendantAccountFixedPenaltyResponse response =
            service.getDefendantAccountFixedPenalty(defendantAccountId);

        assertAll(
            () -> assertEquals(mappedResponse, response),
            () -> verify(defendantAccountRepositoryService).findById(defendantAccountId),
            () -> verify(fixedPenaltyOffenceRepositoryService).findByDefendantAccountId(defendantAccountId),
            () -> verify(defendantAccountFixedPenaltyMapper).toResponse(account, offence)
        );
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldCallProxyWhenAuthorized() {
        // Arrange
        var proxy = mock(DefendantAccountFixedPenaltyServiceProxy.class);
        var userStateService = mock(UserStateService.class);
        var mockUserState = mock(UserState.class);
        var mockResponse = new GetDefendantAccountFixedPenaltyResponse();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(mockUserState);
        when(mockUserState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(proxy.getDefendantAccountFixedPenalty(123L)).thenReturn(mockResponse);

        var service = new DefendantAccountFixedPenaltyService(proxy, userStateService);

        // Act
        var response = service.getDefendantAccountFixedPenalty(123L);

        // Assert
        assertAll(
            () -> verify(proxy).getDefendantAccountFixedPenalty(123L),
            () -> assertEquals(mockResponse, response)
        );
    }


    @Test
    void getDefendantAccountFixedPenalty_shouldThrowWhenNotAuthorized() {
        // Arrange
        var proxy = mock(DefendantAccountFixedPenaltyServiceProxy.class);
        var userStateService = mock(UserStateService.class);
        var mockUserState = mock(UserState.class);

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(mockUserState);
        when(mockUserState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);

        var service = new DefendantAccountFixedPenaltyService(proxy, userStateService);

        // Act + Assert
        assertAll(
            () -> assertThrows(PermissionNotAllowedException.class,
                () -> service.getDefendantAccountFixedPenalty(123L)
            ),
            () -> verifyNoInteractions(proxy)
        );
    }

}
