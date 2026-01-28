package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
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

    // Service under test
    @InjectMocks
    private OpalDefendantAccountFixedPenaltyService service;

    @Test
    void getDefendantAccountFixedPenalty_shouldReturnVehicleFixedPenaltyResponse() {
        Long defendantAccountId = 77L;

        DefendantAccountEntity mockAccount = buildMockAccount(defendantAccountId);
        FixedPenaltyOffenceEntity mockOffence = buildMockOffence(true);

        when(defendantAccountRepositoryService.findById(defendantAccountId))
            .thenReturn(mockAccount);
        when(fixedPenaltyOffenceRepositoryService.findByDefendantAccountId(defendantAccountId))
            .thenReturn(mockOffence);

        GetDefendantAccountFixedPenaltyResponse response =
            service.getDefendantAccountFixedPenalty(defendantAccountId);

        assertNotNull(response);
        assertTrue(response.isVehicleFixedPenaltyFlag());
        assertEquals("Kingston-upon-Thames Mags Court",
            response.getFixedPenaltyTicketDetails().getIssuingAuthority());
        assertEquals("888", response.getFixedPenaltyTicketDetails().getTicketNumber());
        assertEquals("12:34", response.getFixedPenaltyTicketDetails().getTimeOfOffence());
        assertEquals("London", response.getFixedPenaltyTicketDetails().getPlaceOfOffence());
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldReturnNonVehiclePenaltyResponse() {
        Long accountId = 88L;

        DefendantAccountEntity account = buildMockAccount(accountId);
        FixedPenaltyOffenceEntity offence = buildMockOffence(false);
        offence.setOffenceLocation("Manchester");
        offence.setTimeOfOffence(LocalTime.parse("12:12"));

        when(defendantAccountRepositoryService.findById(accountId)).thenReturn(account);
        when(fixedPenaltyOffenceRepositoryService.findByDefendantAccountId(accountId)).thenReturn(offence);

        GetDefendantAccountFixedPenaltyResponse response = service.getDefendantAccountFixedPenalty(accountId);

        assertNotNull(response);
        assertFalse(response.isVehicleFixedPenaltyFlag());
        assertEquals("Kingston-upon-Thames Mags Court", response.getFixedPenaltyTicketDetails().getIssuingAuthority());
        assertEquals("888", response.getFixedPenaltyTicketDetails().getTicketNumber());
        assertEquals("12:12", response.getFixedPenaltyTicketDetails().getTimeOfOffence());
        assertEquals("Manchester", response.getFixedPenaltyTicketDetails().getPlaceOfOffence());
        assertNull(response.getVehicleFixedPenaltyDetails());
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldHandleNullOptionalFields() {
        Long id = 456L;
        DefendantAccountEntity account = buildMockAccount(id);
        account.setOriginatorName(null);

        FixedPenaltyOffenceEntity offence = buildMockOffence(true);
        offence.setOffenceLocation(null);
        offence.setIssuedDate(null);
        offence.setLicenceNumber(null);
        offence.setVehicleRegistration(null);
        offence.setTimeOfOffence(null);

        when(defendantAccountRepositoryService.findById(id)).thenReturn(account);
        when(fixedPenaltyOffenceRepositoryService.findByDefendantAccountId(id)).thenReturn(offence);

        var response = service.getDefendantAccountFixedPenalty(id);
        assertNotNull(response);
        assertNotNull(response.getFixedPenaltyTicketDetails());
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldMapVersionCorrectly() {
        Long id = 789L;
        DefendantAccountEntity acc = buildMockAccount(id);
        acc.setVersionNumber(5L);

        FixedPenaltyOffenceEntity offence = buildMockOffence(false);

        when(defendantAccountRepositoryService.findById(id)).thenReturn(acc);
        when(fixedPenaltyOffenceRepositoryService.findByDefendantAccountId(id)).thenReturn(offence);

        var resp = service.getDefendantAccountFixedPenalty(id);
        assertEquals(BigInteger.valueOf(5), resp.getVersion());
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldCallProxyWhenAuthorized() {
        // Arrange
        var proxy = mock(DefendantAccountFixedPenaltyServiceProxy.class);
        var userStateService = mock(UserStateService.class);
        var mockUserState = mock(UserState.class);
        var mockResponse = new GetDefendantAccountFixedPenaltyResponse();

        when(userStateService.checkForAuthorisedUser("Bearer token")).thenReturn(mockUserState);
        when(mockUserState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(proxy.getDefendantAccountFixedPenalty(123L)).thenReturn(mockResponse);

        var service = new DefendantAccountFixedPenaltyService(proxy, userStateService);

        // Act
        var response = service.getDefendantAccountFixedPenalty(123L, "Bearer token");

        // Assert
        verify(proxy).getDefendantAccountFixedPenalty(123L);
        assertEquals(mockResponse, response);
    }


    @Test
    void getDefendantAccountFixedPenalty_shouldThrowWhenNotAuthorized() {
        // Arrange
        var proxy = mock(DefendantAccountFixedPenaltyServiceProxy.class);
        var userStateService = mock(UserStateService.class);
        var mockUserState = mock(UserState.class);

        when(userStateService.checkForAuthorisedUser("auth")).thenReturn(mockUserState);
        when(mockUserState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);

        var service = new DefendantAccountFixedPenaltyService(proxy, userStateService);

        // Act + Assert
        assertThrows(PermissionNotAllowedException.class,
            () -> service.getDefendantAccountFixedPenalty(123L, "auth")
        );

        verifyNoInteractions(proxy);
    }

    private DefendantAccountEntity buildMockAccount(Long accountId) {
        return DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .originatorName("Kingston-upon-Thames Mags Court")
            .versionNumber(1L)
            .build();
    }

    private FixedPenaltyOffenceEntity buildMockOffence(boolean isVehicle) {
        return FixedPenaltyOffenceEntity.builder()
            .ticketNumber("888")
            .vehicleRegistration(isVehicle ? "AB12CDE" : null)
            .offenceLocation("London")
            .noticeNumber("PN98765")
            .issuedDate(LocalDate.of(2024, 1, 1))
            .licenceNumber("DOE1234567")
            .vehicleFixedPenalty(isVehicle)
            .timeOfOffence(LocalTime.parse("12:34"))
            .build();
    }

}
