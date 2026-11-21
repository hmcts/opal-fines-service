package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;


@DisplayName("Legacy Result Repository Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_result_entity_graph.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_result_entity_graph.sql",
    executionPhase = AFTER_TEST_METHOD
)
class ResultLegacyRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final String RESULT_ID = "RG9200";

    @Autowired
    private ResultLegacyRepository resultRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Test
    void findById_returnsLegacyResult() {
        ResultLegacyEntity result = resultRepository.findById(RESULT_ID).orElseThrow();
        assertEquals(RESULT_ID, result.getResultId());
    }
}
