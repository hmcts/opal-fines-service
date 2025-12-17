package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.result.ResultEntity.Lite;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Lite, String>,
    JpaSpecificationExecutor<Lite> {
    List<Lite> findByResultIdIn(List<String> resultIds);
}
