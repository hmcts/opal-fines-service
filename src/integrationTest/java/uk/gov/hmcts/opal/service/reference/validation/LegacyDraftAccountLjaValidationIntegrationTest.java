package uk.gov.hmcts.opal.service.reference.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("legacy")
@Transactional
class LegacyDraftAccountLjaValidationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DraftAccountReferenceValidationServiceLocator locator;

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-5741")
    @Sql(scripts = "classpath:db/insertData/draft_account_validation_ljas.sql")
    void validatesOriginatorAgainstSelectedLjaTable() {
        var service = locator.getService();
        String selectedName = "Legacy LJA";
        String otherName = "Opal LJA";
        assertDoesNotThrow(() -> service.validateReferences((short) 920, ljaAccount(32750, selectedName)));
        assertFailure(service, ljaAccount(32750, otherName),
            "does not match local justice area name '" + selectedName + "'");

        int selectedOnly = 32752;
        int otherOnly = 32751;
        assertDoesNotThrow(() -> service.validateReferences((short) 920, ljaAccount(selectedOnly, selectedName)));
        assertFailure(service, ljaAccount(otherOnly, otherName),
            "local justice area id " + otherOnly + " does not exist");

        int validType = 32754;
        int invalidType = 32753;
        assertDoesNotThrow(() -> service.validateReferences((short) 920, ljaAccount(validType, selectedName)));
        assertFailure(service, ljaAccount(invalidType, selectedName),
            "local justice area id " + invalidType + " does not exist");
    }

    private String ljaAccount(int id, String name) {
        return "{\"account_type\":\"Fine\",\"originator_type\":\"NEW\",\"originator_id\":" + id
            + ",\"originator_name\":\"" + name + "\"}";
    }

    private void assertFailure(DraftAccountReferenceValidationService service, String account, String expected) {
        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences((short) 920, account));
        assertTrue(exception.getMessage().contains(expected), exception.getMessage());
    }
}
