package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountResponseMapper;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountAtAGlanceRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;

@ExtendWith(MockitoExtension.class)
class OpalMinorCreditorServiceGetTest {

    @Mock
    private MinorCreditorRepository minorCreditorRepository;

    @Mock
    private MinorCreditorAccountHeaderRepository minorCreditorAccountHeaderRepository;

    @Mock
    private MinorCreditorAccountAtAGlanceRepository minorCreditorAccountAtAGlanceRepository;

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private AmendmentService amendmentService;

    @Mock
    private MinorCreditorAccountResponseMapper responseMapper;

    @InjectMocks
    private OpalMinorCreditorService service;

    @Test
    void getMinorCreditorAccount_success_returnsMappedResponseWithVersion() {
        // Arrange
        Long accountId = 101L;
        Long partyId = 201L;

        CreditorAccountEntity account = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .minorCreditorPartyId(partyId)
            .creditorAccountType(CreditorAccountType.MN)
            .versionNumber(5L)
            .build();
        PartyEntity party = PartyEntity.builder().partyId(partyId).organisation(false).build();
        MinorCreditorAccountResponse mappedResponse = new MinorCreditorAccountResponse();

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
        when(responseMapper.toMinorCreditorAccountResponse(account, party)).thenReturn(mappedResponse);

        // Act
        MinorCreditorAccountResponse result = service.getMinorCreditorAccount(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(mappedResponse, result);
        assertEquals(BigInteger.valueOf(5L), result.getVersion());
        verify(responseMapper).toMinorCreditorAccountResponse(account, party);
    }

    @Test
    void getMinorCreditorAccount_missingAccount_throwsEntityNotFoundException() {
        // Arrange
        when(creditorAccountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorAccount(999L));
    }

    @Test
    void getMinorCreditorAccount_nonMinorCreditor_throwsEntityNotFoundException() {
        // Arrange
        Long accountId = 101L;
        CreditorAccountEntity account = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MJ)
            .build();

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorAccount(accountId));
    }

    @Test
    void getMinorCreditorAccount_missingParty_throwsEntityNotFoundException() {
        // Arrange
        Long accountId = 101L;
        Long partyId = 201L;
        CreditorAccountEntity account = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .minorCreditorPartyId(partyId)
            .creditorAccountType(CreditorAccountType.MN)
            .build();

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(partyRepository.findById(partyId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorAccount(accountId));
    }

    @Test
    void getMinorCreditorHistory_success_returnsEmptyPayloadWithVersion() {
        // Arrange
        Long accountId = 101L;
        CreditorAccountEntity account = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MN)
            .versionNumber(5L)
            .build();

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        GetMinorCreditorHistoryResponse result = service.getMinorCreditorHistory(
            accountId, MinorCreditorHistoryFilters.from(null, null, null));

        // Assert
        assertNotNull(result);
        assertEquals(BigInteger.valueOf(5L), result.getVersion());
        assertNotNull(result.getPayload());
        assertEquals(List.of(), result.getPayload().getHistoryItems());
    }

    @Test
    void getMinorCreditorHistory_missingAccount_throwsEntityNotFoundException() {
        // Arrange
        when(creditorAccountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorHistory(
            999L, MinorCreditorHistoryFilters.from(null, null, null)));
    }

    @Test
    void getMinorCreditorHistory_nonMinorCreditor_throwsEntityNotFoundException() {
        // Arrange
        Long accountId = 101L;
        CreditorAccountEntity account = CreditorAccountEntity.builder()
            .creditorAccountId(accountId)
            .creditorAccountType(CreditorAccountType.MJ)
            .build();

        when(creditorAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.getMinorCreditorHistory(
            accountId, MinorCreditorHistoryFilters.from(null, null, null)));
    }
}
