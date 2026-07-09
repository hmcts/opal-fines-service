package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.CentralFundResponse;
import uk.gov.hmcts.opal.mapper.CentralFundMapper;
import uk.gov.hmcts.opal.repository.CentralFundProjection;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;

@ExtendWith(MockitoExtension.class)
class CentralFundServiceTest {

    @Mock
    private UserStateService userStateService;

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private CentralFundMapper centralFundMapper;

    @Mock
    private UserState userState;

    @InjectMocks
    private CentralFundService centralFundService;

    @Test
    void getCentralFundByBusinessUnit_whenUserHasPermission_returnsMappedResponse() {
        CentralFundProjection centralFund = centralFundProjection();
        CentralFundResponse mappedResponse = CentralFundResponse.builder()
            .version(BigInteger.valueOf(7))
            .build();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(creditorAccountRepository.findCentralFundByBusinessUnitId((short) 70))
            .thenReturn(Optional.of(centralFund));
        when(centralFundMapper.toCentralFundResponse(centralFund)).thenReturn(mappedResponse);

        CentralFundResponse response = centralFundService.getCentralFundByBusinessUnit((short) 70);

        assertSame(mappedResponse, response);
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(creditorAccountRepository).findCentralFundByBusinessUnitId((short) 70);
        verify(centralFundMapper).toCentralFundResponse(centralFund);
    }

    @Test
    void getCentralFundByBusinessUnit_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        PermissionNotAllowedException exception = assertThrows(
            PermissionNotAllowedException.class,
            () -> centralFundService.getCentralFundByBusinessUnit((short) 70)
        );

        assertThat(exception.getPermission()).containsExactly(SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(creditorAccountRepository, centralFundMapper);
    }

    @Test
    void getCentralFundByBusinessUnit_whenCentralFundDoesNotExist_throwsEntityNotFoundException() {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(creditorAccountRepository.findCentralFundByBusinessUnitId((short) 70)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> centralFundService.getCentralFundByBusinessUnit((short) 70)
        );

        assertEquals("Central fund not found for business unit: 70", exception.getMessage());
        verifyNoInteractions(centralFundMapper);
    }

    private CentralFundProjection centralFundProjection() {
        return new CentralFundProjection() {
            @Override
            public Long getCreditorAccountId() {
                return 123L;
            }

            @Override
            public String getAccountNumber() {
                return "CF123";
            }

            @Override
            public String getName() {
                return "Central Fund";
            }

            @Override
            public Short getBusinessUnitId() {
                return 70;
            }

            @Override
            public String getBusinessUnitName() {
                return "London Collection";
            }

            @Override
            public Boolean getWelshLanguage() {
                return true;
            }

            @Override
            public Long getVersionNumber() {
                return 7L;
            }
        };
    }
}
