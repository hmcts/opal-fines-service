package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.repository.PartyRepository;

@ExtendWith(MockitoExtension.class)
class PartyRepositoryServiceTest {

    @Mock
    private PartyRepository repository;

    @InjectMocks
    private PartyRepositoryService service;

    @Test
    void shouldReturnPartyWhenFoundById() {
        // given
        Long partyId = 1L;
        PartyEntity party = mock(PartyEntity.class);

        when(repository.findById(partyId)).thenReturn(Optional.of(party));

        // when
        PartyEntity result = service.findById(partyId);

        // then
        assertThat(result).isSameAs(party);
        verify(repository).findById(partyId);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPartyNotFound() {
        // given
        Long partyId = 99L;

        when(repository.findById(partyId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.findById(partyId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Party not found with id: " + partyId);

        verify(repository).findById(partyId);
    }

    @Test
    void shouldSaveParty() {
        // given
        PartyEntity party = mock(PartyEntity.class);

        when(repository.save(party)).thenReturn(party);

        // when
        PartyEntity result = service.save(party);

        // then
        assertThat(result).isSameAs(party);
        verify(repository).save(party);
    }

    @Test
    void shouldUpdatePartyByIdUsingMutator() {
        // given
        Long partyId = 5L;
        PartyEntity existing = mock(PartyEntity.class);
        PartyEntity updated = mock(PartyEntity.class);

        UnaryOperator<PartyEntity> mutator = p -> updated;

        when(repository.findById(partyId)).thenReturn(Optional.of(existing));
        when(repository.save(updated)).thenReturn(updated);

        // when
        PartyEntity result = service.updateById(partyId, mutator);

        // then
        assertThat(result).isSameAs(updated);
        verify(repository).findById(partyId);
        verify(repository).save(updated);
    }
}
