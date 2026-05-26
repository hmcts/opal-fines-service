package uk.gov.hmcts.opal.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@ActiveProfiles({"integration"})
@DisplayName("Creditor Account Repository Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_creditor_accounts_entity_graph.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_creditor_accounts_entity_graph.sql",
    executionPhase = AFTER_TEST_METHOD
)
class CreditorAccountRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final short BUSINESS_UNIT_ID = 951;
    private static final long MAJOR_CREDITOR_ID = 950001L;
    private static final long CREDITOR_ACCOUNT_ID = 950010L;

    @Autowired
    private CreditorAccountRepository creditorAccountRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @JiraStory("PO-2882")
    @JiraEpic("PO-304")
    void shouldKeepAssociationsLazyWhenNoEntityGraphIsUsed() {
        entityManager.clear();

        CreditorAccountEntity creditorAccount = entityManager.find(CreditorAccountEntity.class, CREDITOR_ACCOUNT_ID);
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertNotNull(creditorAccount);
        assertFalse(unitUtil.isLoaded(creditorAccount, "businessUnit"));
        assertFalse(unitUtil.isLoaded(creditorAccount, "majorCreditor"));
    }

    @Test
    @JiraStory("PO-2882")
    @JiraEpic("PO-304")
    void shouldKeepAssociationsLazyForLiteEntityGraphFetch() {
        entityManager.clear();

        CreditorAccountEntity creditorAccount = creditorAccountRepository.findById(CREDITOR_ACCOUNT_ID).orElseThrow();
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertFalse(unitUtil.isLoaded(creditorAccount, "businessUnit"));
        assertFalse(unitUtil.isLoaded(creditorAccount, "majorCreditor"));
        assertEquals(BUSINESS_UNIT_ID, creditorAccount.getBusinessUnitId());
        assertEquals(MAJOR_CREDITOR_ID, creditorAccount.getMajorCreditorId());
    }

    @Test
    @JiraStory("PO-2882")
    @JiraEpic("PO-304")
    void shouldLoadAssociationsForFullEntityGraphFetch() {
        entityManager.clear();

        CreditorAccountEntity creditorAccount = creditorAccountRepository
            .findFullByCreditorAccountId(CREDITOR_ACCOUNT_ID)
            .orElseThrow();
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertTrue(unitUtil.isLoaded(creditorAccount, "businessUnit"));
        assertTrue(unitUtil.isLoaded(creditorAccount, "majorCreditor"));
        assertNotNull(creditorAccount.getBusinessUnit());
        assertNotNull(creditorAccount.getMajorCreditor());
        assertEquals(BUSINESS_UNIT_ID, creditorAccount.getBusinessUnit().getBusinessUnitId());
        assertEquals(MAJOR_CREDITOR_ID, creditorAccount.getMajorCreditor().getMajorCreditorId());
    }
}
