package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.ConfigurationItemLite;

@Repository
public interface ConfigurationItemRepository extends JpaRepository<ConfigurationItemLite, Long>,
    JpaSpecificationExecutor<ConfigurationItemLite> {
}
