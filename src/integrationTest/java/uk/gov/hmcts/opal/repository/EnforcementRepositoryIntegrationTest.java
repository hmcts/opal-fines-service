package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@DisplayName("Enforcement Repository Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_enforcements_entity_graph.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_enforcements_entity_graph.sql",
    executionPhase = AFTER_TEST_METHOD
)
class EnforcementRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final long ENFORCEMENT_ID = 910001L;
    private static final long DEFENDANT_ACCOUNT_ID = 910002L;
    private static final long ENFORCER_ID = 910003L;
    private static final long HEARING_COURT_ID = 910004L;

    @Autowired
    private EnforcementRepository enforcementRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @JiraStory("PO-2883")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6301")
    void shouldKeepAssociationsLazyWhenNoEntityGraphIsUsed() {
        entityManager.clear();

        EnforcementEntity enforcement = entityManager.find(EnforcementEntity.class, ENFORCEMENT_ID);
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertNotNull(enforcement);
        assertFalse(unitUtil.isLoaded(enforcement, "result"));
        assertFalse(unitUtil.isLoaded(enforcement, "defendantAccount"));
        assertFalse(unitUtil.isLoaded(enforcement, "enforcer"));
        assertFalse(unitUtil.isLoaded(enforcement, "hearingCourt"));
    }

    @Test
    @JiraStory("PO-2883")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6303")
    void shouldLoadFullEntityGraphForDirectFetch() {
        entityManager.clear();

        EnforcementEntity enforcement = enforcementRepository.findById(ENFORCEMENT_ID).orElseThrow();
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertTrue(unitUtil.isLoaded(enforcement, "result"));
        assertTrue(unitUtil.isLoaded(enforcement, "defendantAccount"));
        assertTrue(unitUtil.isLoaded(enforcement, "enforcer"));
        assertTrue(unitUtil.isLoaded(enforcement, "hearingCourt"));
        assertEquals("ER9100", enforcement.getResult().getResultId());
        assertEquals(DEFENDANT_ACCOUNT_ID, enforcement.getDefendantAccount().getDefendantAccountId());
        assertEquals(ENFORCER_ID, enforcement.getEnforcer().getEnforcerId());
        assertEquals(HEARING_COURT_ID, enforcement.getHearingCourt().getCourtId());
    }

    @Test
    @JiraStory("PO-2883")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6302")
    void shouldLoadLiteEntityGraphForMostRecentFetch() {
        entityManager.clear();

        EnforcementEntity enforcement = enforcementRepository
            .findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(DEFENDANT_ACCOUNT_ID, "ER9100")
            .orElseThrow();
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertTrue(unitUtil.isLoaded(enforcement, "result"));
        assertFalse(unitUtil.isLoaded(enforcement, "defendantAccount"));
        assertFalse(unitUtil.isLoaded(enforcement, "enforcer"));
        assertFalse(unitUtil.isLoaded(enforcement, "hearingCourt"));
        assertEquals(ENFORCEMENT_ID, enforcement.getEnforcementId());
        assertEquals("ER9100", enforcement.getResult().getResultId());
    }
}
