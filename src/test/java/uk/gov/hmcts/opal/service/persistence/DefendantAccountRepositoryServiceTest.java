package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
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

    @Test
    void validateAccountExistsInBusinessUnit_whenBusinessUnitMatches_doesNotThrow() {
        // arrange
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 78).build())
            .build();

        // act / assert
        assertThatCode(() -> service.validateAccountExistsInBusinessUnit(account, "78"))
            .doesNotThrowAnyException();

        verifyNoInteractions(defendantAccountRepository);
    }

    @Test
    void validateAccountExistsInBusinessUnit_whenBusinessUnitMissing_throwsEntityNotFoundException() {
        // arrange
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(null)
            .build();

        // act / assert
        assertThatThrownBy(() -> service.validateAccountExistsInBusinessUnit(account, "78"))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Defendant Account not found in business unit 78");

        verifyNoInteractions(defendantAccountRepository);
    }

    @Test
    void validateAccountExistsInBusinessUnit_whenBusinessUnitIdMissing_throwsEntityNotFoundException() {
        // arrange
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId(null).build())
            .build();

        // act / assert
        assertThatThrownBy(() -> service.validateAccountExistsInBusinessUnit(account, "78"))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Defendant Account not found in business unit 78");

        verifyNoInteractions(defendantAccountRepository);
    }

    @Test
    void validateAccountExistsInBusinessUnit_whenBusinessUnitDoesNotMatch_throwsEntityNotFoundException() {
        // arrange
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 99).build())
            .build();

        // act / assert
        assertThatThrownBy(() -> service.validateAccountExistsInBusinessUnit(account, "78"))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Defendant Account not found in business unit 78");

        verifyNoInteractions(defendantAccountRepository);
    }

    @Test
    void validateFindByDefendantAccountIdForUpdate_defendantAccountFound() {
        // Arrange
        long defendantAccountId = 77L;
        DefendantAccountEntity entity = new DefendantAccountEntity();
        entity.setDefendantAccountId(defendantAccountId);

        when(defendantAccountRepository.findByDefendantAccountIdForUpdate(defendantAccountId))
            .thenReturn(Optional.of(entity));

        // Act
        DefendantAccountEntity result =
            service.getDefendantAccountByIdForUpdate(defendantAccountId);

        // Assert
        assertThat(result).isSameAs(entity);
        verify(defendantAccountRepository)
            .findByDefendantAccountIdForUpdate(defendantAccountId);
        verifyNoMoreInteractions(defendantAccountRepository);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDefendantAccountNotFound() {
        long defendantAccountId = 77L;
        when(defendantAccountRepository.findByDefendantAccountIdForUpdate(defendantAccountId))
            .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.getDefendantAccountByIdForUpdate(defendantAccountId)
        );

        assertEquals(
            "Defendant Account not found with id: 77",
            exception.getMessage()
        );
    }
}
