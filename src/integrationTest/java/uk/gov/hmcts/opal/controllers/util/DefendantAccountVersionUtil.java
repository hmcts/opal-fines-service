package uk.gov.hmcts.opal.controllers.util;

import org.springframework.jdbc.core.JdbcTemplate;

public final class DefendantAccountVersionUtil {

    private DefendantAccountVersionUtil() {
    }

    public static Integer getVersion(JdbcTemplate jdbcTemplate, long defendantAccountId) {
        return jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            defendantAccountId
        );
    }
}
