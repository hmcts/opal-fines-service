package uk.gov.hmcts.opal.entity.majorcreditor;

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
@Table(name = "major_creditors")
@SuperBuilder
@NoArgsConstructor
public class MajorCreditorEntity extends AbstractMajorCreditorEntity {

    @OneToOne(mappedBy = "majorCreditor", fetch = FetchType.EAGER)
    private CreditorAccountEntityLite creditorAccountEntity;
}
