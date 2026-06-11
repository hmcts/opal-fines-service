package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.PartyAccountType;

public class AccountTypeConverterTest {

    private final AccountTypeConverter converter = new AccountTypeConverter();

    @Test
    void convertToDatabaseColumn_nonNull() {
        String dbValueCreditor = converter.convertToDatabaseColumn(PartyAccountType.CREDITOR);
        String dbValueDefendant = converter.convertToDatabaseColumn(PartyAccountType.DEFENDANT);

        assertEquals("Creditor", dbValueCreditor);
        assertEquals("Defendant", dbValueDefendant);
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        PartyAccountType accountTypeCreditor = converter.convertToEntityAttribute("Creditor");
        PartyAccountType accountTypeDefendant = converter.convertToEntityAttribute("Defendant");

        assertEquals(PartyAccountType.CREDITOR, accountTypeCreditor);
        assertEquals(PartyAccountType.DEFENDANT, accountTypeDefendant);
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
