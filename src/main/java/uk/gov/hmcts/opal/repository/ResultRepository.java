package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.ResultEntity;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<ResultEntity, String>,
    JpaSpecificationExecutor<ResultEntity> {
    List<ResultEntity> findByResultIdIn(List<String> resultIds);
}
