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
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.OpalLocalJusticeAreaRepository;

@ExtendWith(MockitoExtension.class)
class OpalLocalJusticeAreaRepositoryServiceTest {

    @Mock
    private OpalLocalJusticeAreaRepository opalLocalJusticeAreaRepository;

    @InjectMocks
    private OpalLocalJusticeAreaRepositoryService service;

    @Test
    void shouldReturnLocalJusticeAreaWhenIdExists() {
        // given
        short id = 10;
        OpalLocalJusticeAreaEntity entity = mock(OpalLocalJusticeAreaEntity.class);

        when(opalLocalJusticeAreaRepository.findById(id)).thenReturn(Optional.of(entity));

        // when
        Optional<OpalLocalJusticeAreaEntity> result = service.getLjaById(id);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(entity);
        verify(opalLocalJusticeAreaRepository).findById(id);
    }

    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {
        // given
        short id = 99;

        when(opalLocalJusticeAreaRepository.findById(id)).thenReturn(Optional.empty());

        // when
        Optional<OpalLocalJusticeAreaEntity> result = service.getLjaById(id);

        // then
        assertThat(result).isEmpty();
        verify(opalLocalJusticeAreaRepository).findById(id);
    }
}
