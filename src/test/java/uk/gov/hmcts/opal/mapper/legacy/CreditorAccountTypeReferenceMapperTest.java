package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class CreditorAccountTypeReferenceMapperTest extends AbstractMapperTest {

    @Autowired
    private CreditorAccountTypeReferenceMapper mapper;

    @Test
    void givenLegacyCreditorAccountTypeReference_whenToOpal_thenMapsExpectedFields() {
        // Arrange
        uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference legacy =
            uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference.builder()
                .accountType("MN")
                .build();

        // Act
        CreditorAccountTypeReference mapped = mapper.toOpal(legacy);

        // Assert
        assertNotNull(mapped);
        assertEquals(CreditorAccountTypeReference.TypeEnum.MN, mapped.getType());
        assertEquals(CreditorAccountTypeReference.DisplayNameEnum.MINOR_CREDITOR, mapped.getDisplayName());
    }

    @Test
    void givenUnknownLegacyCreditorAccountTypeReference_whenToOpal_thenThrowsIllegalArgumentException() {
        // Arrange
        uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference legacy =
            uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference.builder()
                .accountType("UNKNOWN")
                .build();

        // Act
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> mapper.toOpal(legacy));
    }
}
