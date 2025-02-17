package uk.gov.hmcts.opal.util;

import java.time.LocalDateTime;

public interface Lockable extends Versioned {
    String getLockIdData();

    LocalDateTime getLockTimeout();
}
