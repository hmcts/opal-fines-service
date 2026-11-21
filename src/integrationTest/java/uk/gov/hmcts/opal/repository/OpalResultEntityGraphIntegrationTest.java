package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;

@Sql(scripts = "classpath:db/insertData/insert_into_result_entity_graph.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_result_entity_graph.sql", executionPhase = AFTER_TEST_METHOD)
class OpalResultEntityGraphIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ResultOpalRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void findById_keepsEnforcementsLazy() {
        entityManager.clear();
        ResultOpalEntity result = repository.findById("RG9200").orElseThrow();
        assertFalse(entityManagerFactory.getPersistenceUnitUtil().isLoaded(result, "enforcements"));
    }

    @Test
    void fullGraph_loadsEnforcements() {
        entityManager.clear();
        ResultOpalEntity result = repository.findWithFullGraphByResultId("RG9200").orElseThrow();
        assertTrue(entityManagerFactory.getPersistenceUnitUtil().isLoaded(result, "enforcements"));
        assertEquals(1, result.getEnforcements().size());
        assertEquals(920001L, result.getEnforcements().get(0).getEnforcementId());
    }
}
