package uk.gov.hmcts.opal.service.persistence;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;

@ExtendWith(MockitoExtension.class)
class DefendantAccountPartiesRepositoryServiceTest {

    @Mock
    private DefendantAccountPartiesRepository repository;

    @InjectMocks
    private DefendantAccountPartiesRepositoryService service;

    @Test
    void delete_whenEntityExists_callsRepository() {
        DefendantAccountPartiesEntity entity = mock(DefendantAccountPartiesEntity.class);

        service.delete(entity);

        verify(repository).delete(entity);
    }

    @Test
    void delete_whenEntityIsNull_doesNotCallRepository() {
        service.delete(null);

        verifyNoInteractions(repository);
    }
}
