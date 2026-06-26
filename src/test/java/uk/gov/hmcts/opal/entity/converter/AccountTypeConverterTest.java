package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.opal.entity.enforcement.AccountType;


public class AccountTypeConverterTest {

    private AccountTypeConverter converter = new AccountTypeConverter();

    @ParameterizedTest(name = "#{index} enum value: {0}")
    @EnumSource(AccountType.class)
    void convertToDatabaseColumn(AccountType accountType) {
        String value = converter.convertToDatabaseColumn(accountType);

        assertEquals(accountType.getCode(), value);
    }

    @Test
    void convertNullToDatabaseColumn() {
        String value = converter.convertToDatabaseColumn(null);

        assertNull(value);
    }

    @Test
    void convertToEntityAttribute() {
        AccountType col = converter.convertToEntityAttribute("COL");
        AccountType youth = converter.convertToEntityAttribute("Y");
        AccountType nullAttr = converter.convertToEntityAttribute(null);

        assertAll(
            () -> assertEquals(AccountType.ADULT_COLLECTION_ORDER, col),
            () -> assertEquals(AccountType.YOUTH, youth),
            () -> assertNull(nullAttr)
        );

    }

}
