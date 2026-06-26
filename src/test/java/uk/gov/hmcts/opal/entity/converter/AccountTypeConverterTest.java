package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
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
        try (MockedStatic<AccountType> accountType = mockStatic(AccountType.class)) {
            converter.convertToEntityAttribute("COL");
            accountType.verify(() -> AccountType.getByCode("COL"), times(1));
        }
    }

    @Test
    void convertToNullEntityAttribute() {
        try (MockedStatic<AccountType> accountType = mockStatic(AccountType.class)) {
            AccountType entityAttr = converter.convertToEntityAttribute(null);

            assertNull(entityAttr);
            accountType.verifyNoInteractions();
        }
    }
}
