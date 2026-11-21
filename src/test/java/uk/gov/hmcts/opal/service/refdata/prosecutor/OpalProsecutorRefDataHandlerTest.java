package uk.gov.hmcts.opal.service.refdata.prosecutor;

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
import uk.gov.hmcts.opal.entity.OpalProsecutorEntity;
import uk.gov.hmcts.opal.repository.OpalProsecutorRepository;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@ExtendWith(MockitoExtension.class)
class OpalProsecutorRefDataHandlerTest {

    @Mock
    private OpalProsecutorRepository repository;

    @Mock
    private OpalProsecutorRefDataMapper mapper;

    @InjectMocks
    private OpalProsecutorRefDataHandler handler;

    @Test
    void exposesReferenceDataHandlerMetadata() {
        // When / Then
        assertThat(handler.refDataType()).isEqualTo("Prosecutor");
        assertThat(handler.payloadType()).isEqualTo(OpalProsecutorRecord.class);
        assertThat(handler.cachesToClear()).containsExactly("opalProsecutorReferenceDataCache");
    }

    @Test
    void findsEntityUsingProsecutorCode() {
        // Given
        OpalProsecutorRecord record = validRecord();
        OpalProsecutorEntity entity = new OpalProsecutorEntity();
        when(repository.findById("1234")).thenReturn(Optional.of(entity));

        // When
        assertThat(handler.findEntity(record)).containsSame(entity);

        // Then
        verify(repository).findById("1234");
    }

    @Test
    void createsEntityAndDelegatesMapping() {
        // Given
        OpalProsecutorRecord record = validRecord();

        // When
        OpalProsecutorEntity entity = handler.createEntityFromDto(record);

        // Then
        assertThat(entity.getProsecutorCode()).isEqualTo("1234");
        verify(mapper).updateEntityFromDto(record, entity);
    }

    @Test
    void updatesEntityUsingMapper() {
        // Given
        OpalProsecutorRecord record = validRecord();
        OpalProsecutorEntity entity = new OpalProsecutorEntity();

        // When
        handler.updateEntityFromDto(record, entity);

        // Then
        verify(mapper).updateEntityFromDto(record, entity);
    }

    @Test
    void savesEntity() {
        // Given
        OpalProsecutorEntity entity = new OpalProsecutorEntity();
        when(repository.save(entity)).thenReturn(entity);

        // When
        assertThat(handler.saveEntity(entity)).isSameAs(entity);

        // Then
        verify(repository).save(entity);
    }

    @Test
    void deletesEntityUsingBusinessKey() {
        // When
        handler.deleteEntity("1234");

        // Then
        verify(repository).deleteById("1234");
    }

    @Test
    void deleteEntity_rejectsNullBusinessKey() {
        assertThatThrownBy(() -> handler.deleteEntity(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("prosecutor_code must not be null");
    }

    @Test
    void deleteEntity_rejectsBusinessKeyThatExceedsDatabaseLength() {
        assertThatThrownBy(() -> handler.deleteEntity("12345"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("prosecutor_code must not exceed 4 characters");
    }

    @Test
    void returnsProsecutorCodeAsBusinessKey() {
        assertThat(handler.getBusinessKey(validRecord())).isEqualTo("1234");
    }

    @Test
    void getBusinessKey_rejectsMissingProsecutorCode() {
        OpalProsecutorRecord record = validRecord();
        record.setProsecutorCode(null);

        assertThatThrownBy(() -> handler.getBusinessKey(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("prosecutor_code must not be null");
    }

    @Test
    void validateDto_acceptsValidProsecutorRecord() {
        assertThatCode(() -> handler.validateDto(validRecord())).doesNotThrowAnyException();
    }

    @Test
    void validateDto_acceptsMissingAddresses() {
        OpalProsecutorRecord record = validRecord();
        record.setAddresses(null);

        assertThatCode(() -> handler.validateDto(record)).doesNotThrowAnyException();
    }

    @Test
    void validateDto_rejectsProsecutorCodeThatExceedsDatabaseLength() {
        OpalProsecutorRecord record = validRecord();
        record.setProsecutorCode(12345);

        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("prosecutor_code must not exceed 4 characters");
    }

    @Test
    void validateDto_rejectsMissingName() {
        OpalProsecutorRecord record = validRecord();
        record.setName(null);

        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name must not be null");
    }

    @Test
    void validateDto_rejectsNameThatExceedsDatabaseLength() {
        OpalProsecutorRecord record = validRecord();
        record.setName("A".repeat(201));

        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name must not exceed 200 characters");
    }

    @Test
    void validateDto_requiresExactlyOneProsecutorAddressWhenAddressesAreProvided() {
        OpalProsecutorRecord record = validRecord();
        record.setAddresses(List.of(Address.builder().addressType("Court Address").build()));

        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("addresses must contain exactly one Prosecutor address");
    }

    @Test
    void validateDto_rejectsMultipleProsecutorAddresses() {
        OpalProsecutorRecord record = validRecord();
        record.setAddresses(List.of(prosecutorAddress(), prosecutorAddress()));

        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("addresses must contain exactly one Prosecutor address");
    }

    @Test
    void validateDto_rejectsAddressLineOneThatExceedsDatabaseLength() {
        OpalProsecutorRecord record = validRecord();
        record.getAddresses().getFirst().setAddressLine1("A".repeat(61));

        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("address_line_1 must not exceed 60 characters");
    }

    @Test
    void validateDto_rejectsOtherAddressLineThatExceedsDatabaseLength() {
        OpalProsecutorRecord record = validRecord();
        record.getAddresses().getFirst().setAddressLine2("A".repeat(36));

        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("address_line_2 must not exceed 35 characters");
    }

    @Test
    void validateDto_rejectsPostcodeThatExceedsDatabaseLength() {
        OpalProsecutorRecord record = validRecord();
        record.getAddresses().getFirst().setPostCode("A".repeat(9));

        assertThatThrownBy(() -> handler.validateDto(record))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("postcode must not exceed 8 characters");
    }

    @Test
    void validateDto_ignoresNonProsecutorAddressFields() {
        OpalProsecutorRecord record = validRecord();
        Address otherAddress = Address.builder()
            .addressType("Court Address")
            .addressLine1("A".repeat(61))
            .postCode("A".repeat(9))
            .build();
        record.setAddresses(List.of(otherAddress, prosecutorAddress()));

        assertThatCode(() -> handler.validateDto(record)).doesNotThrowAnyException();
    }

    private static OpalProsecutorRecord validRecord() {
        return OpalProsecutorRecord.builder()
            .prosecutorCode(1234)
            .name("Test Prosecutor")
            .addresses(List.of(prosecutorAddress()))
            .build();
    }

    private static Address prosecutorAddress() {
        return Address.builder()
            .addressType(OpalProsecutorRecord.PROSECUTOR_ADDRESS_TYPE)
            .addressLine1("Prosecutor address")
            .postCode("GIR 0AA")
            .build();
    }
}
