package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementActionDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementOverviewDefendantAccount;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponseDefendantAccount;
import uk.gov.hmcts.opal.util.Versioned;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL) // This line forces the HTTP Response to be of type 'application/json'
public class EnforcementStatus extends GetEnforcementStatusResponseDefendantAccount
    implements ToJsonString, Versioned {

    @JsonIgnore
    private BigInteger version;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private EnforcementOverviewDefendantAccount enforcementOverview;
        private EnforcementOverrideCommon enforcementOverride;
        private EnforcementActionDefendantAccount lastEnforcementAction;
        private String nextEnforcementActionData;
        private GetEnforcementStatusResponseDefendantAccount.DefendantAccountTypeEnum defendantAccountType;
        private AccountStatusReferenceCommon accountStatusReference;
        private Boolean employerFlag;
        private Boolean isHmrcCheckEligible;
        private BigInteger version;

        public Builder enforcementOverview(EnforcementOverviewDefendantAccount enforcementOverview) {
            this.enforcementOverview = enforcementOverview;
            return this;
        }

        public Builder enforcementOverride(EnforcementOverrideCommon enforcementOverride) {
            this.enforcementOverride = enforcementOverride;
            return this;
        }

        public Builder lastEnforcementAction(EnforcementActionDefendantAccount lastEnforcementAction) {
            this.lastEnforcementAction = lastEnforcementAction;
            return this;
        }

        public Builder nextEnforcementActionData(String nextEnforcementActionData) {
            this.nextEnforcementActionData = nextEnforcementActionData;
            return this;
        }

        public Builder defendantAccountType(
            GetEnforcementStatusResponseDefendantAccount.DefendantAccountTypeEnum defendantAccountType) {
            this.defendantAccountType = defendantAccountType;
            return this;
        }

        public Builder accountStatusReference(AccountStatusReferenceCommon accountStatusReference) {
            this.accountStatusReference = accountStatusReference;
            return this;
        }

        public Builder employerFlag(Boolean employerFlag) {
            this.employerFlag = employerFlag;
            return this;
        }

        public Builder isHmrcCheckEligible(Boolean isHmrcCheckEligible) {
            this.isHmrcCheckEligible = isHmrcCheckEligible;
            return this;
        }

        public Builder version(BigInteger version) {
            this.version = version;
            return this;
        }

        public EnforcementStatus build() {
            EnforcementStatus status = new EnforcementStatus();
            status.setEnforcementOverview(enforcementOverview);
            status.setEnforcementOverride(enforcementOverride);
            status.setLastEnforcementAction(lastEnforcementAction);
            status.setNextEnforcementActionData(nextEnforcementActionData);
            status.setDefendantAccountType(defendantAccountType);
            status.setAccountStatusReference(accountStatusReference);
            status.setEmployerFlag(employerFlag);
            status.setIsHmrcCheckEligible(isHmrcCheckEligible);
            status.setVersion(version);
            return status;
        }
    }

}
