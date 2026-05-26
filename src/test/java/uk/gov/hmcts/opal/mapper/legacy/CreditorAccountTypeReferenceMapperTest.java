package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.common.CreditorAccountTypeReference;
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
        assertEquals("MN", mapped.getType());
        assertEquals("Minor Creditor", mapped.getDisplayName());
    }

    @Test
    void givenUnknownLegacyCreditorAccountTypeReference_whenToOpal_thenMapsNullDisplayName() {
        // Arrange
        uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference legacy =
            uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference.builder()
                .accountType("UNKNOWN")
                .build();

        // Act
        CreditorAccountTypeReference mapped = mapper.toOpal(legacy);

        // Assert
        assertNotNull(mapped);
        assertEquals("UNKNOWN", mapped.getType());
        assertNull(mapped.getDisplayName());
    }
}
