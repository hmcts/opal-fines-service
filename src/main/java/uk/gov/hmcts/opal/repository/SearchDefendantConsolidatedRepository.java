package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.search.SearchConsolidatedEntity;

@Repository
public interface SearchDefendantConsolidatedRepository extends JpaRepository<SearchConsolidatedEntity, Long>,
        JpaSpecificationExecutor<SearchConsolidatedEntity> {
}
