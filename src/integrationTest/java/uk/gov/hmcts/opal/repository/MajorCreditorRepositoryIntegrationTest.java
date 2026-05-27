package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;
import uk.gov.hmcts.opal.repository.jpa.MajorCreditorSpecs;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@DisplayName("Major Creditor Repository Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_creditor_accounts_entity_graph.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_creditor_accounts_entity_graph.sql",
    executionPhase = AFTER_TEST_METHOD
)
class MajorCreditorRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final short BUSINESS_UNIT_ID = 951;
    private static final long MAJOR_CREDITOR_ID = 950001L;
    private static final long CREDITOR_ACCOUNT_ID = 950010L;

    @Autowired
    private MajorCreditorRepository majorCreditorRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    private PersistenceUnitUtil unitUtil;

    @BeforeEach
    void setUp() {
        entityManager.clear();
        unitUtil = entityManagerFactory.getPersistenceUnitUtil();
    }

    @Test
    @JiraStory("PO-2887")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6310")
    void shouldKeepAssociationsLazyWhenNoEntityGraphIsUsed() {

        MajorCreditorEntity majorCreditor = entityManager.find(MajorCreditorEntity.class, MAJOR_CREDITOR_ID);

        assertAll(
            () -> assertNotNull(majorCreditor),
            () -> assertFalse(unitUtil.isLoaded(majorCreditor, "creditorAccountEntity"))
        );
    }

    @Test
    @JiraStory("PO-2887")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6309")
    void shouldKeepAssociationsLazyForLiteEntityGraphFetch() {
        EntityGraph<?> liteGraph = entityManager.getEntityGraph(MajorCreditorEntity.ENTITY_GRAPH_LITE);
        MajorCreditorEntity majorCreditor = entityManager.find(
            MajorCreditorEntity.class,
            MAJOR_CREDITOR_ID,
            Map.of("jakarta.persistence.fetchgraph", liteGraph)
        );

        assertAll(
            () -> assertNotNull(majorCreditor),
            () -> assertFalse(unitUtil.isLoaded(majorCreditor, "creditorAccountEntity")),
            () -> assertEquals(BUSINESS_UNIT_ID, majorCreditor.getBusinessUnitId())
        );
    }

    @Test
    @JiraStory("PO-2887")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6311")
    void shouldLoadFullEntityGraphForDirectFetch() {
        MajorCreditorEntity majorCreditor = majorCreditorRepository.findById(MAJOR_CREDITOR_ID).orElseThrow();

        assertAll(
            () -> assertTrue(unitUtil.isLoaded(majorCreditor, "creditorAccountEntity")),
            () -> assertNotNull(majorCreditor.getCreditorAccountEntity()),
            () -> assertEquals(CREDITOR_ACCOUNT_ID, majorCreditor.getCreditorAccountEntity().getCreditorAccountId())
        );
    }

    @Test
    @JiraStory("PO-2887")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6308")
    void shouldLoadFullEntityGraphForSpecificationFetch() {
        Page<MajorCreditorEntity> page = majorCreditorRepository.findBy(
            MajorCreditorSpecs.equalsMajorCreditorId(MAJOR_CREDITOR_ID),
            ffq -> ffq.page(Pageable.unpaged())
        );
        MajorCreditorEntity majorCreditor = page.getContent().getFirst();

        assertAll(
            () -> assertTrue(unitUtil.isLoaded(majorCreditor, "creditorAccountEntity")),
            () -> assertEquals(CREDITOR_ACCOUNT_ID, majorCreditor.getCreditorAccountEntity().getCreditorAccountId())
        );
    }
}
