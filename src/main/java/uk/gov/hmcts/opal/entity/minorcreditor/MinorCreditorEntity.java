package uk.gov.hmcts.opal.entity.minorcreditor;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntityLite;

@Getter
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "minor_creditors")
@SuperBuilder
@NoArgsConstructor
public class MinorCreditorEntity extends AbstractMinorCreditorEntity{

    @OneToOne(mappedBy = "minorCreditor", fetch = FetchType.EAGER)
    private CreditorAccountEntityLite creditorAccountEntity;
}
