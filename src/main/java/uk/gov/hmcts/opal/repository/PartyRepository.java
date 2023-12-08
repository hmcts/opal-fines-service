package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartySummary;

import java.util.List;

@Repository
public interface PartyRepository extends JpaRepository<PartyEntity, Long> {

    List<PartySummary> findBySurnameContaining(String surname);

}
