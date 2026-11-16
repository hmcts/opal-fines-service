package uk.gov.hmcts.opal.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.DraftAccountMapping;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("integration")
@DisplayName("Draft Account Mapping Integration Test")
@Sql(scripts = "classpath:db/insertData/insert_draft_account_mapping.sql",
    executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_draft_account_mapping.sql",
    executionPhase = ExecutionPhase.AFTER_TEST_CLASS)
class DraftAccountMappingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LocalJusticeAreaRepository repository;

    @Test
    @JiraStory("PO-10322")
    @JiraEpic("PO-7459")
    void findsBusinessUnitAndCourt() {
        assertThat(repository.findDraftAccountMappingByLjaCode((short) 30001))
            .contains(new DraftAccountMapping((short) 31001, 900000000001L));
    }

    @Test
    @JiraStory("PO-10322")
    @JiraEpic("PO-7459")
    void returnsEmptyForMissingLja() {
        assertThat(repository.findDraftAccountMappingByLjaCode((short) 30004)).isEmpty();
    }

    @Test
    @JiraStory("PO-10322")
    @JiraEpic("PO-7459")
    void returnsEmptyForMissingCourtMapping() {
        assertThat(repository.findDraftAccountMappingByLjaCode((short) 30002)).isEmpty();
    }

    @Test
    @JiraStory("PO-10322")
    @JiraEpic("PO-7459")
    void rejectsAmbiguousMapping() {
        assertThatThrownBy(() -> repository.findDraftAccountMappingByLjaCode((short) 30003))
            .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }
}
