package uk.gov.hmcts.opal.service.opal.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatchers;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;

import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;

@ExtendWith(MockitoExtension.class)
class CreditorAccountTransactionalTest {

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private CreditorTransactionRepository creditorTransactionRepository;

    @Mock
    private ImpositionRepository impositionRepository;

    @InjectMocks
    private CreditorAccountTransactional creditorAccountTransactional;

    @Test
    void testGetCreditorAccount() {
        // Arrange
        CreditorAccountEntity.Lite creditorAccountEntity = CreditorAccountEntity.Lite
            .builder().businessUnitId((short)77).build();
        when(creditorAccountRepository.findById(any())).thenReturn(Optional.of(creditorAccountEntity));

        // Act
        CreditorAccountEntity result = creditorAccountTransactional.getCreditorAccountById(1);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testDeleteCreditorAccount_success() {
        // Arrange
        CreditorAccountEntity.Lite creditorAccountEntity = CreditorAccountEntity.Lite.builder()
            .creditorAccountType(CreditorAccountType.MN)
            .build();
        when(creditorAccountRepository.findById(any())).thenReturn(Optional.of(creditorAccountEntity));

        // Act
        boolean deleted = creditorAccountTransactional
            .deleteMinorCreditorAccountAndRelatedData(1, creditorAccountTransactional
        );
        assertTrue(deleted);
    }

    @Test
    void testDeleteCreditorAccount_fail1() {
        // Arrange
        when(creditorAccountRepository.findById(any())).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException enfe = assertThrows(
            EntityNotFoundException.class, () -> creditorAccountTransactional
                .deleteMinorCreditorAccountAndRelatedData(1, creditorAccountTransactional)
        );

        // Assert
        assertEquals("Creditor Account not found with id: 1", enfe.getMessage());
    }

    @Test
    void deleteAllByDefendantAccountId_deletesRemainingImpositionsAfterMinorCreditorDeletes() {
        // Arrange
        CreditorAccountTransactionalProxy proxy = mock(CreditorAccountTransactionalProxy.class);
        long defendantAccountId = 99L;

        List<ImpositionEntity.Lite> initialImpositions = List.of(
            ImpositionEntity.Lite.builder().creditorAccountId(11L).build(),
            ImpositionEntity.Lite.builder().creditorAccountId(12L).build()
        );
        List<ImpositionEntity.Lite> remainingImpositions = List.of(
            ImpositionEntity.Lite.builder().creditorAccountId(77L).build()
        );

        when(impositionRepository.findAllByDefendantAccountId(defendantAccountId))
            .thenReturn(initialImpositions)
            .thenReturn(remainingImpositions);
        when(proxy.deleteMinorCreditorAccountAndRelatedData(any(Long.class), eq(proxy))).thenReturn(true);

        // Act
        boolean result = creditorAccountTransactional.deleteAllByDefendantAccountId(defendantAccountId, proxy);

        // Assert
        assertTrue(result);
        verify(proxy, times(2)).deleteMinorCreditorAccountAndRelatedData(any(Long.class), eq(proxy));
        verify(impositionRepository).deleteAll(remainingImpositions);
    }

    @Test
    void deleteMinorCreditorAccountAndRelatedData_deletesAssociatedPartyWhenPresent() {
        // Arrange
        Long creditorAccountId = 11L;
        Long partyId = 201L;

        CreditorAccountEntity.Lite creditorAccount = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(creditorAccountId)
            .creditorAccountType(CreditorAccountType.MN)
            .minorCreditorPartyId(partyId)
            .build();
        PartyEntity party = PartyEntity.builder().partyId(partyId).build();

        when(creditorAccountRepository.findById(creditorAccountId)).thenReturn(Optional.of(creditorAccount));
        when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
        when(creditorTransactionRepository.delete(
            ArgumentMatchers.<Specification<CreditorTransactionEntity>>any())).thenReturn(1L);

        // Act
        boolean result = creditorAccountTransactional.deleteMinorCreditorAccountAndRelatedData(
            creditorAccountId,
            creditorAccountTransactional
        );

        // Assert
        assertTrue(result);
        verify(impositionRepository).delete(ArgumentMatchers.<Specification<ImpositionEntity.Lite>>any());
        verify(creditorAccountRepository).delete(creditorAccount);
        verify(partyRepository).delete(party);
    }

    @Test
    void deleteMinorCreditorAccountAndRelatedData_partyStillReferenced_throwsDataIntegrityViolationException() {
        // Arrange
        Long creditorAccountId = 12L;
        Long partyId = 202L;

        CreditorAccountEntity.Lite creditorAccount = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(creditorAccountId)
            .creditorAccountType(CreditorAccountType.MN)
            .minorCreditorPartyId(partyId)
            .build();
        PartyEntity party = PartyEntity.builder().partyId(partyId).build();

        when(creditorAccountRepository.findById(creditorAccountId)).thenReturn(Optional.of(creditorAccount));
        when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
        when(creditorTransactionRepository.delete(
            ArgumentMatchers.<Specification<CreditorTransactionEntity>>any())).thenReturn(1L);
        doThrow(new DataIntegrityViolationException("still referenced")).when(partyRepository).delete(party);

        // Act
        DataIntegrityViolationException ex = assertThrows(
            DataIntegrityViolationException.class,
            () -> creditorAccountTransactional.deleteMinorCreditorAccountAndRelatedData(
                creditorAccountId,
                creditorAccountTransactional
            )
        );

        // Assert
        assertEquals("still referenced", ex.getMessage());
        verify(creditorAccountRepository).delete(creditorAccount);
        verify(partyRepository).delete(party);
    }

}
