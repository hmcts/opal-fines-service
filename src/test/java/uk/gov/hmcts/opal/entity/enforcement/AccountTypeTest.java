package uk.gov.hmcts.opal.entity.enforcement;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AccountTypeTest {

    @Test
    void getByCode() {
        AccountType col = AccountType.getByCode("COL");
        AccountType youth = AccountType.getByCode("Y");

        assertAll(
            () -> assertEquals(AccountType.ADULT_COLLECTION_ORDER, col),
            () -> assertEquals(AccountType.YOUTH, youth)
        );
    }

    @Test
    void getByCode_Unknown_Throws() {
        assertThrows(IllegalArgumentException.class, () -> AccountType.getByCode("foo"));
    }
}
