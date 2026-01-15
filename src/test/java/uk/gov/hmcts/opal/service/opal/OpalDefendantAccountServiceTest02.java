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

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
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
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountServiceTest02 {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;

    // Service under test
    @InjectMocks
    private OpalDefendantAccountService service;

    @Test
    void getDefendantAccountFixedPenalty_shouldReturnVehicleFixedPenaltyResponse() {
        Long defendantAccountId = 77L;

        DefendantAccountEntity mockAccount = buildMockAccount(defendantAccountId);
        FixedPenaltyOffenceEntity mockOffence = buildMockOffence(true);

        when(defendantAccountRepository.findById(defendantAccountId))
            .thenReturn(Optional.of(mockAccount));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId))
            .thenReturn(Optional.of(mockOffence));

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

        when(defendantAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(accountId)).thenReturn(Optional.of(offence));

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
    void getDefendantAccountFixedPenalty_shouldThrowWhenNoOffenceFound() {
        Long accountId = 999L;
        DefendantAccountEntity account = buildMockAccount(accountId);

        when(defendantAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(accountId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
            EntityNotFoundException.class,
            () -> service.getDefendantAccountFixedPenalty(accountId)
        );

        assertTrue(ex.getMessage().contains("Fixed Penalty Offence not found for account: 999"));
        verify(fixedPenaltyOffenceRepository).findByDefendantAccountId(accountId);
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldThrowWhenAccountNotFound() {
        Long id = 123L;
        when(defendantAccountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getDefendantAccountFixedPenalty(id));
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

        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(account));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(id)).thenReturn(Optional.of(offence));

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

        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(acc));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(id)).thenReturn(Optional.of(offence));

        var resp = service.getDefendantAccountFixedPenalty(id);
        assertEquals(BigInteger.valueOf(5), resp.getVersion());
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldCallProxyWhenAuthorized() {
        // Arrange
        var proxy = mock(DefendantAccountServiceProxy.class);
        var userStateService = mock(UserStateService.class);
        var mockUserState = mock(UserState.class);
        var mockResponse = new GetDefendantAccountFixedPenaltyResponse();

        when(userStateService.checkForAuthorisedUser("Bearer token")).thenReturn(mockUserState);
        when(mockUserState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(proxy.getDefendantAccountFixedPenalty(123L)).thenReturn(mockResponse);

        var service = new DefendantAccountService(proxy, userStateService);

        // Act
        var response = service.getDefendantAccountFixedPenalty(123L, "Bearer token");

        // Assert
        verify(proxy).getDefendantAccountFixedPenalty(123L);
        assertEquals(mockResponse, response);
    }


    @Test
    void getDefendantAccountFixedPenalty_shouldThrowWhenNotAuthorized() {
        // Arrange
        var proxy = mock(DefendantAccountServiceProxy.class);
        var userStateService = mock(UserStateService.class);
        var mockUserState = mock(UserState.class);

        when(userStateService.checkForAuthorisedUser("auth")).thenReturn(mockUserState);
        when(mockUserState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);

        var service = new DefendantAccountService(proxy, userStateService);

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
