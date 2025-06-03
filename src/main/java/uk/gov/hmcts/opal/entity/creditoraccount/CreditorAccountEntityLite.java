package uk.gov.hmcts.opal.entity.creditoraccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "creditor_accounts")
@SuperBuilder
@NoArgsConstructor
public class CreditorAccountEntityLite extends AbstractCreditorAccountEntity {

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private Short businessUnitId;
}
