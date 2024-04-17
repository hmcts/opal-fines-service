package uk.gov.hmcts.opal.scheduler.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogRetentionService {

    private final JdbcTemplate jdbcTemplate;

    public void deleteExpiredLogAudit() {
        jdbcTemplate.execute("CALL delete_expired_log_audit()");
    }
}
