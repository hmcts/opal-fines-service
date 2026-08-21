package uk.gov.hmcts.opal.repository;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.TillEntity;

@Repository
public interface TillRepository extends JpaRepository<TillEntity, Long> {

    long countByInterfaceFile_InterfaceFileIdIn(Collection<Long> interfaceFileIds);
}
