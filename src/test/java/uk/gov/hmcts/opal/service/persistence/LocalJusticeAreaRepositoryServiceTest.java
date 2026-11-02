package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;

@ExtendWith(MockitoExtension.class)
class LocalJusticeAreaRepositoryServiceTest {

    @Mock
    private LocalJusticeAreaRepository localJusticeAreaRepository;

    @InjectMocks
    private LocalJusticeAreaRepositoryService service;

    @Test
    void shouldReturnLocalJusticeAreaWhenIdExists() {
        // given
        short id = 10;
        LocalJusticeAreaEntity entity = mock(LocalJusticeAreaEntity.class);

        when(localJusticeAreaRepository.findById(id)).thenReturn(Optional.of(entity));

        // when
        Optional<LocalJusticeAreaEntity> result = service.getLjaById(id);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(entity);
        verify(localJusticeAreaRepository).findById(id);
    }

    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {
        // given
        short id = 99;

        when(localJusticeAreaRepository.findById(id)).thenReturn(Optional.empty());

        // when
        Optional<LocalJusticeAreaEntity> result = service.getLjaById(id);

        // then
        assertThat(result).isEmpty();
        verify(localJusticeAreaRepository).findById(id);
    }
}
