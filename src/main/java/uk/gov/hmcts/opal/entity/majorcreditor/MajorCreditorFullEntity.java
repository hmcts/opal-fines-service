package uk.gov.hmcts.opal.entity.majorcreditor;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;

@Getter
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "major_creditors")
@SuperBuilder
@NoArgsConstructor
public class MajorCreditorFullEntity extends MajorCreditorEntity {

    @OneToOne(mappedBy = "majorCreditor")
    private CreditorAccountEntity creditorAccountEntity;
}
