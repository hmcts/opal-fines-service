package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.result.ResultEntityLite;

import java.util.List;

@Repository
public interface ResultLiteRepository extends JpaRepository<ResultEntityLite, String>,
    JpaSpecificationExecutor<ResultEntityLite> {
    List<ResultEntityLite> findByResultIdIn(List<String> resultIds);
}
