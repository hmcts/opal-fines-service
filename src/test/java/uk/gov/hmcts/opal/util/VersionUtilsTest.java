package uk.gov.hmcts.opal.util;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.exception.ResourceConflictException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VersionUtilsTest {

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

        StaleObjectStateException rte = assertThrows(StaleObjectStateException.class, () ->
            VersionUtils.verifyVersions(entity, dto, "test id", "testVerifyVersions_fail")
        );

        assertEquals("UtilVersioned", rte.getEntityName());
        assertEquals("test id", rte.getIdentifier());
        assertEquals("Row was updated or deleted by another transaction "
                         + "(or unsaved-value mapping was incorrect) : [UtilVersioned#test id]", rte.getMessage());
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

        private final Long version;

        public UtilVersioned(Long v) {
            this.version = v;
        }

        @Override
        public Long getVersion() {
            return version;
        }
    }
}
