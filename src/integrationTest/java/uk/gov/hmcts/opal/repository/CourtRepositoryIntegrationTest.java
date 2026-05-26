package uk.gov.hmcts.opal.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.repository.jpa.CourtSpecs;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@ActiveProfiles({"integration"})
@DisplayName("Court Repository Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_courts_entity_graph.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_courts_entity_graph.sql",
    executionPhase = AFTER_TEST_METHOD
)
class CourtRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final long COURT_ID = 951002L;
    private static final long PARENT_COURT_ID = 951001L;
    private static final short BUSINESS_UNIT_ID = 951;
    private static final short LOCAL_JUSTICE_AREA_ID = 951;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private CourtLiteRepository courtLiteRepository;

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
    @JiraStory("PO-2885")
    @JiraEpic("PO-304")
    void shouldKeepAssociationsLazyWhenNoEntityGraphIsUsed() {
        CourtEntity court = entityManager.find(CourtEntity.class, COURT_ID);

        assertAll(
            () -> assertNotNull(court),
            () -> assertFalse(unitUtil.isLoaded(court, "businessUnit")),
            () -> assertFalse(unitUtil.isLoaded(court, "localJusticeArea")),
            () -> assertFalse(unitUtil.isLoaded(court, "parentCourt"))
        );
    }

    @Test
    @JiraStory("PO-2885")
    @JiraEpic("PO-304")
    void shouldKeepAssociationsLazyWhenLiteEntityGraphIsUsedForDirectFetch() {
        CourtEntity court = courtLiteRepository.findById(COURT_ID).orElseThrow();

        assertAssociationsRemainLazy(court);
    }

    @Test
    @JiraStory("PO-2885")
    @JiraEpic("PO-304")
    void shouldKeepAssociationsLazyWhenLiteEntityGraphIsUsedForSpecificationFetch() {
        Page<CourtEntity> page = courtLiteRepository.findBy(
            CourtSpecs.equalsCourtId(COURT_ID),
            ffq -> ffq.page(Pageable.unpaged())
        );
        CourtEntity court = page.getContent().getFirst();

        assertEquals(1, page.getContent().size());
        assertAssociationsRemainLazy(court);
    }

    @Test
    @JiraStory("PO-2885")
    @JiraEpic("PO-304")
    void shouldLoadFullEntityGraphForDirectFetch() {
        CourtEntity court = courtRepository.findById(COURT_ID).orElseThrow();

        assertAll(
            () -> assertTrue(unitUtil.isLoaded(court, "businessUnit")),
            () -> assertTrue(unitUtil.isLoaded(court, "localJusticeArea")),
            () -> assertTrue(unitUtil.isLoaded(court, "parentCourt")),
            () -> assertEquals(BUSINESS_UNIT_ID, court.getBusinessUnit().getBusinessUnitId()),
            () -> assertEquals(LOCAL_JUSTICE_AREA_ID, court.getLocalJusticeArea().getLocalJusticeAreaId()),
            () -> assertEquals(PARENT_COURT_ID, court.getParentCourt().getCourtId())
        );
    }

    @Test
    @JiraStory("PO-2885")
    @JiraEpic("PO-304")
    void shouldLoadFullEntityGraphForSpecificationFetch() {
        Page<CourtEntity> page = courtRepository.findBy(
            CourtSpecs.equalsCourtId(COURT_ID),
            ffq -> ffq.page(Pageable.unpaged())
        );
        CourtEntity court = page.getContent().getFirst();

        assertAll(
            () -> assertEquals(1, page.getContent().size()),
            () -> assertTrue(unitUtil.isLoaded(court, "businessUnit")),
            () -> assertTrue(unitUtil.isLoaded(court, "localJusticeArea")),
            () -> assertTrue(unitUtil.isLoaded(court, "parentCourt"))
        );
    }

    private void assertAssociationsRemainLazy(CourtEntity court) {
        assertAll(
            () -> assertFalse(unitUtil.isLoaded(court, "businessUnit")),
            () -> assertFalse(unitUtil.isLoaded(court, "localJusticeArea")),
            () -> assertFalse(unitUtil.isLoaded(court, "parentCourt"))
        );
    }
}
