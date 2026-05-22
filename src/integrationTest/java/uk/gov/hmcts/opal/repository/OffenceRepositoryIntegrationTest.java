package uk.gov.hmcts.opal.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity_;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@ActiveProfiles({"integration"})
@DisplayName("Offence Repository Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_offences_entity_graph.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_offences_entity_graph.sql",
    executionPhase = AFTER_TEST_METHOD
)
class OffenceRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final long OFFENCE_ID = 990001L;
    private static final short BUSINESS_UNIT_ID = 951;

    @Autowired
    private OffenceRepository offenceRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @JiraStory("PO-2886")
    @JiraEpic("PO-304")
    void shouldKeepBusinessUnitLazyWhenNoEntityGraphIsUsed() {
        entityManager.clear();

        OffenceEntity offence = entityManager.find(OffenceEntity.class, OFFENCE_ID);
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertNotNull(offence);
        assertEquals(BUSINESS_UNIT_ID, offence.getBusinessUnitId());
        assertFalse(unitUtil.isLoaded(offence, "businessUnit"));
    }

    @Test
    @JiraStory("PO-2886")
    @JiraEpic("PO-304")
    void shouldLoadFullEntityGraphForDirectFetch() {
        entityManager.clear();

        OffenceEntity offence = offenceRepository.findById(OFFENCE_ID).orElseThrow();
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertTrue(unitUtil.isLoaded(offence, "businessUnit"));
        assertNotNull(offence.getBusinessUnit());
        assertEquals(BUSINESS_UNIT_ID, offence.getBusinessUnit().getBusinessUnitId());
        assertEquals("Offence Graph Business Unit", offence.getBusinessUnit().getBusinessUnitName());
    }

    @Test
    @JiraStory("PO-2886")
    @JiraEpic("PO-304")
    void shouldKeepBusinessUnitLazyForLiteSpecificationFetch() {
        entityManager.clear();

        Page<OffenceEntity> page = offenceRepository.findBy(
            (root, query, builder) -> builder.equal(root.get(OffenceEntity_.offenceId), OFFENCE_ID),
            ffq -> ffq.page(Pageable.unpaged())
        );
        OffenceEntity offence = page.getContent().getFirst();
        PersistenceUnitUtil unitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertEquals(1, page.getContent().size());
        assertEquals(BUSINESS_UNIT_ID, offence.getBusinessUnitId());
        assertFalse(unitUtil.isLoaded(offence, "businessUnit"));
    }
}
