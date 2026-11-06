package uk.gov.hmcts.opal.service.refdata.lja;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@ExtendWith(MockitoExtension.class)
class LocalJusticeAreaRefDataHandlerTest {

    @Mock
    private LocalJusticeAreaRepository repository;

    @Mock
    private LocalJusticeAreaRefDataMapper mapper;

    @InjectMocks
    private LocalJusticeAreaRefDataHandler handler;

    @Test
    void exposesReferenceDataHandlerMetadata() {
        // Given
        // When
        assertThat(handler.refDataType()).isEqualTo("LJA");

        // Then
        assertThat(handler.payloadType()).isEqualTo(LjaRecord.class);
        assertThat(handler.cachesToClear()).containsExactly("opalLjaReferenceDataCache");
    }

    @Test
    void findsEntityUsingLjaCode() {
        // Given
        LjaRecord record = validRecord();
        LocalJusticeAreaEntity entity = new LocalJusticeAreaEntity();
        when(repository.findById((short) 1234)).thenReturn(Optional.of(entity));

        // When
        assertThat(handler.findEntity(record)).containsSame(entity);

        // Then
        verify(repository).findById((short) 1234);
    }

    @Test
    void createsNewEntityWithMappedValues() {
        // Given
        LjaRecord record = validRecord();

        // When
        LocalJusticeAreaEntity entity = handler.createEntityFromDto(record);

        // Then
        assertThat(entity.getLocalJusticeAreaId()).isEqualTo((short) 1234);
        assertThat(entity.getEndDate()).isNull();
        verify(mapper).updateEntityFromDto(record, entity);
    }

    @Test
    void savesEntity() {
        // Given
        LocalJusticeAreaEntity entity = new LocalJusticeAreaEntity();
        when(repository.save(entity)).thenReturn(entity);

        // When
        assertThat(handler.saveEntity(entity)).isSameAs(entity);

        // Then
        verify(repository).save(entity);
    }

    @Test
    void deletesEntityUsingBusinessKey() {
        // Given
        String businessKey = "01234";

        // When
        handler.deleteEntity(businessKey);

        // Then
        verify(repository).deleteById((short) 1234);
    }

    @Test
    void rejectsInvalidDeleteBusinessKey() {
        // Given
        String businessKey = "not-a-number";

        // When / Then
        assertThatThrownBy(() -> handler.deleteEntity(businessKey))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid business key received: not-a-number");
    }

    @Test
    void updatesEntityUsingMapper() {
        // Given
        LjaRecord record = validRecord();
        LocalJusticeAreaEntity entity = new LocalJusticeAreaEntity();

        // When
        handler.updateEntityFromDto(record, entity);

        // Then
        verify(mapper).updateEntityFromDto(record, entity);
    }

    @Test
    void returnsLjaCodeAsBusinessKey() {
        // Given
        LjaRecord record = validRecord();

        // When
        String businessKey = handler.getBusinessKey(record);

        // Then
        assertThat(businessKey).isEqualTo("1234");
    }

    @Test
    void validateDto_acceptsValidLjaRecord() {
        // Given
        LjaRecord record = validRecord();

        // When / Then
        assertThatCode(() -> handler.validateDto(record)).doesNotThrowAnyException();
    }

    @Test
    void validateDto_rejectsLjaCodeThatIsNotFourDigits() {
        // Given
        LjaRecord record = validRecord();
        record.setLjaCode("12A4");

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("lja_code must contain exactly four digits");
    }

    @Test
    void validateDto_rejectsAddressLineThatExceedsMaximumLength() {
        // Given
        LjaRecord record = validRecord();
        record.getAddresses().getFirst().setAddressLine1("A".repeat(36));

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("address_line_1 must not exceed 35 characters");
    }

    @Test
    void validateDto_requiresExactlyOneCourtAddress() {
        // Given
        LjaRecord record = validRecord();
        record.getAddresses().getFirst().setAddressType("Financial Office Address");

        // When / Then
        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("addresses must contain exactly one Court Address");
    }

    private static LjaRecord validRecord() {
        return LjaRecord.builder()
            .ljaCode("1234")
            .ljaName("Test LJA")
            .addresses(List.of(Address.builder()
                .addressType("Court Address")
                .addressLine1("Court address")
                .postCode("GIR 0AA")
                .build()))
            .build();
    }
}
