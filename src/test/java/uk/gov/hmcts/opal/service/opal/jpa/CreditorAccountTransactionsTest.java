package uk.gov.hmcts.opal.service.opal.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;

import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;

@ExtendWith(MockitoExtension.class)
class CreditorAccountTransactionsTest {

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private CreditorTransactionRepository creditorTransactionRepository;

    @Mock
    private ImpositionRepository impositionRepository;

    @InjectMocks
    private CreditorAccountTransactions creditorAccountTransactions;

    @Test
    void testGetCreditorAccount() {
        // Arrange
        CreditorAccountEntity.Lite creditorAccountEntity = CreditorAccountEntity.Lite
            .builder().businessUnitId((short)77).build();
        when(creditorAccountRepository.findById(any())).thenReturn(Optional.of(creditorAccountEntity));

        // Act
        CreditorAccountEntity result = creditorAccountTransactions.getCreditorAccountById(1);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testDeleteCreditorAccount_success() {
        // Arrange
        CreditorAccountEntity.Lite creditorAccountEntity = CreditorAccountEntity.Lite.builder().build();
        when(creditorAccountRepository.findById(any())).thenReturn(Optional.of(creditorAccountEntity));

        // Act
        boolean deleted = creditorAccountTransactions.deleteMinorCreditor(1, creditorAccountTransactions);
        assertTrue(deleted);
    }

    @Test
    void testDeleteCreditorAccount_fail1() {
        // Arrange
        when(creditorAccountRepository.findById(any())).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException enfe = assertThrows(
            EntityNotFoundException.class, () -> creditorAccountTransactions
                .deleteMinorCreditor(1, creditorAccountTransactions)
        );

        // Assert
        assertEquals("Creditor Account not found with id: 1", enfe.getMessage());
    }











}
