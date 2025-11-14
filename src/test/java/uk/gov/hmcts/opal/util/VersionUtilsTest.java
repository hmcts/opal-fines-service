package uk.gov.hmcts.opal.util;

import org.junit.jupiter.api.DisplayName;
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

        private final Long version;

        public UtilVersioned(Long v) {
            this.version = v;
        }

        @Override
        public Long getVersion() {
            return version;
        }
    }

    @Test
    @DisplayName("null/blank -> 1")
    void nullOrBlank() {
        assertEquals(1, VersionUtils.parseIfMatchVersion(null));
        assertEquals(1, VersionUtils.parseIfMatchVersion(""));
        assertEquals(1, VersionUtils.parseIfMatchVersion("   "));
    }

    @Test
    @DisplayName("Quoted / weak validators parse their digits")
    void commonFormats() {
        assertEquals(3,  VersionUtils.parseIfMatchVersion("\"3\""));
        assertEquals(7,  VersionUtils.parseIfMatchVersion("W/\"7\""));
        assertEquals(12, VersionUtils.parseIfMatchVersion("  \"12\"  "));
        assertEquals(1,  VersionUtils.parseIfMatchVersion("W/\"001\"")); // leading zeros ok -> 1
    }

    @Test
    @DisplayName("Garbage or no digits -> 1")
    void garbage() {
        assertEquals(1, VersionUtils.parseIfMatchVersion("garbage"));
        assertEquals(1, VersionUtils.parseIfMatchVersion("W/\"abc\""));
    }

    @Test
    @DisplayName("Negative or zero -> 1")
    void nonPositive() {
        assertEquals(1, VersionUtils.parseIfMatchVersion("\"-1\""));
        assertEquals(1, VersionUtils.parseIfMatchVersion("\"0\""));
    }

    @Test
    @DisplayName("Bounds: MAX_INT ok, overflow -> 1")
    void bounds() {
        assertEquals(Integer.MAX_VALUE,
            VersionUtils.parseIfMatchVersion("W/\"2147483647\""));
        assertEquals(1,
            VersionUtils.parseIfMatchVersion("W/\"2147483648\"")); // > Integer.MAX_VALUE
        assertEquals(1,
            VersionUtils.parseIfMatchVersion("999999999999999999999999"));
    }
}
