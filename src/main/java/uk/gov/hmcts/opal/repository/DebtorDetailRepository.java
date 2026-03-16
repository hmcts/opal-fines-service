package uk.gov.hmcts.opal.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;

@Repository
public interface DebtorDetailRepository extends JpaRepository<DebtorDetailEntity, Long>,
    JpaSpecificationExecutor<DebtorDetailEntity> {

    Optional<DebtorDetailEntity> findByPartyId(Long partyId);

    List<DebtorDetailEntity> findByPartyIdIn(Set<Long> partyIds);
}
