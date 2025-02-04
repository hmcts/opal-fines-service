package uk.gov.hmcts.opal.scheduler.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

class LogRetentionServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private LogRetentionService logRetentionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logRetentionService = new LogRetentionService(jdbcTemplate);
    }

    @Test
    void testDeleteExpiredLogAudit_SuccessfulExecution() {
        logRetentionService.deleteExpiredLogAudit();

        verify(jdbcTemplate).execute("CALL delete_expired_log_audit()");

    }

    @Test
    void testDeleteExpiredLogAudit_DatabaseError() {
        doThrow(new RuntimeException("Database error")).when(jdbcTemplate).execute("CALL delete_expired_log_audit()");

        assertThrows(RuntimeException.class, () -> logRetentionService.deleteExpiredLogAudit());
        verify(jdbcTemplate).execute("CALL delete_expired_log_audit()");
    }
}
