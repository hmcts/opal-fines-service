package uk.gov.hmcts.opal.service.refdata.prison;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.repository.PrisonRepository;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@ExtendWith(MockitoExtension.class)
class PrisonRefDataHandlerTest {

    @Mock
    private PrisonRepository repository;

    @Mock
    private PrisonRefDataMapper mapper;

    @InjectMocks
    private PrisonRefDataHandler handler;

    @Test
    void shouldExposeReferenceDataHandlerMetadata() {
        // Given
        // When
        // Then
        assertThat(handler.refDataType()).isEqualTo("Prison");
        assertThat(handler.payloadType()).isEqualTo(PrisonRecord.class);
        assertThat(handler.cachesToClear()).containsExactly("prisonReferenceDataCache");
    }

    @Test
    void shouldFindEntityByName() {
        // Given
        PrisonRecord record = validRecord();
        PrisonEntity entity = new PrisonEntity();
        when(repository.findByName(record.getName())).thenReturn(Optional.of(entity));

        // When
        Optional<PrisonEntity> result = handler.findEntity(record);

        // Then
        assertThat(result).containsSame(entity);
        verify(repository).findByName(record.getName());
    }

    @Test
    void shouldCreateEntityAndDelegateMapping() {
        // Given
        PrisonRecord record = validRecord();

        // When
        PrisonEntity result = handler.createEntityFromDto(record);

        // Then
        assertThat(result.getPrisonId()).isNull();
        verify(mapper).updateEntityFromDto(record, result);
    }

    @Test
    void shouldSaveEntity() {
        // Given
        PrisonEntity entity = new PrisonEntity();
        when(repository.save(entity)).thenReturn(entity);

        // When
        PrisonEntity result = handler.saveEntity(entity);

        // Then
        assertThat(result).isSameAs(entity);
        verify(repository).save(entity);
    }

    @Test
    void shouldDeleteEntityByName() {
        // Given
        String businessKey = "Integration Prison";

        // When
        handler.deleteEntity(businessKey);

        // Then
        verify(repository).deleteByName(businessKey);
    }

    @Test
    void shouldUpdateEntityUsingMapper() {
        // Given
        PrisonRecord record = validRecord();
        PrisonEntity entity = new PrisonEntity();

        // When
        handler.updateEntityFromDto(record, entity);

        // Then
        verify(mapper).updateEntityFromDto(record, entity);
    }

    @Test
    void shouldAcceptValidRecord() {
        // Given
        PrisonRecord record = validRecord();

        // When / Then
        assertThatCode(() -> handler.validateDto(record)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectNullRecord() {
        // Given
        // When / Then
        assertThatThrownBy(() -> handler.validateDto(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("prisonRecord must not be null");
    }

    @Test
    void shouldRejectMissingName() {
        // Given
        PrisonRecord record = validRecord();
        record.setName(null);

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name is required");
    }

    @Test
    void shouldRejectNameThatExceedsDatabaseMaximum() {
        // Given
        PrisonRecord record = validRecord();
        record.setName("A".repeat(201));

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name must not exceed 200 characters");
    }

    @Test
    void shouldRejectAddressFieldThatExceedsDatabaseMaximum() {
        // Given
        PrisonRecord record = validRecord();
        record.getAddresses().getFirst().setAddressLine1("A".repeat(36));

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("address_line_1 must not exceed 35 characters");
    }

    @Test
    void shouldRejectAddressTypeThatExceedsSchemaMaximum() {
        // Given
        PrisonRecord record = validRecord();
        record.getAddresses().getFirst().setAddressType("A".repeat(101));

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("address_type must not exceed 100 characters");
    }

    private static PrisonRecord validRecord() {
        return PrisonRecord.builder()
            .name("Integration Prison")
            .addresses(List.of(Address.builder()
                .addressType("PRIS")
                .addressLine1("Line 1")
                .postCode("IP1 1AA")
                .build()))
            .build();
    }
}
