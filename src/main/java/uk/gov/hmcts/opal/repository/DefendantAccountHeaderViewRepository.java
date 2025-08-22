package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DefendantAccountHeaderViewEntity;

@Repository
public interface DefendantAccountHeaderViewRepository extends JpaRepository<DefendantAccountHeaderViewEntity, Long> {
}
