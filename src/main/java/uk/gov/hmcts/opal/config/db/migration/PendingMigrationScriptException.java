package uk.gov.hmcts.opal.config.db.migration;

import lombok.Generated;

@Generated
public class PendingMigrationScriptException extends RuntimeException {

    public PendingMigrationScriptException(String script) {
        super("Found migration not yet applied: " + script);
    }
}
