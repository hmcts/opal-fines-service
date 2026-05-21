package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.GetCentralFundByBusinessUnit200Response;
import uk.gov.hmcts.opal.generated.model.GetCentralFundByBusinessUnit200ResponseMajorCreditor;
import uk.gov.hmcts.opal.repository.CentralFundProjection;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;

@ExtendWith(MockitoExtension.class)
class CentralFundServiceTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Mock
    private UserStateService userStateService;

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private UserState userState;

    @InjectMocks
    private CentralFundService centralFundService;

    @Test
    void getCentralFundByBusinessUnit_whenUserHasPermission_returnsMappedResponse() {
        CentralFundProjection centralFund = centralFundProjection();

        when(userStateService.checkForAuthorisedUser(AUTH_HEADER)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(creditorAccountRepository.findCentralFundByBusinessUnitId((short) 70))
            .thenReturn(Optional.of(centralFund));

        CentralFundResponse response = centralFundService.getCentralFundByBusinessUnit(70, AUTH_HEADER);

        GetCentralFundByBusinessUnit200Response payload = response.getPayload();
        GetCentralFundByBusinessUnit200ResponseMajorCreditor majorCreditor = payload.getMajorCreditor();
        BusinessUnitSummaryCommon businessUnitDetails = payload.getBusinessUnitDetails();

        assertEquals(123L, majorCreditor.getCreditorAccountId());
        assertEquals("CF123", majorCreditor.getAccountNumber());
        assertEquals("Central Fund", majorCreditor.getName());
        assertEquals("70", businessUnitDetails.getBusinessUnitId());
        assertEquals("London Collection", businessUnitDetails.getBusinessUnitName());
        assertEquals("Y", businessUnitDetails.getWelshSpeaking());
        assertEquals(BigInteger.valueOf(7), response.getVersion());
        verify(userStateService).checkForAuthorisedUser(AUTH_HEADER);
        verify(creditorAccountRepository).findCentralFundByBusinessUnitId((short) 70);
    }

    @Test
    void getCentralFundByBusinessUnit_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        PermissionNotAllowedException exception = assertThrows(
            PermissionNotAllowedException.class,
            () -> centralFundService.getCentralFundByBusinessUnit(70, AUTH_HEADER)
        );

        assertThat(exception.getPermission()).containsExactly(SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(creditorAccountRepository);
    }

    @Test
    void getCentralFundByBusinessUnit_whenCentralFundDoesNotExist_throwsEntityNotFoundException() {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(creditorAccountRepository.findCentralFundByBusinessUnitId((short) 70)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> centralFundService.getCentralFundByBusinessUnit(70, AUTH_HEADER)
        );

        assertEquals("Central fund not found for business unit: 70", exception.getMessage());
    }

    @Test
    void getCentralFundByBusinessUnit_whenBusinessUnitIdOutOfRange_throwsIllegalArgumentException() {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> centralFundService.getCentralFundByBusinessUnit(Short.MAX_VALUE + 1, AUTH_HEADER)
        );

        assertEquals("Business unit id is out of range: 32768", exception.getMessage());
        verifyNoInteractions(creditorAccountRepository);
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
