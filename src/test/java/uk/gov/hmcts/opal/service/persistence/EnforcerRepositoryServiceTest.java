package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.repository.EnforcerRepository;

@ExtendWith(MockitoExtension.class)
class EnforcerRepositoryServiceTest {

    @Mock
    private EnforcerRepository enforcerRepository;

    @InjectMocks
    private EnforcerRepositoryService service;

    @Test
    void shouldReturnEnforcerWhenIdExistsAndEnforcerIdIsNotNull() {
        // given
        Long enforcerId = 1L;
        EnforcerEntity entity = mock(EnforcerEntity.class);

        when(entity.getEnforcerId()).thenReturn(enforcerId);
        when(enforcerRepository.findById(enforcerId)).thenReturn(Optional.of(entity));

        // when
        Optional<EnforcerEntity> result = service.findById(enforcerId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(entity);
        verify(enforcerRepository).findById(enforcerId);
    }

    @Test
    void shouldReturnEmptyWhenEnforcerFoundButEnforcerIdIsNull() {
        // given
        Long enforcerId = 2L;
        EnforcerEntity entity = mock(EnforcerEntity.class);

        when(entity.getEnforcerId()).thenReturn(null);
        when(enforcerRepository.findById(enforcerId)).thenReturn(Optional.of(entity));

        // when
        Optional<EnforcerEntity> result = service.findById(enforcerId);

        // then
        assertThat(result).isEmpty();
        verify(enforcerRepository).findById(enforcerId);
    }

    @Test
    void shouldReturnEmptyAndNotCallRepositoryWhenEnforcerIdIsNull() {
        // when
        Optional<EnforcerEntity> result = service.findById(null);

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(enforcerRepository);
    }
}
