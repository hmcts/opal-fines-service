package uk.gov.hmcts.opal.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.exception.ResourceConflictException;

import java.util.Optional;

@Slf4j(topic = "opal.VersionUtils")
public class VersionUtils {

    // first run of digits in the header (e.g., matches 7 in W/"7")
    private static final Pattern DIGITS = Pattern.compile("(\\d+)");

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
            String msg = MessageFormatter.arrayFormat(
                ":{}: Versions do not match for: {} '{}'; DB version: {}, supplied update version: {}",
                new Object[] { method, existingFromDB.getClass().getSimpleName(),
                    id, existingFromDB.getVersion(), dto.getVersion()
                })
                .getMessage();
            log.warn(msg);
            throw new ObjectOptimisticLockingFailureException(DraftAccountEntity.class, id, msg, null);
        }
    }

    public static void verifyUpdated(Versioned updatedFromDB, Versioned dto, Object id, String method) {
        if (versionsAreEqual(updatedFromDB, dto)) {
            log.warn(":{}: NO differences detected in Entity '{}',  ID: '{}'. DB has NOT been updated from version :{}",
                     method, updatedFromDB.getClass().getSimpleName(), id, updatedFromDB.getVersion());
            throw new ResourceConflictException(updatedFromDB.getClass().getSimpleName(), id,
                                                "No differences detected between DB state and requested update.");
        } else {
            log.debug(":{}: Updated Entity '{}' - '{}", method, updatedFromDB.getClass().getSimpleName(), id);
        }
    }

    public static boolean versionsAreEqual(Versioned fromDB, Versioned other) {
        return fromDB.getVersion().equals(other.getVersion());
    }

    public static boolean versionsAreNotEqual(Versioned fromDB, Versioned other) {
        return !versionsAreEqual(fromDB, other);
    }

    public static String createETag(Versioned versioned) {
        return "\"" + versioned.getVersion() + "\"";
    }

    public static void verifyIfMatch(Versioned existingFromDB, String ifMatch, Object id, String method) {
        verifyVersions(existingFromDB, () -> Optional.ofNullable(ifMatch)
            .map(s -> s.replace("\"", ""))
            .map(Long::parseLong)
            .orElseThrow(() -> new ResourceConflictException(existingFromDB.getClass().getSimpleName(), id,
                  "Could not parse 'ifMatch': " + ifMatch + " in method: " + method)), id, method);
    }

    /**
     * Parse If-Match header into a version number.
     *
     * <p>
     * For legacy system, version is inferred from the IfMatch value.
     *
     * Examples:
     * | If-Match value                 | Result                              |
     * | ------------------------------ | ----------------------------------- |
     * | `null`, `""`, `"   "`          | **1**                               |
     * | `"\"3\""`, `"3"`, `"W/\"12\""` | **valid → parsed int**              |
     * | `"W/\"001\""`                  | **`1` is valid**, returns **1**     |
     * | `"garbage"`                    | throws PropertyValueException       |
     * | `"W/\"abc\""`                  | throws PropertyValueException       |
     * | `"0"`, `"W/\"0\""`             | throws PropertyValueException       |
     * | `"-1"`                         | throws PropertyValueException       |
     * | `"2147483648"`                 | throws (overflow)                   |
     * | `"999999999999999999999999"`   | throws PropertyValueException       |
     * </p>
     */
    public static int parseIfMatchVersion(String ifMatch) {
        // Per requirement: null/blank -> throw
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new IllegalArgumentException("Invalid If-Match header value: " + ifMatch);
        }

        // If there's an explicit negative sign anywhere, treat as invalid
        if (ifMatch.indexOf('-') >= 0) {
            throw new IllegalArgumentException("Invalid If-Match header value: " + ifMatch);
        }

        Matcher m = DIGITS.matcher(ifMatch);
        if (!m.find()) {
            // no digit found -> invalid
            throw new IllegalArgumentException("Invalid If-Match header value: " + ifMatch);
        }

        String digits = m.group(1);
        try {
            long parsed = Long.parseLong(digits);

            if (parsed <= 0) {
                throw new IllegalArgumentException("Invalid If-Match header value: " + ifMatch);
            }
            if (parsed > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid If-Match header value: " + ifMatch);
            }

            return (int) parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid If-Match header value: " + ifMatch);
        }
    }
}
