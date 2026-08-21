package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.CreditorHeaderLegacy;
import uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountHeaderSummaryResponseCreditor;

@ExtendWith(MockitoExtension.class)
class CreditorHeaderLegacyMapperTest {

    @InjectMocks
    private CreditorHeaderLegacyMapperImpl mapper;

    @Mock
    private CreditorAccountTypeReferenceMapper creditorAccountTypeReferenceMapper;

    @Test
    void givenLegacyCreditorHeader_whenToOpal_thenMapsExpectedFields() {
        // Arrange
        uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference accountType =
            uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference.builder()
                .accountType("MN")
                .build();
        CreditorHeaderLegacy legacy = CreditorHeaderLegacy.builder()
            .accountVersion(BigInteger.valueOf(3))
            .accountId("12345")
            .accountNumber("ACC001")
            .accountType(accountType)
            .hasAssociatedDefendant(true)
            .build();
        CreditorAccountTypeReference mappedAccountType = new CreditorAccountTypeReference()
            .type(CreditorAccountTypeReference.TypeEnum.MN)
            .displayName(CreditorAccountTypeReference.DisplayNameEnum.MINOR_CREDITOR);
        when(creditorAccountTypeReferenceMapper.toOpal(accountType)).thenReturn(mappedAccountType);

        // Act
        MinorCreditorAccountHeaderSummaryResponseCreditor mapped = mapper.toOpal(legacy);

        // Assert
        assertNotNull(mapped);
        assertEquals("12345", mapped.getAccountId());
        assertEquals("ACC001", mapped.getAccountNumber());
        assertEquals(true, mapped.getHasAssociatedDefendant());
        assertEquals(CreditorAccountTypeReference.TypeEnum.MN, mapped.getAccountType().getType());
    }
}
