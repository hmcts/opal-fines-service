package uk.gov.hmcts.opal.util;

import java.math.BigInteger;
import java.util.Optional;

public interface VersionedLong extends Versioned {

    Long getVersionNumber();

    default BigInteger getVersion() {
        return Optional.ofNullable(getVersionNumber()).map(BigInteger::valueOf).orElse(null);
    }
}
