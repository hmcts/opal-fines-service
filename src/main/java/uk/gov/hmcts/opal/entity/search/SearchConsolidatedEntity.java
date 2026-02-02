package uk.gov.hmcts.opal.entity.search;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;
import uk.gov.hmcts.opal.util.Versioned;

@Getter
@Entity
@Table(name = "v_search_defendant_accounts_consolidation")
@Immutable
@SuperBuilder
@NoArgsConstructor
public class SearchConsolidatedEntity extends SearchDefendantAccount implements Versioned {

    @Column(name = "has_collection_order")
    private Boolean hasCollectionOrder;

    @Column(name = "version_number")
    private Long versionNumber;

    @Column(name = "errors")
    private List<String> errors;

    @Column(name = "warnings")
    private List<String> warnings;

    @Override
    public BigInteger getVersion() {
        return Optional.ofNullable(versionNumber).map(BigInteger::valueOf).orElse(null);
    }
}
