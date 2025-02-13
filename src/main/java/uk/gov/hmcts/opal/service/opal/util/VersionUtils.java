package uk.gov.hmcts.opal.service.opal.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.dto.Versioned;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;

@Slf4j(topic = "VersionUtils")
public class VersionUtils {

    private VersionUtils() {
    }

    /**
     * Ensure that DB updates are only done on Entities with synchronized version numbers.
     * Although JPA does provide some level of Transactional consistency out of the box, when updates are
     * done over a longer timeframe, e.g. within a web app, then we need to perform extra validation against
     * the version numbers of the Entity in the DB and the DTO providing updated parameters.
     * @param existingFromDB the latest entity from the DB, obtained within a transactional context
     * @param updated the object containing the updated information, typically a dto
     * @param id the id of the entity being updated, only used in the exceptional 'mismatch' case.
     */
    public static void verifyVersions(Versioned existingFromDB, Versioned updated, Object id) {
        if (!existingFromDB.getVersion().equals(updated.getVersion())) {
            log.warn(":verifyVersions: Versions Do Not Match! Entity: {} {}, DB version: {}, Update version: {}",
                     existingFromDB.getClass().getSimpleName(), id, existingFromDB.getVersion(),  updated.getVersion());
            throw new ObjectOptimisticLockingFailureException(DraftAccountEntity.class, id);
        }
    }

}
