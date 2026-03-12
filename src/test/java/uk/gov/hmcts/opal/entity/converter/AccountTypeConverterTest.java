package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.AccountType;

public class AccountTypeConverterTest {

    private final AccountTypeConverter converter = new AccountTypeConverter();

    @Test
    void convertToDatabaseColumn_nonNull() {
        String dbValueCreditor = converter.convertToDatabaseColumn(AccountType.CREDITOR);
        String dbValueDefendant = converter.convertToDatabaseColumn(AccountType.DEFENDANT);

        assertEquals("Creditor", dbValueCreditor);
        assertEquals("Defendant", dbValueDefendant);
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        AccountType accountTypeCreditor = converter.convertToEntityAttribute("Creditor");
        AccountType accountTypeDefendant = converter.convertToEntityAttribute("Defendant");

        assertEquals(AccountType.CREDITOR, accountTypeCreditor);
        assertEquals(AccountType.DEFENDANT, accountTypeDefendant);
    }

    @Test
    void convertToEntityAttribute_null() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("Unknown"));
    }
}
