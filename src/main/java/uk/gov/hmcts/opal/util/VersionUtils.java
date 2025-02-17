package uk.gov.hmcts.opal.util;

import jakarta.persistence.RollbackException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j(topic = "opal.VersionUtils")
public class VersionUtils {

    private VersionUtils() {
    }

    /**
     * Ensure that DB updates are only done on Entities with synchronized version numbers.
     * Although JPA does provide some level of Transactional consistency out of the box, when updates are
     * done over a longer timeframe, e.g. within a web app, then we need to perform extra validation against
     * the version numbers of the Entity in the DB and the DTO providing dto parameters.
     * @param existingFromDB the latest entity from the DB, obtained within a transactional context.
     * @param dto the object containing the new data for the entity, typically a dto.
     * @param id the id of the entity being updated, only used in the exceptional 'mismatch' case.
     * @param method the name of the method from where this is being called, only used in the error log.
     */
    public static void verifyVersions(Versioned existingFromDB, Versioned dto, Object id, String method) {
        if (versionsAreNotEqual(existingFromDB, dto)) {
            log.warn(":{}: Versions Do Not Match! Entity: {} {}, DB version: {}, Update version: {}", method,
                     existingFromDB.getClass().getSimpleName(), id, existingFromDB.getVersion(), dto.getVersion());
            throw new StaleObjectStateException(existingFromDB.getClass().getSimpleName(), id);
        }
    }

    public static void verifyUpdated(Versioned updatedFromDB, Versioned dto, Object id, String method) {
        if (versionsAreEqual(updatedFromDB, dto)) {
            log.warn(":{}: NO differences detected in Entity '{}',  ID: '{}'. DB has NOT been updated from version :{}",
                     method, updatedFromDB.getClass().getSimpleName(), id, updatedFromDB.getVersion());
            throw new ResourceConflictException(updatedFromDB.getClass().getSimpleName(), id,
                                                "No differences detected between DB state and requested update.");
        }
    }

    public static boolean versionsAreEqual(Versioned fromDB, Versioned other) {
        return fromDB.getVersion().equals(other.getVersion());
    }

    public static boolean versionsAreNotEqual(Versioned fromDB, Versioned other) {
        return !versionsAreEqual(fromDB, other);
    }

    public static boolean isUnlocked(Lockable entity, String userId,  Object id) {
        if (isNullOrEmptyOrMatchesUser(entity.getLockIdData(), userId) || isNullOrInThePast(entity.getLockTimeout())) {
            return true;
        } else {
            LocalDateTime now = LocalDateTime.now();
            long delta = now.until(entity.getLockTimeout(), ChronoUnit.MILLIS);
            throw new RollbackException("Draft Account '" + id + "' version "
                                            + entity.getVersion() + ", is currently locked by "
                                            + entity.getLockIdData() + ", until "
                                            + entity.getLockTimeout() + ". Currently " + now + "(" + delta + "ms).");
        }
    }

    public static boolean isNullOrEmptyOrMatchesUser(String candidate, String userId) {
        return candidate == null || candidate.isEmpty() || candidate.equals(userId);
    }

    public static boolean isNullOrInThePast(LocalDateTime candidate) {
        return candidate == null || LocalDateTime.now().isAfter(candidate);
    }
}
