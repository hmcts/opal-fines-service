package uk.gov.hmcts.opal.service.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;

@Sql(scripts = {"classpath:db/insertData/insert_into_result_entity_graph.sql",
    "classpath:db/insertData/update_legacy_result_override_title.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_result_entity_graph.sql", executionPhase = AFTER_TEST_METHOD)
class ResultRepositoryServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ResultRepositoryService service;

    @Test
    void getResultById_readsOpalResult() {
        assertEquals("Result Graph Result", service.getResultById("RG9200").orElseThrow().getResultTitle());
    }
}
