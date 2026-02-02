package uk.gov.hmcts.opal.repository.jpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.search.SearchConsolidatedEntity;

@Component
@Slf4j(topic = "opal.SearchBasicEntitySpecs")
public class SearchConsolidatedEntitySpecs extends SearchDefendantAccountSpecs<SearchConsolidatedEntity> {

}
