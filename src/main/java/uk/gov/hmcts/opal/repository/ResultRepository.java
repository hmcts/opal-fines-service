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
}
