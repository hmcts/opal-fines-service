package uk.gov.hmcts.opal.service.reference.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.repository.ResultLegacyRepository;
import uk.gov.hmcts.opal.repository.ResultOpalRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("legacy")
@Transactional
@JiraStory("PO-11027")
@JiraEpic("PO-5741")
class LegacyDraftAccountResultValidationIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private DraftAccountReferenceValidationServiceLocator locator;
    @Autowired
    private LegacyDraftAccountReferenceValidationService expectedService;
    @Autowired
    private ResultOpalRepository resultOpalRepository;
    @Autowired
    private ResultLegacyRepository resultLegacyRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void createResults() {
        // DVEN is an enforcement in legacy, but not in Opal.
        resultLegacyRepository.save(ResultLegacyEntity.builder().resultId("DVEN").resultTitle("DVEN")
            .resultTitleCy("DVEN").resultType(ResultType.RESULT).active(true).enforcement(true)
            .imposition(false).impositionAccruing(false).build());
        resultOpalRepository.save(ResultOpalEntity.builder().resultId("DVEN").resultTitle("DVEN")
            .resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(false).build());
        // DVIM is an imposition in legacy, but not in Opal.
        resultLegacyRepository.save(ResultLegacyEntity.builder().resultId("DVIM").resultTitle("DVIM")
            .resultTitleCy("DVIM").resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(true).impositionAccruing(false).build());
        resultOpalRepository.save(ResultOpalEntity.builder().resultId("DVIM").resultTitle("DVIM")
            .resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(false).build());
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void selectsServiceAndValidatesResultsFromItsTable() {
        assertSame(expectedService, locator.getService());
        assertDoesNotThrow(() -> service().validateReferences((short) 920, enforcementAccount("DVEN")));
        assertDoesNotThrow(() -> service().validateReferences((short) 920, impositionAccount("DVIM", "")));
        assertFailure(enforcementAccount("DVIM"), "is not an enforcement result");
        assertFailure(impositionAccount("DVEN", ""), "is not an imposition result");
    }

    @Test
    void rejectsInactiveResults() {
        var enforcement = resultLegacyRepository.findById("DVEN").orElseThrow();
        var imposition = resultLegacyRepository.findById("DVIM").orElseThrow();
        enforcement.setActive(false);
        imposition.setActive(false);
        entityManager.flush();
        entityManager.clear();
        assertFailure(enforcementAccount("DVEN"), "is not an active result");
        assertFailure(impositionAccount("DVIM", ""), "is not active");
    }

    @Test
    void rejectsResultsPresentOnlyInOtherMode() {
        resultOpalRepository.save(ResultOpalEntity.builder().resultId("OTHREN").resultTitle("OTHREN")
            .resultType(ResultType.RESULT).active(true).enforcement(true)
            .imposition(false).build());
        resultOpalRepository.save(ResultOpalEntity.builder().resultId("OTHRIM").resultTitle("OTHRIM")
            .resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(true).build());
        entityManager.flush();
        entityManager.clear();
        assertFailure(enforcementAccount("OTHREN"), "result id OTHREN does not exist");
        assertFailure(impositionAccount("OTHRIM", ""), "result id OTHRIM does not exist");
    }

    @Test
    void appliesCreditorRuleFromSelectedResultTable() {
        resultLegacyRepository.save(ResultLegacyEntity.builder().resultId("DVRULE").resultTitle("DVRULE")
            .resultTitleCy("DVRULE").resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(true).impositionAccruing(false).impositionCreditor(ImpositionCreditor.ANY).build());
        resultOpalRepository.save(ResultOpalEntity.builder().resultId("DVRULE").resultTitle("DVRULE")
            .resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(true).impositionCreditor(ImpositionCreditor.CF).build());
        entityManager.flush();
        entityManager.clear();
        assertDoesNotThrow(() -> service().validateReferences((short) 32767,
            impositionAccount("DVRULE", ",\"minor_creditor\":{}")));
        InvalidReferenceValidationException ex = assertThrows(InvalidReferenceValidationException.class,
            () -> service().validateReferences((short) 32767, impositionAccount("DVRULE", "")));
        assertTrue(ex.getMessage().contains("minor creditor or valid major creditor"), ex.getMessage());
    }

    @Test
    void aggregatesFailures() {
        String account = """
            {"offences":[{"offence_id":999999999,"impositions":[{"result_id":"MISSING1"}]}],
             "payment_terms":{"enforcements":[{"result_id":"MISSING2"}]}}
            """;
        InvalidReferenceValidationException ex = assertThrows(InvalidReferenceValidationException.class,
            () -> service().validateReferences((short) 920, account));
        assertTrue(ex.getMessage().contains("3 error(s)"), ex.getMessage());
        assertTrue(ex.getMessage().contains("account.offences[0].offence_id"), ex.getMessage());
        assertTrue(ex.getMessage().contains("$.offences[0].impositions[0].result_id"), ex.getMessage());
        assertTrue(ex.getMessage().contains("$.payment_terms.enforcements[0].result_id"), ex.getMessage());
    }

    private DraftAccountReferenceValidationService service() {
        return locator.getService();
    }

    private String impositionAccount(String resultId, String extra) {
        return "{\"offences\":[{\"impositions\":[{\"result_id\":\"" + resultId + "\"" + extra + "}]}]}";
    }

    private String enforcementAccount(String resultId) {
        return "{\"payment_terms\":{\"enforcements\":[{\"result_id\":\"" + resultId + "\"}]}}";
    }

    private void assertFailure(String account, String expected) {
        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service().validateReferences((short) 920, account));
        assertTrue(exception.getMessage().contains(expected), exception.getMessage());
    }
}
