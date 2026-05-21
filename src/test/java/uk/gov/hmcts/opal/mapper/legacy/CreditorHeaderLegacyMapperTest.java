package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.CreditorHeader;
import uk.gov.hmcts.opal.dto.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.CreditorHeaderLegacy;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class CreditorHeaderLegacyMapperTest extends AbstractMapperTest {

    @Autowired
    private CreditorHeaderLegacyMapper mapper;

    @MockitoBean
    private CreditorAccountTypeReferenceMapper creditorAccountTypeReferenceMapper;

    @Test
    void givenLegacyCreditorHeader_whenToOpal_thenMapsExpectedFieldsAndCallsSubmapper() {
        // Arrange
        uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference accountType =
            uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference.builder()
                .accountType("MN")
                .build();
        CreditorAccountTypeReference mappedAccountType = CreditorAccountTypeReference.builder()
            .type("MN")
            .displayName("Minor Creditor")
            .build();
        CreditorHeaderLegacy legacy = CreditorHeaderLegacy.builder()
            .accountVersion(3)
            .accountId("12345")
            .accountNumber("ACC001")
            .accountType(accountType)
            .hasAssociatedDefendant(true)
            .build();

        when(creditorAccountTypeReferenceMapper.toOpal(accountType)).thenReturn(mappedAccountType);

        // Act
        CreditorHeader mapped = mapper.toOpal(legacy);

        // Assert
        assertNotNull(mapped);
        assertEquals("12345", mapped.getAccountId());
        assertEquals("ACC001", mapped.getAccountNumber());
        assertEquals(true, mapped.getHasAssociatedDefendant());
        assertEquals(mappedAccountType, mapped.getAccountType());
        verify(creditorAccountTypeReferenceMapper).toOpal(accountType);
    }
}
