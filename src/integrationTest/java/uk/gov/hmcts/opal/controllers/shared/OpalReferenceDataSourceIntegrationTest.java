package uk.gov.hmcts.opal.controllers.shared;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles({"integration", "opal"})
@DisplayName("Opal mode reference data source integration tests")
@Sql(scripts = "classpath:db/insertData/insert_opal_only_reference_data.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_opal_only_reference_data.sql", executionPhase = AFTER_TEST_CLASS)
@Isolated
class OpalReferenceDataSourceIntegrationTest extends ReferenceDataSourceIntegrationTest {
}
