package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

@ExtendWith(MockitoExtension.class)
class DefendantAccountRepositoryServiceTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @InjectMocks
    private DefendantAccountRepositoryService service;

    @Test
    void findById_whenAccountExists_returnsAccount() {
        // arrange
        long defendantAccountId = 1L;
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(defendantAccountId)
            .build();

        when(defendantAccountRepository.findById(defendantAccountId))
            .thenReturn(Optional.of(account));

        // act
        DefendantAccountEntity result = service.findById(defendantAccountId);

        // assert
        assertThat(result).isEqualTo(account);
        verify(defendantAccountRepository).findById(defendantAccountId);
    }

    @Test
    void findById_whenAccountNotFound_throwsEntityNotFoundException() {
        // arrange
        long defendantAccountId = 99L;

        when(defendantAccountRepository.findById(defendantAccountId))
            .thenReturn(Optional.empty());

        // act / assert
        assertThatThrownBy(() -> service.findById(defendantAccountId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Defendant Account not found with id: " + defendantAccountId);

        verify(defendantAccountRepository).findById(defendantAccountId);
    }

    @Test
    void saveAndFlush_whenValidAccount_returnsSavedAccount() {
        // arrange
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(10L)
            .versionNumber(1L)
            .build();

        DefendantAccountEntity savedAccount = DefendantAccountEntity.builder()
            .defendantAccountId(10L)
            .versionNumber(2L)
            .build();

        when(defendantAccountRepository.saveAndFlush(account))
            .thenReturn(savedAccount);

        // act
        DefendantAccountEntity result = service.saveAndFlush(account);

        // assert
        assertThat(result).isEqualTo(savedAccount);
        assertThat(result.getVersionNumber()).isEqualTo(2L);
        verify(defendantAccountRepository).saveAndFlush(account);
    }

    @Test
    void save_whenValidAccount_returnsSavedAccount() {
        // arrange
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(5L)
            .versionNumber(1L)
            .build();

        DefendantAccountEntity savedAccount = DefendantAccountEntity.builder()
            .defendantAccountId(5L)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.save(account))
            .thenReturn(savedAccount);

        // act
        DefendantAccountEntity result = service.save(account);

        // assert
        assertThat(result).isEqualTo(savedAccount);
        verify(defendantAccountRepository).save(account);
    }
}

