package uk.gov.hmcts.opal.entity.imposition;

import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountFullEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "impositions")
@SuperBuilder
@NoArgsConstructor
public class ImpositionFullEntity extends ImpositionEntity {

    @ManyToOne
    @JoinColumn(name = "defendant_account_id", insertable = false, updatable = false)
    private DefendantAccountEntity defendantAccount;

    @ManyToOne
    @JoinColumn(name = "imposing_court_id", insertable = false, updatable = false, nullable = false)
    private CourtEntity.Lite imposingCourt;

    @ManyToOne
    @JoinColumn(name = "creditor_account_id", insertable = false, updatable = false)
    private CreditorAccountFullEntity creditorAccount;
}
