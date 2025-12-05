package uk.gov.hmcts.opal.entity.enforcement;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;

@Entity
@Table(name = "enforcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EnforcementFullEntity extends EnforcementEntity {

    @ManyToOne
    @JoinColumn(name = "defendant_account_id", nullable = false, insertable = false, updatable = false)
    private DefendantAccountEntity defendantAccount;

    @ManyToOne
    @JoinColumn(name = "enforcer_id", insertable = false, updatable = false)
    private EnforcerEntity enforcer;

    @ManyToOne
    @JoinColumn(name = "hearing_court_id", nullable = false, insertable = false, updatable = false)
    private CourtEntity.Lite hearingCourt;
}
