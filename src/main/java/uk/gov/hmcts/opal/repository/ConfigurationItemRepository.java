package uk.gov.hmcts.opal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;

@Repository
public interface ConfigurationItemRepository extends JpaRepository<ConfigurationItemEntity, Long> {

    Optional<ConfigurationItemEntity> findByItemNameAndBusinessUnitIdIsNull(String itemName);
}
