package uk.gov.hmcts.opal.entity.creditoraccount;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;

@Getter
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "creditor_accounts")
@SuperBuilder
@NoArgsConstructor
public class CreditorAccountEntity extends AbstractCreditorAccountEntity {

    @ManyToOne
    @JoinColumn(name = "business_unit_id", updatable = false)
    private BusinessUnitEntity businessUnit;
}
