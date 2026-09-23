package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.Path;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.entity.search.SearchConsolidatedEntity;

@Component
@Slf4j(topic = "opal.SearchBasicEntitySpecs")
public class SearchConsolidatedEntitySpecs extends SearchDefendantAccountSpecs<SearchConsolidatedEntity> {

    public Specification<SearchConsolidatedEntity> findBySearchWithNonZeroBalance(AccountSearchDto accountSearchDto) {
        return findBySearch(accountSearchDto).and(hasNonZeroBalance());
    }

    private Specification<SearchConsolidatedEntity> hasNonZeroBalance() {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Path<BigDecimal> balance = root.get("defendantAccountBalance");

            return criteriaBuilder.and(
                criteriaBuilder.isNotNull(balance),
                criteriaBuilder.notEqual(balance, BigDecimal.ZERO)
            );
        };
    }

}
