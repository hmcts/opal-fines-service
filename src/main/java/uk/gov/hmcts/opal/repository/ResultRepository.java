package uk.gov.hmcts.opal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.result.ResultEntity;

@Repository
public interface ResultRepository extends JpaRepository<ResultEntity.Lite, Long> {
    /**
     * (Standard CRUD method - visible for clarity.)
     */
    Optional<ResultEntity.Lite> findById(Long resultId);

    /**
     * Lookup multiple results by id (useful if you want to fetch projections or batch-check flags).
     */
    // List<ResultEntity> findByResultIdIn(List<Long> resultIds);

    /**
     * Efficiently fetch only the extendTtpPreserveLastEnf flag for a given result id.
     * This avoids loading the whole ResultEntity when you only need the boolean.
     *
     * <p>
     * Returns an Optional of Boolean — empty if no row found.
     * </p>
     */
    // @Query("select r.extendTtpPreserveLastEnf from ResultEntity.Lite r where r.resultId = :resultId")
    // Optional<Boolean> findExtendTtpPreserveLastEnfByResultId(@Param("resultId") Long resultId);

}
