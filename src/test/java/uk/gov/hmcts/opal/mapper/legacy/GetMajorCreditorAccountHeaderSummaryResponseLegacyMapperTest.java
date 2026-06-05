package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse.MajorCreditorLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReferenceCommon;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class GetMajorCreditorAccountHeaderSummaryResponseLegacyMapperTest extends AbstractMapperTest {

    @Autowired
    private GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper mapper;

    @Test
    void toOpal_mapsLegacyHeaderSummaryResponse() {
        GetMajorCreditorAccountHeaderSummaryLegacyResponse legacy =
            GetMajorCreditorAccountHeaderSummaryLegacyResponse.builder()
                .majorCreditor(MajorCreditorLegacy.builder()
                                   .creditorAccountId(123L)
                                   .accountVersion(7L)
                                   .accountNumber("87654321")
                                   .name("Major Creditor Ltd")
                                   .accountReference(CreditorAccountTypeReference.builder()
                                                         .accountType("MJ")
                                                         .build())
                                   .build())
                .businessUnitDetails(BusinessUnitSummary.builder()
                                         .businessUnitId("77")
                                         .businessUnitName("Camberwell Green")
                                         .welshSpeaking("N")
                                         .build())
                .awaitingPayout(new BigDecimal("123.45"))
                .build();

        GetMajorCreditorAccountHeaderSummaryResponse result = mapper.toOpal(legacy);

        assertEquals(123L, result.getMajorCreditor().getCreditorAccountId());
        assertEquals("87654321", result.getMajorCreditor().getAccountNumber());
        assertEquals("Major Creditor Ltd", result.getMajorCreditor().getName());
        assertEquals("MJ", result.getMajorCreditor().getAccountReference().getAccountType().getValue());
        assertEquals("Major Creditor",
                     result.getMajorCreditor().getAccountReference().getDisplayName().getValue());
        assertEquals("77", result.getBusinessUnitDetails().getBusinessUnitId());
        assertEquals("Camberwell Green", result.getBusinessUnitDetails().getBusinessUnitName());
        assertEquals("N", result.getBusinessUnitDetails().getWelshSpeaking());
        assertEquals(new BigDecimal("123.45"), result.getAwaitingPayout());
        assertEquals(7L, result.getVersion().longValue());
    }

    @Test
    void toOpal_returnsNullWhenLegacyHeaderSummaryResponseIsNull() {
        assertNull(mapper.toOpal((GetMajorCreditorAccountHeaderSummaryLegacyResponse) null));
    }

    @Test
    void toOpal_returnsNullWhenMajorCreditorIsNull() {
        assertNull(mapper.toOpal((MajorCreditorLegacy) null));
    }

    @Test
    void toOpal_returnsNullWhenBusinessUnitSummaryIsNull() {
        assertNull(mapper.toOpal((BusinessUnitSummary) null));
    }

    @Test
    void toOpal_returnsNullWhenCreditorAccountTypeReferenceIsNull() {
        assertNull(mapper.toOpal((CreditorAccountTypeReference) null));
    }

    @Test
    void toOpal_mapsNullNestedValues() {
        GetMajorCreditorAccountHeaderSummaryLegacyResponse legacy =
            GetMajorCreditorAccountHeaderSummaryLegacyResponse.builder()
                .majorCreditor(MajorCreditorLegacy.builder()
                                   .creditorAccountId(123L)
                                   .accountReference(null)
                                   .build())
                .businessUnitDetails(null)
                .awaitingPayout(new BigDecimal("123.45"))
                .build();

        GetMajorCreditorAccountHeaderSummaryResponse result = mapper.toOpal(legacy);

        assertEquals(123L, result.getMajorCreditor().getCreditorAccountId());
        assertNull(result.getMajorCreditor().getAccountReference());
        assertNull(result.getBusinessUnitDetails());
        assertEquals(new BigDecimal("123.45"), result.getAwaitingPayout());
        assertNull(result.getVersion());
    }

    @Test
    void toOpal_mapsAccountReferenceWithNullAccountType() {
        CreditorAccountTypeReferenceCommon result = mapper.toOpal(CreditorAccountTypeReference.builder().build());

        assertNull(result.getAccountType());
        assertNull(result.getDisplayName());
    }
}
