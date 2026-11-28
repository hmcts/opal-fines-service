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
import org.springframework.test.context.jdbc.Sql;
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

@ActiveProfiles("opal")
@Transactional
@JiraStory("PO-11027")
@JiraEpic("PO-5741")
class OpalDraftAccountResultValidationIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private DraftAccountReferenceValidationServiceLocator locator;
    @Autowired
    private OpalDraftAccountReferenceValidationService expectedService;
    @Autowired
    private ResultOpalRepository resultOpalRepository;
    @Autowired
    private ResultLegacyRepository resultLegacyRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void createResults() {
        // DVEN is an enforcement in Opal, but not in legacy.
        resultOpalRepository.save(ResultOpalEntity.builder().resultId("DVEN").resultTitle("DVEN")
            .resultType(ResultType.RESULT).active(true).enforcement(true)
            .imposition(false).build());
        resultLegacyRepository.save(ResultLegacyEntity.builder().resultId("DVEN").resultTitle("DVEN")
            .resultTitleCy("DVEN").resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(false).impositionAccruing(false).build());
        // DVIM is an imposition in Opal, but not in legacy.
        resultOpalRepository.save(ResultOpalEntity.builder().resultId("DVIM").resultTitle("DVIM")
            .resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(true).build());
        resultLegacyRepository.save(ResultLegacyEntity.builder().resultId("DVIM").resultTitle("DVIM")
            .resultTitleCy("DVIM").resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(false).impositionAccruing(false).build());
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
        var enforcement = resultOpalRepository.findById("DVEN").orElseThrow();
        var imposition = resultOpalRepository.findById("DVIM").orElseThrow();
        enforcement.setActive(false);
        imposition.setActive(false);
        entityManager.flush();
        entityManager.clear();
        assertFailure(enforcementAccount("DVEN"), "is not an active result");
        assertFailure(impositionAccount("DVIM", ""), "is not active");
    }

    @Test
    void rejectsResultsPresentOnlyInOtherMode() {
        resultLegacyRepository.save(ResultLegacyEntity.builder().resultId("OTHREN").resultTitle("OTHREN")
            .resultTitleCy("OTHREN").resultType(ResultType.RESULT).active(true).enforcement(true)
            .imposition(false).impositionAccruing(false).build());
        resultLegacyRepository.save(ResultLegacyEntity.builder().resultId("OTHRIM").resultTitle("OTHRIM")
            .resultTitleCy("OTHRIM").resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(true).impositionAccruing(false).build());
        entityManager.flush();
        entityManager.clear();
        assertFailure(enforcementAccount("OTHREN"), "result id OTHREN does not exist");
        assertFailure(impositionAccount("OTHRIM", ""), "result id OTHRIM does not exist");
    }

    @Test
    void appliesCreditorRuleFromSelectedResultTable() {
        resultOpalRepository.save(ResultOpalEntity.builder().resultId("DVRULE").resultTitle("DVRULE")
            .resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(true).impositionCreditor(ImpositionCreditor.ANY).build());
        resultLegacyRepository.save(ResultLegacyEntity.builder().resultId("DVRULE").resultTitle("DVRULE")
            .resultTitleCy("DVRULE").resultType(ResultType.RESULT).active(true).enforcement(false)
            .imposition(true).impositionAccruing(false).impositionCreditor(ImpositionCreditor.CF).build());
        entityManager.flush();
        entityManager.clear();
        assertDoesNotThrow(() -> service().validateReferences((short) 32767,
            impositionAccount("DVRULE", ",\"minor_creditor\":{}")));
        InvalidReferenceValidationException ex = assertThrows(InvalidReferenceValidationException.class,
            () -> service().validateReferences((short) 32767, impositionAccount("DVRULE", "")));
        assertTrue(ex.getMessage().contains("minor creditor or valid major creditor"), ex.getMessage());
    }

    @Test
    @Sql(scripts = "classpath:db/insertData/draft_account_not_cps_creditors.sql")
    void notCpsResultRejectsProsecutionServiceCreditorAndAcceptsOtherMajorCreditor() {
        resultOpalRepository.saveAndFlush(ResultOpalEntity.builder().resultId("DVNCPS").resultTitle("DVNCPS")
            .resultType(ResultType.RESULT).active(true).imposition(true)
            .impositionCreditor(ImpositionCreditor.NOT_CPS).build());

        assertDoesNotThrow(() -> service().validateReferences((short) 78,
            impositionAccount("DVNCPS", ",\"major_creditor_id\":770000009902")));
        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service().validateReferences((short) 78,
                impositionAccount("DVNCPS", ",\"major_creditor_id\":770000009901")));
        assertTrue(exception.getMessage().contains("$.offences[0].impositions[0].major_creditor_id"),
            exception.getMessage());
        assertTrue(exception.getMessage().contains("major creditor id 770000009901 is not valid"),
            exception.getMessage());
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
