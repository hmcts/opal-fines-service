package uk.gov.hmcts.opal.util;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VersionUtilsTest {

    @Test
    void testVerifyVersions_success() {
        Versioned entity = new UtilVersioned(1L);
        Versioned dto = new UtilVersioned(1L);
        VersionUtils.verifyVersions(entity, dto, "test id", "testVerifyVersions_success");
    }

    @Test
    void testVerifyVersions_fail() {
        Versioned entity = new UtilVersioned(1L);
        Versioned dto = new UtilVersioned(2L);

        ObjectOptimisticLockingFailureException rte = assertThrows(ObjectOptimisticLockingFailureException.class, () ->
            VersionUtils.verifyVersions(entity, dto, "test id", "testVerifyVersions_fail")
        );

        assertEquals("uk.gov.hmcts.opal.entity.draft.DraftAccountEntity", rte.getPersistentClassName());
        assertEquals("test id", rte.getIdentifier());
        assertEquals(":testVerifyVersions_fail: Versions do not match for: UtilVersioned 'test id'; "
                         + "DB version: 1, supplied update version: 2", rte.getMessage());
    }

    @Test
    void testVerifyUpdated_success() {
        Versioned entity = new UtilVersioned(2L);
        Versioned dto = new UtilVersioned(1L);
        VersionUtils.verifyUpdated(entity, dto, "test id", "testVerifyVersions_success");
    }

    @Test
    void testVerifyUpdated_fail() {
        Versioned entity = new UtilVersioned(2L);
        Versioned dto = new UtilVersioned(2L);

        ResourceConflictException rte = assertThrows(ResourceConflictException.class, () ->
            VersionUtils.verifyUpdated(entity, dto, "test id", "testVerifyVersions_fail")
        );

        assertEquals("UtilVersioned", rte.getResourceType());
        assertEquals("test id", rte.getResourceId());
        assertEquals("No differences detected between DB state and requested update.",
                     rte.getConflictReason());
    }

    private class UtilVersioned implements Versioned {

        private final BigInteger version;

        public UtilVersioned(Long v) {
            this.version = BigInteger.valueOf(v);
        }

        @Override
        public BigInteger getVersion() {
            return version;
        }
    }
}
