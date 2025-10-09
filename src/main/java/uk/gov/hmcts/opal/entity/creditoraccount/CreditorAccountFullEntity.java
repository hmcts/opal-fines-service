package uk.gov.hmcts.opal.entity.creditoraccount;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorFullEntity;

@Getter
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "creditor_accounts")
@SuperBuilder
@NoArgsConstructor
public class CreditorAccountFullEntity extends CreditorAccountEntity {

    @ManyToOne
    @JoinColumn(name = "business_unit_id", insertable = false, updatable = false)
    private BusinessUnitFullEntity businessUnit;

    @OneToOne
    @JoinColumn(name = "major_creditor_id", insertable = false, updatable = false)
    private MajorCreditorFullEntity majorCreditor;
}
