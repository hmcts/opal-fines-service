package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HistoryValueMaskingUtilTest {

    @Test
    void maskExceptLastTwo_masksAllButLastTwoCharacters() {
        // Arrange
        String value = "12345678";

        // Act
        String result = HistoryValueMaskingUtil.maskExceptLastTwo(value);

        // Assert
        assertEquals("******78", result);
    }

    @Test
    void maskExceptLastTwo_keepsNullEmptyAndShortValues() {
        // Arrange

        // Act

        // Assert
        assertNull(HistoryValueMaskingUtil.maskExceptLastTwo(null));
        assertEquals("", HistoryValueMaskingUtil.maskExceptLastTwo(""));
        assertEquals("1", HistoryValueMaskingUtil.maskExceptLastTwo("1"));
        assertEquals("12", HistoryValueMaskingUtil.maskExceptLastTwo("12"));
    }

    @Test
    void isBacsAmendmentAttribute_matchesBacsLabelsAndBankDataItemNames() {
        // Arrange

        // Act

        // Assert
        assertTrue(HistoryValueMaskingUtil.isBacsAmendmentAttribute("BACS Sort Code"));
        assertTrue(HistoryValueMaskingUtil.isBacsAmendmentAttribute("BACS Account Number"));
        assertTrue(HistoryValueMaskingUtil.isBacsAmendmentAttribute("BACS Account Name"));
        assertTrue(HistoryValueMaskingUtil.isBacsAmendmentAttribute("BACS Account Reference"));
        assertTrue(HistoryValueMaskingUtil.isBacsAmendmentAttribute("bank_account_number"));
        assertTrue(HistoryValueMaskingUtil.isBacsAmendmentAttribute("bank-account-holder-name"));
    }

    @Test
    void isBacsAmendmentAttribute_doesNotMatchNonBankAccountFields() {
        // Arrange

        // Act

        // Assert
        assertFalse(HistoryValueMaskingUtil.isBacsAmendmentAttribute("Account Number"));
        assertFalse(HistoryValueMaskingUtil.isBacsAmendmentAttribute("Pay by BACS"));
        assertFalse(HistoryValueMaskingUtil.isBacsAmendmentAttribute("Hold Pay Out"));
        assertFalse(HistoryValueMaskingUtil.isBacsAmendmentAttribute(null));
    }

    @Test
    void maskIfBacsAmendment_masksOnlyBacsAttributes() {
        // Arrange
        String bankAccountNumber = "12345678";
        String nonBankValue = "visible";

        // Act
        String maskedBankValue = HistoryValueMaskingUtil.maskIfBacsAmendment(
            "BACS Account Number",
            bankAccountNumber
        );
        String unmaskedValue = HistoryValueMaskingUtil.maskIfBacsAmendment("Hold Pay Out", nonBankValue);

        // Assert
        assertEquals("******78", maskedBankValue);
        assertEquals(nonBankValue, unmaskedValue);
    }
}
