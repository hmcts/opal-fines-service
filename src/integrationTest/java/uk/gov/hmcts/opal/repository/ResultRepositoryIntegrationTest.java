package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.result.ResultEntity;

import java.util.Map;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("Result Repository Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_result_entity_graph.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_result_entity_graph.sql",
    executionPhase = AFTER_TEST_METHOD
)
class ResultRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final String RESULT_ID = "RG9200";
    private static final long ENFORCEMENT_ID = 920001L;

    @Autowired
    private ResultRepository resultRepository;

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
    @JiraStory("PO-2888")
    @JiraEpic("PO-304")
    void shouldKeepEnforcementsLazyWhenNoEntityGraphIsUsed() {
        ResultEntity result = entityManager.find(ResultEntity.class, RESULT_ID);

        assertAll(
            () -> assertNotNull(result),
            () -> assertFalse(unitUtil.isLoaded(result, "enforcements"))
        );
    }

    @Test
    @JiraStory("PO-2888")
    @JiraEpic("PO-304")
    void shouldKeepEnforcementsLazyForRepositoryFindById() {
        ResultEntity result = resultRepository.findById(RESULT_ID).orElseThrow();

        assertAll(
            () -> assertNotNull(result),
            () -> assertFalse(unitUtil.isLoaded(result, "enforcements"))
        );
    }

    @Test
    @JiraStory("PO-2888")
    @JiraEpic("PO-304")
    void shouldLoadFullEntityGraphWhenRequestedExplicitly() {
        EntityGraph<?> fullGraph = entityManager.getEntityGraph(ResultEntity.ENTITY_GRAPH_FULL);
        ResultEntity result = entityManager.find(
            ResultEntity.class,
            RESULT_ID,
            Map.of("jakarta.persistence.fetchgraph", fullGraph)
        );

        assertAll(
            () -> assertNotNull(result),
            () -> assertTrue(unitUtil.isLoaded(result, "enforcements")),
            () -> assertEquals(1, result.getEnforcements().size()),
            () -> assertEquals(ENFORCEMENT_ID, result.getEnforcements().get(0).getEnforcementId())
        );
    }

    @Test
    @JiraStory("PO-2888")
    @JiraEpic("PO-304")
    void shouldKeepEnforcementsLazyForSpecificationFetch() {
        Specification<ResultEntity> spec =
            (root, query, builder) -> builder.equal(root.get("resultId"), RESULT_ID);

        Page<ResultEntity> page = resultRepository.findBy(spec, query -> query.page(Pageable.unpaged()));
        ResultEntity result = page.getContent().get(0);

        assertAll(
            () -> assertEquals(1, page.getTotalElements()),
            () -> assertNotNull(result),
            () -> assertFalse(unitUtil.isLoaded(result, "enforcements"))
        );
    }
}
