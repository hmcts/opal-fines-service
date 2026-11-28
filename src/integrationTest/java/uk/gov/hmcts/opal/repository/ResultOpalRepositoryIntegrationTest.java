package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.repository.jpa.ResultOpalSpecs;

class ResultOpalRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final String RESULT_ID = "ORIT01";
    private static final String OTHER_ID = "ORIT02";

    @Autowired
    private ResultOpalRepository opalRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        opalRepository.saveAllAndFlush(List.of(
            ResultOpalEntity.builder().resultId(RESULT_ID).resultTitle("Active result")
                .resultType(ResultType.RESULT).active(true).build(),
            ResultOpalEntity.builder().resultId(OTHER_ID).resultTitle("Inactive result")
                .resultType(ResultType.RESULT).manualEnforcement(false).build()
        ));
    }

    @AfterEach
    void tearDown() {
        opalRepository.deleteAllById(List.of(RESULT_ID, OTHER_ID));
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void findById_preservesNullableFlags() {
        ResultOpalEntity result = opalRepository.findById(RESULT_ID).orElseThrow();
        assertTrue(result.isActive());
        assertNull(result.getManualEnforcement());
        assertNull(result.getRequiresEmploymentData());
        assertNull(result.getAllowPaymentTerms());
        assertNull(result.getAllowAdditionalAction());
        assertNull(result.getRequiresLja());
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void fullGraph_returnsResult() {
        assertEquals(RESULT_ID, opalRepository.findWithFullGraphByResultId(RESULT_ID).orElseThrow().getResultId());
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void specification_filtersResults() {
        var spec = ResultOpalSpecs.equalsAnyResultId(List.of(RESULT_ID, OTHER_ID))
            .and(ResultOpalSpecs.hasActive(true));
        var page = opalRepository.findBy(spec, query -> query.page(Pageable.unpaged()));
        assertEquals(List.of(RESULT_ID), page.getContent().stream().map(ResultOpalEntity::getResultId).toList());
    }

    @Test
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void findByResultIdIn_returnsMatchingResults() {
        assertEquals(List.of(RESULT_ID), opalRepository.findByResultIdIn(List.of(RESULT_ID, "NOPE99"))
            .stream().map(ResultOpalEntity::getResultId).toList());
    }

    @Test
    @Sql(scripts = "classpath:db/insertData/insert_into_result_entity_graph.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_result_entity_graph.sql", executionPhase = AFTER_TEST_METHOD)
    @JiraStory("PO-11027")
    @JiraEpic("PO-10769")
    void shouldLoadFullEntityGraphWhenRequestedExplicitly() {
        entityManager.clear();
        EntityGraph<?> fullGraph = entityManager.getEntityGraph(ResultOpalEntity.ENTITY_GRAPH_FULL);
        ResultOpalEntity result = entityManager.find(
            ResultOpalEntity.class,
            "RG9200",
            Map.of("jakarta.persistence.fetchgraph", fullGraph)
        );

        assertAll(
            () -> assertNotNull(result),
            () -> assertTrue(entityManagerFactory.getPersistenceUnitUtil().isLoaded(result, "enforcements")),
            () -> assertEquals(1, result.getEnforcements().size()),
            () -> assertEquals(920001L, result.getEnforcements().get(0).getEnforcementId())
        );
    }
}
