package uk.gov.hmcts.opal.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;

@Repository
public interface ConfigurationItemRepository extends JpaRepository<ConfigurationItemEntity, Long> {

    Optional<ConfigurationItemEntity> findByItemNameAndBusinessUnitIdIsNull(String itemName);

    @EntityGraph(value = ConfigurationItemEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    List<ConfigurationItemEntity> findByItemName(String itemName);
}
